package br.pucpr.authserver.produtos

import jakarta.validation.constraints.*
import java.math.BigDecimal

data class ProdutoRequest(
    @NotBlank
    var nome: String?,

    @NotNull
    @DecimalMin("0.01")
    var preco: BigDecimal?,

    @NotNull
    @PositiveOrZero
    var estoque: Int?,

    @NotNull
    @Min(1)
    var idCategoria: Long?
)
