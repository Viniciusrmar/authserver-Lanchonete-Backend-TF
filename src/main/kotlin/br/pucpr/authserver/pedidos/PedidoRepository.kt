package br.pucpr.authserver.pedidos

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PedidoRepository : JpaRepository<Pedido, Long> {
    fun findByStatus(status: StatusPedido, sort: Sort): List<Pedido>
}
