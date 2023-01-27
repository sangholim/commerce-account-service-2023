package io.commerce.accountservice.config

import io.commerce.accountservice.core.CustomReactiveOpaqueTokenIntrospector
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.keycloak.KeycloakConstants.ROLE_CUSTOMER
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authorization.AuthorityAuthorizationDecision
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.ReactiveAuthorizationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authorization.AuthorizationContext
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
class WebFluxSecurityConfig(
    private val properties: OAuth2ResourceServerProperties
) {
    private val corsConfig = CorsConfiguration().apply {
        allowedOrigins = listOf("*")
        allowedHeaders = listOf("*")
        allowedMethods = listOf("GET", "HEAD", "POST", "PUT", "DELETE")
    }

    private val authorities = AuthorityUtils.createAuthorityList(ROLE_CUSTOMER)

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http {
            csrf { disable() }
            authorizeExchange {
                authorize("/account/validation", permitAll)
                authorize("/account/register/**", permitAll)
                authorize("/account/reset-password/**", permitAll)
                authorize("/account/legacy/migrate", permitAll)
                authorize("/account/activation/verify/*", authenticated)
                authorize("/account/activation/{customerId}", isAuthenticatedByCustomerId())
                authorize("/account/legacy/activation/{customerId}", isAuthenticatedByCustomerId())
                authorize("/account/update-password/verify/*", hasAuthority(SecurityConstants.CUSTOMER))
                authorize("/account/profile/verify/*", hasAuthority(SecurityConstants.CUSTOMER))
                authorize("/account/profile/{customerId}", hasAuthorityByCustomerId())
                authorize("/account/profile/{customerId}/*", hasAuthorityByCustomerId())
                authorize("/account/{customerId}/shipping-addresses", hasAuthorityByCustomerId())
                authorize("/account/{customerId}/shipping-addresses/**", hasAuthorityByCustomerId())
                authorize("/admin/**", hasAuthority(SecurityConstants.SERVICE_ADMIN))
                authorize(anyExchange, permitAll)
            }
            oauth2ResourceServer {
                opaqueToken { }
            }
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource = UrlBasedCorsConfigurationSource().apply {
        registerCorsConfiguration("/**", corsConfig)
    }

    @Bean
    fun introspector(): ReactiveOpaqueTokenIntrospector = CustomReactiveOpaqueTokenIntrospector(properties.opaquetoken)

    /**
     * 고객 번호에 따른 인증 여부
     * 인증 토큰이 유효,
     * path-variable 값이 고객 번호, 토큰의 고객 번호와 일치
     */
    private fun isAuthenticatedByCustomerId(): ReactiveAuthorizationManager<AuthorizationContext> =
        ReactiveAuthorizationManager { authentication: Mono<Authentication>, context: AuthorizationContext ->
            authentication.filter { it.checkCustomerId(context.customerId()) }.map { AuthorizationDecision(true) }
                .defaultIfEmpty(AuthorizationDecision(false))
        }

    /**
     * 고객 번호에 따른 인증 권한 여부
     * 인증 토큰이 유효, 고객 권한이 존재하고,
     * path-variable 값이 고객 번호, 토큰의 고객 번호와 일치
     */
    private fun hasAuthorityByCustomerId(): ReactiveAuthorizationManager<AuthorizationContext> =
        ReactiveAuthorizationManager { authentication: Mono<Authentication>, context: AuthorizationContext ->
            authentication.filter { it.checkAuthorityByCustomerId(context.customerId()) }
                .map { AuthorizationDecision(true) }
                .defaultIfEmpty(AuthorityAuthorizationDecision(false, authorities))
        }

    private fun AuthorizationContext.customerId(): String = variables["customerId"].toString()

    private fun Authentication.checkCustomerId(customerId: String) = isAuthenticated && name == customerId

    private fun Authentication.checkAuthorityByCustomerId(customerId: String) =
        isAuthenticated && name == customerId && authorities.any { it.authority == ROLE_CUSTOMER }
}
