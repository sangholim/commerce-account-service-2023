package io.commerce.accountservice.keycloak

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "keycloak")
data class KeycloakProperties(
    val serverUrl: String,
    val realm: String,
    val clientId: String,
    val clientSecret: String
)
