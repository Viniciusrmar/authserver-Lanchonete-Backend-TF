package br.pucpr.authserver.produtos

import br.pucpr.authserver.exceptions.BadRequestException

enum class ProdutoSortField(val campo: String) {
    NOME("nome"), PRECO("preco"), ESTOQUE("estoque");

    companion object {
        fun findOrThrow(sortBy: String): ProdutoSortField =
            entries.find { it.name == sortBy.uppercase() }
                ?: throw BadRequestException("Campo de ordenação inválido! Use NOME, PRECO ou ESTOQUE")
    }
}
