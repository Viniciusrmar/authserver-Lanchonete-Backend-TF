package br.pucpr.authserver.pedidos

import br.pucpr.authserver.produtos.Produto
import jakarta.persistence.*
import java.math.BigDecimal

// Tabela no meio do many-to-many Pedido <-> Produto, com dados extras (quantidade e preço)
@Entity
class ItemPedido(
    @Id @GeneratedValue
    var id: Long? = null,

    @ManyToOne(optional = false)
    @JoinColumn(name = "idPedido")
    var pedido: Pedido,

    @ManyToOne(optional = false)
    @JoinColumn(name = "idProduto")
    var produto: Produto,

    @Column(nullable = false)
    var quantidade: Int,

    // Preço "congelado" no momento da compra
    @Column(nullable = false, precision = 10, scale = 2)
    var precoUnitario: BigDecimal
) {
    fun subtotal(): BigDecimal = precoUnitario * BigDecimal(quantidade)
}
