package br.pucpr.authserver.pedidos

import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.produtos.ProdutoRepository
import br.pucpr.authserver.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PedidoService(
    val repository: PedidoRepository,
    val produtoRepository: ProdutoRepository
) {
    fun insert(cliente: String): Pedido =
        repository.save(Pedido(cliente = cliente))
            .also { log.info("Pedido {} criado para {}", it.id, cliente) }

    fun findAll(status: String?, sortDir: String?): List<Pedido> {
        val dir = SortDir.findOrThrow(sortDir ?: "DESC")
        val sort = Sort.by(Sort.Direction.valueOf(dir.name), "dataCriacao")
        return if (status == null) repository.findAll(sort)
        else repository.findByStatus(StatusPedido.findOrThrow(status), sort)
    }

    fun findByIdOrThrow(id: Long): Pedido =
        repository.findByIdOrNull(id) ?: throw NotFoundException("Pedido $id não encontrado")

    // Associação: adiciona um produto ao pedido (ou soma a quantidade se já estiver nele)
    @Transactional
    fun addItem(idPedido: Long, idProduto: Long, quantidade: Int): Pedido {
        val pedido = findAbertoOrThrow(idPedido)
        val produto = produtoRepository.findByIdOrNull(idProduto)
            ?: throw NotFoundException("Produto $idProduto não encontrado")

        if (produto.estoque < quantidade)
            throw BadRequestException("Estoque insuficiente para ${produto.nome}. Disponível: ${produto.estoque}")

        val item = pedido.itens.find { it.produto.id == idProduto }
        if (item == null) {
            pedido.itens.add(
                ItemPedido(pedido = pedido, produto = produto, quantidade = quantidade, precoUnitario = produto.preco)
            )
        } else {
            item.quantidade += quantidade
        }
        produto.estoque -= quantidade

        return repository.save(pedido)
            .also { log.info("Produto {} (x{}) adicionado ao pedido {}", idProduto, quantidade, idPedido) }
    }

    // Associação: remove um produto do pedido e devolve ao estoque
    @Transactional
    fun removeItem(idPedido: Long, idProduto: Long): Pedido {
        val pedido = findAbertoOrThrow(idPedido)
        val item = pedido.itens.find { it.produto.id == idProduto }
            ?: throw NotFoundException("Produto $idProduto não está no pedido $idPedido")

        item.produto.estoque += item.quantidade
        pedido.itens.remove(item)

        return repository.save(pedido)
            .also { log.info("Produto {} removido do pedido {}", idProduto, idPedido) }
    }

    @Transactional
    fun changeStatus(id: Long, novoStatus: String): Pedido {
        val pedido = findAbertoOrThrow(id)
        val status = StatusPedido.findOrThrow(novoStatus)
        if (status == StatusPedido.ABERTO) return pedido

        if (status == StatusPedido.FINALIZADO && pedido.itens.isEmpty())
            throw BadRequestException("Não é possível finalizar um pedido vazio")
        if (status == StatusPedido.CANCELADO) devolverEstoque(pedido)

        pedido.status = status
        return repository.save(pedido)
            .also { log.info("Pedido {} agora está {}", id, status) }
    }

    @Transactional
    fun delete(id: Long) {
        val pedido = findByIdOrThrow(id)
        if (pedido.status == StatusPedido.ABERTO) devolverEstoque(pedido)
        repository.delete(pedido)
        log.warn("Pedido {} excluído", id)
    }

    private fun findAbertoOrThrow(id: Long): Pedido {
        val pedido = findByIdOrThrow(id)
        if (pedido.status != StatusPedido.ABERTO)
            throw ConflictException("Pedido $id está ${pedido.status} e não pode ser alterado")
        return pedido
    }

    private fun devolverEstoque(pedido: Pedido) =
        pedido.itens.forEach { it.produto.estoque += it.quantidade }

    companion object {
        private val log = LoggerFactory.getLogger(PedidoService::class.java)
    }
}
