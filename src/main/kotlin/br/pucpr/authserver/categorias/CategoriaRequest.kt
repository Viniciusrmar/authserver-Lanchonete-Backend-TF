package br.pucpr.authserver.categorias

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CategoriaRequest(
    @NotBlank
    @Size(max = 50)
    var nome: String?,

    @NotBlank
    var descricao: String?
) {
    fun toCategoria() = Categoria(
        id = null,
        nome = nome!!,
        descricao = descricao!!
    )
}
