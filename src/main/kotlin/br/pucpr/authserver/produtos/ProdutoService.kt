package br.pucpr.authserver.produtos

import br.pucpr.authserver.categorias.CategoriaRepository
import br.pucpr.authserver.exceptions.BadRequestException
import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.pedidos.ItemPedidoRepository
import br.pucpr.authserver.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProdutoService(
    val repository: ProdutoRepository,
    val categoriaRepository: CategoriaRepository,
    val itemPedidoRepository: ItemPedidoRepository
) {
    fun insert(req: ProdutoRequest): Produto {
        val produto = Produto(
            nome = req.nome!!,
            preco = req.preco!!,
            estoque = req.estoque!!,
            categoria = findCategoriaOrThrow(req.idCategoria!!)
        )
        return repository.save(produto)
            .also { log.info("Produto {} criado na categoria {}", it.id, it.categoria.id) }
    }

    fun search(
        nome: String?,
        idCategoria: Long?,
        precoMin: BigDecimal?,
        precoMax: BigDecimal?,
        sortBy: String?,
        sortDir: String?
    ): List<Produto> {
        if (precoMin != null && precoMax != null && precoMin > precoMax)
            throw BadRequestException("precoMin não pode ser maior que precoMax")

        val campo = ProdutoSortField.findOrThrow(sortBy ?: "NOME")
        val dir = SortDir.findOrThrow(sortDir ?: "ASC")
        val sort = Sort.by(Sort.Direction.valueOf(dir.name), campo.campo)
        val padraoNome = nome?.takeIf { it.isNotBlank() }?.let { "%${it.lowercase()}%" }

        return repository.search(padraoNome, idCategoria, precoMin, precoMax, sort)
    }

    fun findByIdOrThrow(id: Long): Produto =
        repository.findByIdOrNull(id) ?: throw NotFoundException("Produto $id não encontrado")

    fun findByCategoria(idCategoria: Long): List<Produto> {
        findCategoriaOrThrow(idCategoria)
        return repository.findByCategoriaIdOrderByNome(idCategoria)
    }

    fun update(id: Long, req: ProdutoRequest): Produto {
        val produto = findByIdOrThrow(id)
        produto.nome = req.nome!!
        produto.preco = req.preco!!
        produto.estoque = req.estoque!!
        produto.categoria = findCategoriaOrThrow(req.idCategoria!!)
        return repository.save(produto)
            .also { log.info("Produto {} atualizado", id) }
    }

    // Associação: move o produto para outra categoria
    fun changeCategoria(id: Long, idCategoria: Long): Produto {
        val produto = findByIdOrThrow(id)
        produto.categoria = findCategoriaOrThrow(idCategoria)
        return repository.save(produto)
            .also { log.info("Produto {} movido para a categoria {}", id, idCategoria) }
    }

    fun delete(id: Long) {
        val produto = findByIdOrThrow(id)
        if (itemPedidoRepository.existsByProdutoId(id))
            throw ConflictException("Produto $id está em pedidos e não pode ser excluído")
        repository.delete(produto)
        log.warn("Produto {} excluído", id)
    }

    private fun findCategoriaOrThrow(id: Long) =
        categoriaRepository.findByIdOrNull(id) ?: throw NotFoundException("Categoria $id não encontrada")

    companion object {
        private val log = LoggerFactory.getLogger(ProdutoService::class.java)
    }
}
