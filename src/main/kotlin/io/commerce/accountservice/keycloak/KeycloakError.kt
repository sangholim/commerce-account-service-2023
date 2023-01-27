package io.commerce.accountservice.keycloak

import io.commerce.accountservice.core.BaseError

enum class KeycloakError(override val message: String) : BaseError {
    USER_NOT_FOUND("계정을 찾을수 없습니다")
}
