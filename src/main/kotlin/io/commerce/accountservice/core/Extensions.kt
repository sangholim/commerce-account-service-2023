package io.commerce.accountservice.core

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal

val OAuth2AuthenticatedPrincipal.customerId: String
    get() = attributes["sub"] as String
