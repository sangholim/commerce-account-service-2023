package io.commerce.accountservice.keycloak

import io.commerce.accountservice.core.ErrorCodeException
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service

@Service
class KeycloakAdminService(
    private val keycloakUserService: KeycloakUserService
) {
    /**
     * 회원 비활성화
     * @param id 회원 id
     */
    fun disableUser(id: String) {
        keycloakUserService.findOneByCustomerId(id)
            ?.let(UserRepresentation::disable)
            ?.run { keycloakUserService.update(this) }
            ?: throw ErrorCodeException.of(KeycloakError.USER_NOT_FOUND)
    }
}
