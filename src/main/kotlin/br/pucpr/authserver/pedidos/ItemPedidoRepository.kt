package br.pucpr.authserver.pedidos

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemPedidoRepository : JpaRepository<ItemPedido, Long> {
    fun existsByProdutoId(idProduto: Long): Boolean
}
