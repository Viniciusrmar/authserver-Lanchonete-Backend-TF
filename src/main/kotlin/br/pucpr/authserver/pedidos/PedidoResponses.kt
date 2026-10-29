package br.pucpr.authserver.pedidos

import java.math.BigDecimal
import java.time.LocalDateTime

data class ItemPedidoResponse(
    val idProduto: Long,
    val produto: String,
    val quantidade: Int,
    val precoUnitario: BigDecimal,
    val subtotal: BigDecimal
) {
    constructor(item: ItemPedido) : this(
        idProduto = item.produto.id!!,
        produto = item.produto.nome,
        quantidade = item.quantidade,
        precoUnitario = item.precoUnitario,
        subtotal = item.subtotal()
    )
}

data class PedidoResponse(
    val id: Long,
    val cliente: String,
    val dataCriacao: LocalDateTime,
    val status: StatusPedido,
    val itens: List<ItemPedidoResponse>,
    val total: BigDecimal
) {
    constructor(pedido: Pedido) : this(
        id = pedido.id!!,
        cliente = pedido.cliente,
        dataCriacao = pedido.dataCriacao,
        status = pedido.status,
        itens = pedido.itens.map { ItemPedidoResponse(it) },
        total = pedido.total()
    )
}
