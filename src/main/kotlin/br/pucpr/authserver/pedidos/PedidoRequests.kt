package br.pucpr.authserver.pedidos

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PedidoRequest(
    @NotBlank
    var cliente: String?
)

data class ItemPedidoRequest(
    @NotNull
    @Min(1)
    var idProduto: Long?,

    @NotNull
    @Min(1)
    var quantidade: Int?
)
