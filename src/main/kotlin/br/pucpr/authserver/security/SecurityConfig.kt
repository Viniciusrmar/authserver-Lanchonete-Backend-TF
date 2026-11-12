package br.pucpr.authserver.security

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
// Adiciona o botão "Authorize" no Swagger para enviar o token
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@OpenAPIDefinition(security = [SecurityRequirement(name = "bearerAuth")])
class SecurityConfig(
    private val jwt: JWT
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }                     // API stateless com token: não usa cookie de sessão
        .cors(Customizer.withDefaults())
        .headers { it.frameOptions { f -> f.sameOrigin() } }   // libera o console do H2
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        .authorizeHttpRequests { auth ->
            auth
                // Cadastro e login são públicos
                .requestMatchers(HttpMethod.POST, "/users", "/users/login").permitAll()

                // Cliente logado pode montar o próprio pedido (inclusive remover itens)
                .requestMatchers(HttpMethod.DELETE, "/pedidos/*/itens/*").authenticated()

                // Ações destrutivas: só ADMIN
                .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                // Gerenciar catálogo e permissões: só ADMIN
                .requestMatchers(HttpMethod.POST, "/categorias/**", "/produtos/**", "/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categorias/**", "/produtos/**", "/users/**").hasRole("ADMIN")

                // Pedidos e alteração de usuário exigem login
                .requestMatchers("/pedidos/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/users/**").authenticated()

                // Consultas (GET), Swagger, health e H2 ficam abertos
                .anyRequest().permitAll()
        }
        .addFilterBefore(JwtTokenFilter(jwt), UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.addAllowedOriginPattern("*")
        config.addAllowedMethod("*")
        config.addAllowedHeader("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
