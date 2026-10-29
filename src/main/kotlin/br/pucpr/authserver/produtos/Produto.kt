package br.pucpr.authserver.produtos

import br.pucpr.authserver.categorias.Categoria
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
class Produto(
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var nome: String,

    @Column(nullable = false, precision = 10, scale = 2)
    var preco: BigDecimal,

    @Column(nullable = false)
    var estoque: Int,

    // Muitos produtos pertencem a uma categoria (lado "many" do one-to-many)
    @ManyToOne(optional = false)
    @JoinColumn(name = "idCategoria")
    var categoria: Categoria
)
