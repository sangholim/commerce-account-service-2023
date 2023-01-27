package io.commerce.accountservice.core

import io.commerce.accountservice.core.SecurityConstants.ROLES
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector
import reactor.core.publisher.Mono

class CustomReactiveOpaqueTokenIntrospector(
    properties: OAuth2ResourceServerProperties.Opaquetoken
) : ReactiveOpaqueTokenIntrospector {
    private val delegate: ReactiveOpaqueTokenIntrospector = NimbusReactiveOpaqueTokenIntrospector(
        properties.introspectionUri,
        properties.clientId,
        properties.clientSecret
    )

    override fun introspect(token: String?): Mono<OAuth2AuthenticatedPrincipal> = delegate
        .introspect(token)
        .map {
            DefaultOAuth2AuthenticatedPrincipal(it.name, it.attributes, extractAuthorities(it))
        }

    private fun extractAuthorities(principal: OAuth2AuthenticatedPrincipal) = principal
        .getAttribute<List<String>>(ROLES)?.map { SimpleGrantedAuthority(it) }
}
