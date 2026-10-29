package br.pucpr.authserver.produtos

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProdutoRepository : JpaRepository<Produto, Long> {
    fun existsByCategoriaId(idCategoria: Long): Boolean
    fun findByCategoriaIdOrderByNome(idCategoria: Long): List<Produto>

    // Cada filtro só é aplicado se o parâmetro não for nulo
    @Query(
        """
            select p from Produto p
            where (:nome is null or lower(p.nome) like :nome)
              and (:idCategoria is null or p.categoria.id = :idCategoria)
              and (:precoMin is null or p.preco >= :precoMin)
              and (:precoMax is null or p.preco <= :precoMax)
        """
    )
    fun search(
        @Param("nome") nome: String?,
        @Param("idCategoria") idCategoria: Long?,
        @Param("precoMin") precoMin: BigDecimal?,
        @Param("precoMax") precoMax: BigDecimal?,
        sort: Sort
    ): List<Produto>
}
