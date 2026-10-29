package br.pucpr.authserver.categorias

import br.pucpr.authserver.produtos.ProdutoResponse
import br.pucpr.authserver.produtos.ProdutoService
import br.pucpr.authserver.users.SortDir
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/categorias")
@Validated
class CategoriaController(
    val service: CategoriaService,
    val produtoService: ProdutoService
) {
    @GetMapping
    fun list(@RequestParam sortDir: String?) =
        service.findAll(SortDir.findOrThrow(sortDir ?: "ASC"))
            .map { CategoriaResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@Min(1) @PathVariable id: Long) =
        ResponseEntity.ok(CategoriaResponse(service.findByIdOrThrow(id)))

    // Associação: lista os produtos de uma categoria (one-to-many)
    @GetMapping("/{id}/produtos")
    fun listProdutos(@Min(1) @PathVariable id: Long) =
        produtoService.findByCategoria(id)
            .map { ProdutoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PostMapping
    fun insert(@Valid @RequestBody categoria: CategoriaRequest) =
        service.insert(categoria.toCategoria())
            .let { CategoriaResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @PutMapping("/{id}")
    fun update(
        @Min(1) @PathVariable id: Long,
        @Valid @RequestBody categoria: CategoriaRequest
    ) = service.update(id, categoria.toCategoria())
        .let { ResponseEntity.ok(CategoriaResponse(it)) }

    @DeleteMapping("/{id}")
    fun delete(@Min(1) @PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
