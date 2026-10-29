package br.pucpr.authserver.categorias

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoriaRepository : JpaRepository<Categoria, Long> {
    fun findByNomeIgnoreCase(nome: String): Categoria?
}
