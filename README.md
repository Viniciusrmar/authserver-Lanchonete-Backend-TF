# Lanchonete API — Trabalho Final Backend (PUCPR)

Extensão do **AuthServer** visto na disciplina *Back-End: Web Services* (Opção 1 — Estendendo os serviços).

## Integrantes
- Vinicius Robaskievicz Marinho
- Pedro Augusto Stabach

## Vídeo
🎥 _link do YouTube (não listado) aqui_

## Tecnologias
Kotlin · Spring Boot 4 · Spring Data JPA · H2 · Bean Validation · Spring Security + JWT (jjwt) · BCrypt · SpringDoc (Swagger) · SLF4J

## Como rodar
1. Abra o projeto no IntelliJ (Java 21).
2. Rode `AuthserverApplication`.
3. Swagger: http://localhost:8080/api — Console H2: http://localhost:8080/api/h2-console (JDBC URL `jdbc:h2:mem:db`, user `sa`, senha `sa`).

Ao subir, o servidor já cria 3 categorias, 7 produtos e 2 usuários de exemplo:

| Usuário | Senha | Role |
|---|---|---|
| `admin@authserver.com` | `admin` | ADMIN |
| `cliente@authserver.com` | `cliente` | — |

## Segurança (JWT)
1. Faça `POST /api/users/login` com `{"email": "admin@authserver.com", "password": "admin"}`.
2. Copie o `token` da resposta.
3. No Swagger, clique em **Authorize** e cole o token (sem escrever "Bearer").

| Rota | Quem pode |
|---|---|
| `GET` em qualquer recurso, cadastro e login | Público |
| Criar, abrir e montar pedidos (`/pedidos/**`, incluindo remover item) | Usuário logado |
| `DELETE` em qualquer recurso (exceto remover item do próprio pedido) | ADMIN |
| `POST`/`PUT` em categorias e produtos, `PUT` em usuários (roles), `POST` em roles | ADMIN |

Sem token → **401 Unauthorized**. Com token mas sem a role exigida → **403 Forbidden**.

Como funciona:
- **`SecurityConfig`**: define as regras de cada rota, deixa a API *stateless* (sem sessão), desliga o CSRF (não usamos cookie) e configura o CORS.
- **`JwtTokenFilter`**: roda antes de cada requisição, lê o header `Authorization: Bearer <token>` e, se o token for válido, marca o usuário como autenticado.
- **`JWT`**: gera o token (assinado com HMAC-SHA256, com id, nome, roles e expiração de 2h) e valida assinatura, emissor e expiração.
- **Senhas** são salvas com **BCrypt**, nunca em texto puro.
- **Segredo externalizado**: a chave do JWT fica no `application.yaml` e pode ser trocada pela variável de ambiente `JWT_SECRET`.

## Modelo

```
Categoria 1 ──── N Produto            (one-to-many)
Pedido    N ──── N Produto            (many-to-many com tabela no meio: ItemPedido)
              └─ ItemPedido(quantidade, precoUnitario)
```

Classes novas: `Categoria`, `Produto`, `Pedido`, `ItemPedido`.

## Endpoints novos (prefixo `/api`)

### Categorias — CRUD completo
| Método | Rota | Descrição |
|---|---|---|
| GET | `/categorias?sortDir=ASC\|DESC` | Lista ordenada por nome |
| GET | `/categorias/{id}` | Busca por id |
| GET | `/categorias/{id}/produtos` | Produtos da categoria |
| POST | `/categorias` | Cria |
| PUT | `/categorias/{id}` | Atualiza |
| DELETE | `/categorias/{id}` | Exclui (409 se tiver produtos) |

### Produtos — CRUD completo + consulta com filtros
| Método | Rota | Descrição |
|---|---|---|
| GET | `/produtos?nome=&idCategoria=&precoMin=&precoMax=&sortBy=NOME\|PRECO\|ESTOQUE&sortDir=ASC\|DESC` | Consulta com filtragem e ordenação |
| GET | `/produtos/{id}` | Busca por id |
| POST | `/produtos` | Cria |
| PUT | `/produtos/{id}` | Atualiza |
| PUT | `/produtos/{id}/categoria/{idCategoria}` | Associa o produto a outra categoria |
| DELETE | `/produtos/{id}` | Exclui (409 se estiver em pedidos) |

### Usuários (já existentes no AuthServer) + login
| Método | Rota | Descrição |
|---|---|---|
| POST | `/users` | Cadastro (senha salva com BCrypt) |
| POST | `/users/login` | Login — devolve o token JWT |

### Pedidos — associação com produtos
| Método | Rota | Descrição |
|---|---|---|
| GET | `/pedidos?status=ABERTO\|FINALIZADO\|CANCELADO&sortDir=` | Lista com filtro por status |
| GET | `/pedidos/{id}` | Pedido com itens e total |
| POST | `/pedidos` | Abre um pedido |
| POST | `/pedidos/{id}/itens` | Adiciona produto ao pedido (baixa estoque) |
| DELETE | `/pedidos/{id}/itens/{idProduto}` | Remove produto do pedido (devolve estoque) |
| PATCH | `/pedidos/{id}/status?valor=FINALIZADO\|CANCELADO` | Muda o status |
| DELETE | `/pedidos/{id}` | Exclui o pedido |

## Boas práticas aplicadas
- **Validação** com Bean Validation nos requests (`@NotBlank`, `@DecimalMin`, `@PositiveOrZero`, `@Min`).
- **Exceções** customizadas (`NotFoundException`, `ConflictException`, `BadRequestException`) tratadas pelo `GlobalExceptionHandler`, que devolve `ProblemDetail`.
- **Logs** com SLF4J nos services (`info` em criações/alterações, `warn` em exclusões), gravados em `./logs`.
- **Segurança**: Spring Security + JWT, ações destrutivas só para ADMIN, senhas com BCrypt.
- **Regras de negócio**: controle de estoque, preço congelado no item, pedido só pode ser alterado enquanto ABERTO.
