package br.pucpr.authserver.security

import br.pucpr.authserver.users.User
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

// Dados do usuário logado que ficam disponíveis no SecurityContext
data class UserToken(
    val id: Long,
    val name: String,
    val roles: List<String>
)

@Component
class JWT(
    @Value("\${security.jwt.secret}") secret: String,
    @Value("\${security.jwt.issuer}") private val issuer: String,
    @Value("\${security.jwt.expire-hours}") private val expireHours: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    // Gera o token assinado (HMAC-SHA256) com o id, o nome e as roles do usuário
    fun createToken(user: User): String {
        val now = Instant.now()
        return Jwts.builder()
            .issuer(issuer)
            .subject(user.id.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expireHours, ChronoUnit.HOURS)))
            .claim("name", user.name)
            .claim("roles", user.roles.map { it.name })
            .signWith(key)
            .compact()
    }

    // Lê o header "Authorization: Bearer <token>", valida a assinatura e a expiração
    // e devolve o usuário autenticado (ou null se não houver token válido)
    fun extract(request: HttpServletRequest): Authentication? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        if (!header.startsWith(BEARER)) return null
        val token = header.removePrefix(BEARER).trim()

        return try {
            val claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .payload

            val roles = (claims["roles"] as? List<*>)?.map { it.toString() } ?: emptyList()
            val user = UserToken(
                id = claims.subject.toLong(),
                name = claims["name"]?.toString() ?: "",
                roles = roles
            )
            // O Spring exige o prefixo ROLE_ para que hasRole("ADMIN") funcione
            val authorities = roles.map { SimpleGrantedAuthority("ROLE_$it") }
            UsernamePasswordAuthenticationToken.authenticated(user, null, authorities)
        } catch (e: JwtException) {
            log.warn("Token inválido: {}", e.message)
            null
        } catch (e: IllegalArgumentException) {
            log.warn("Token inválido: {}", e.message)
            null
        }
    }

    companion object {
        private const val BEARER = "Bearer "
        private val log = LoggerFactory.getLogger(JWT::class.java)
    }
}
