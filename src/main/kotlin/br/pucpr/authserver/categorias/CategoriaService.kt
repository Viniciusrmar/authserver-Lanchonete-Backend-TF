package br.pucpr.authserver.categorias

import br.pucpr.authserver.exceptions.ConflictException
import br.pucpr.authserver.exceptions.NotFoundException
import br.pucpr.authserver.produtos.ProdutoRepository
import br.pucpr.authserver.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CategoriaService(
    val repository: CategoriaRepository,
    val produtoRepository: ProdutoRepository
) {
    fun insert(categoria: Categoria): Categoria {
        if (repository.findByNomeIgnoreCase(categoria.nome) != null)
            throw ConflictException("Categoria ${categoria.nome} já existe")
        return repository.save(categoria)
            .also { log.info("Categoria {} criada", it.id) }
    }

    fun findAll(dir: SortDir): List<Categoria> =
        repository.findAll(Sort.by(Sort.Direction.valueOf(dir.name), "nome"))

    fun findByIdOrThrow(id: Long): Categoria =
        repository.findByIdOrNull(id) ?: throw NotFoundException("Categoria $id não encontrada")

    fun update(id: Long, dados: Categoria): Categoria {
        val categoria = findByIdOrThrow(id)
        val outra = repository.findByNomeIgnoreCase(dados.nome)
        if (outra != null && outra.id != id)
            throw ConflictException("Categoria ${dados.nome} já existe")

        categoria.nome = dados.nome
        categoria.descricao = dados.descricao
        return repository.save(categoria)
            .also { log.info("Categoria {} atualizada", id) }
    }

    fun delete(id: Long) {
        val categoria = findByIdOrThrow(id)
        if (produtoRepository.existsByCategoriaId(id))
            throw ConflictException("Categoria $id possui produtos e não pode ser excluída")
        repository.delete(categoria)
        log.warn("Categoria {} excluída", id)
    }

    companion object {
        private val log = LoggerFactory.getLogger(CategoriaService::class.java)
    }
}
