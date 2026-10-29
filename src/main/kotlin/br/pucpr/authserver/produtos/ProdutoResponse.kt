package br.pucpr.authserver.produtos

import br.pucpr.authserver.categorias.CategoriaResponse
import java.math.BigDecimal

data class ProdutoResponse(
    val id: Long,
    val nome: String,
    val preco: BigDecimal,
    val estoque: Int,
    val categoria: CategoriaResponse
) {
    constructor(produto: Produto) : this(
        id = produto.id!!,
        nome = produto.nome,
        preco = produto.preco,
        estoque = produto.estoque,
        categoria = CategoriaResponse(produto.categoria)
    )
}
