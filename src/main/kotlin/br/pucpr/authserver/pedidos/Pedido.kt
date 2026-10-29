package br.pucpr.authserver.pedidos

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
class Pedido(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var cliente: String,

    @Column(nullable = false)
    var dataCriacao: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StatusPedido = StatusPedido.ABERTO,

    // Um pedido tem vários itens. Os itens são salvos/removidos junto com o pedido.
    @OneToMany(mappedBy = "pedido", cascade = [CascadeType.ALL], orphanRemoval = true)
    var itens: MutableList<ItemPedido> = mutableListOf()
) {
    fun total(): BigDecimal = itens.fold(BigDecimal.ZERO) { soma, item -> soma + item.subtotal() }
}
