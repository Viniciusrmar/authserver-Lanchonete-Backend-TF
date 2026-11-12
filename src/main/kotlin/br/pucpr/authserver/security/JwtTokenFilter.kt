package br.pucpr.authserver.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

// Filtro que roda em toda requisição: se houver um token válido, marca o usuário como logado
class JwtTokenFilter(private val jwt: JWT) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        jwt.extract(request)?.let { SecurityContextHolder.getContext().authentication = it }
        filterChain.doFilter(request, response)
    }
}
