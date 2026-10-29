package br.pucpr.authserver.produtos

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/produtos")
@Validated
class ProdutoController(
    val service: ProdutoService
) {
    // Consulta com filtragem e ordenação via query params
    // Ex: GET /api/produtos?nome=cafe&idCategoria=1&precoMax=20&sortBy=PRECO&sortDir=DESC
    @GetMapping
    fun search(
        @RequestParam nome: String?,
        @RequestParam idCategoria: Long?,
        @RequestParam precoMin: BigDecimal?,
        @RequestParam precoMax: BigDecimal?,
        @RequestParam sortBy: String?,
        @RequestParam sortDir: String?
    ) = service.search(nome, idCategoria, precoMin, precoMax, sortBy, sortDir)
        .map { ProdutoResponse(it) }
        .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Min(1) @PathVariable id: Long) =
        ResponseEntity.ok(ProdutoResponse(service.findByIdOrThrow(id)))

    @PostMapping
    fun insert(@Valid @RequestBody produto: ProdutoRequest) =
        service.insert(produto)
            .let { ProdutoResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PutMapping("/{id}")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody produto: ProdutoRequest
    ) = service.update(id, produto)
        .let { ResponseEntity.ok(ProdutoResponse(it)) }

    // Associação: troca a categoria do produto
    @PutMapping("/{id}/categoria/{idCategoria}")
    fun changeCategoria(
        @Min(1) @PathVariable id: Long,
        @Min(1) @PathVariable idCategoria: Long
    ) = service.changeCategoria(id, idCategoria)
        .let { ResponseEntity.ok(ProdutoResponse(it)) }

    @DeleteMapping("/{id}")
    fun delete(@Min(1) @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
