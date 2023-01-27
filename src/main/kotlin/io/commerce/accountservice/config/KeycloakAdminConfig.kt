package io.commerce.accountservice.config

import io.commerce.accountservice.keycloak.KeycloakProperties
import org.keycloak.OAuth2Constants
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(KeycloakProperties::class)
class KeycloakAdminConfig(
    val keycloakProperties: KeycloakProperties
) {
    @Bean
    fun keycloakAdminClientFactory(): Keycloak = KeycloakBuilder.builder()
        .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
        .serverUrl(keycloakProperties.serverUrl)
        .realm(keycloakProperties.realm)
        .clientId(keycloakProperties.clientId)
        .clientSecret(keycloakProperties.clientSecret)
        .build()
}
