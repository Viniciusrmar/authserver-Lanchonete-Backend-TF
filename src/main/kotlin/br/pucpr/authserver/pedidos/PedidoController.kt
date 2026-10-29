package br.pucpr.authserver.pedidos

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/pedidos")
@Validated
class PedidoController(
    val service: PedidoService
) {
    // Ex: GET /api/pedidos?status=ABERTO&sortDir=ASC
    @GetMapping
    fun list(
        @RequestParam status: String?,
        @RequestParam sortDir: String?
    ) = service.findAll(status, sortDir)
        .map { PedidoResponse(it) }
        .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Min(1) @PathVariable id: Long) =
        ResponseEntity.ok(PedidoResponse(service.findByIdOrThrow(id)))

    @PostMapping
    fun insert(@Valid @RequestBody pedido: PedidoRequest) =
        service.insert(pedido.cliente!!)
            .let { PedidoResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PostMapping("/{id}/itens")
    fun addItem(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody item: ItemPedidoRequest
    ) = service.addItem(id, item.idProduto!!, item.quantidade!!)
        .let { ResponseEntity.ok(PedidoResponse(it)) }

    @DeleteMapping("/{id}/itens/{idProduto}")
    fun removeItem(
        @Min(1) @PathVariable id: Long,
        @Min(1) @PathVariable idProduto: Long
    ) = service.removeItem(id, idProduto)
        .let { ResponseEntity.ok(PedidoResponse(it)) }

    // Ex: PATCH /api/pedidos/1/status?valor=FINALIZADO
    @PatchMapping("/{id}/status")
    fun changeStatus(
        @Min(1) @PathVariable id: Long,
        @RequestParam valor: String
    ) = service.changeStatus(id, valor)
        .let { ResponseEntity.ok(PedidoResponse(it)) }

    @DeleteMapping("/{id}")
    fun delete(@Min(1) @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
