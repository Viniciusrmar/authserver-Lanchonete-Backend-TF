package br.pucpr.authserver.pedidos

import br.pucpr.authserver.exceptions.BadRequestException

enum class StatusPedido {
    ABERTO, FINALIZADO, CANCELADO;

    companion object {
        fun findOrThrow(status: String): StatusPedido =
            entries.find { it.name == status.uppercase() }
                ?: throw BadRequestException("Status inválido! Use ABERTO, FINALIZADO ou CANCELADO")
    }
}
