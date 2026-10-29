package br.pucpr.authserver

import br.pucpr.authserver.categorias.Categoria
import br.pucpr.authserver.categorias.CategoriaRepository
import br.pucpr.authserver.produtos.Produto
import br.pucpr.authserver.produtos.ProdutoRepository
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import java.math.BigDecimal

// Dados de exemplo para testar no Swagger sem precisar cadastrar tudo na mão
@Component
class LojaBootStrapper(
    val categoriaRepository: CategoriaRepository,
    val produtoRepository: ProdutoRepository
) : ApplicationListener<ContextRefreshedEvent> {

    @Transactional
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (categoriaRepository.count() > 0) return

        val bebidas = categoriaRepository.save(Categoria(nome = "Bebidas", descricao = "Cafés, sucos e refrigerantes"))
        val lanches = categoriaRepository.save(Categoria(nome = "Lanches", descricao = "Salgados e sanduíches"))
        val doces = categoriaRepository.save(Categoria(nome = "Doces", descricao = "Bolos e sobremesas"))

        produtoRepository.saveAll(
            listOf(
                Produto(nome = "Café expresso", preco = BigDecimal("6.50"), estoque = 100, categoria = bebidas),
                Produto(nome = "Cappuccino", preco = BigDecimal("9.90"), estoque = 50, categoria = bebidas),
                Produto(nome = "Suco de laranja", preco = BigDecimal("8.00"), estoque = 30, categoria = bebidas),
                Produto(nome = "Coxinha", preco = BigDecimal("7.00"), estoque = 40, categoria = lanches),
                Produto(nome = "Misto quente", preco = BigDecimal("12.00"), estoque = 25, categoria = lanches),
                Produto(nome = "Bolo de cenoura", preco = BigDecimal("8.50"), estoque = 15, categoria = doces),
                Produto(nome = "Brigadeiro", preco = BigDecimal("3.00"), estoque = 60, categoria = doces)
            )
        )
    }
}
