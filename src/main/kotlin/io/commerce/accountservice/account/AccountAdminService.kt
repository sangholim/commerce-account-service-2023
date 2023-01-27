package io.commerce.accountservice.account

import io.commerce.accountservice.keycloak.KeycloakAdminService
import io.commerce.accountservice.profile.ProfileAdminService
import org.springframework.stereotype.Service

@Service
class AccountAdminService(
    private val keycloakAdminService: KeycloakAdminService,
    private val profileAdminService: ProfileAdminService
) {
    /**
     * aegis 회원 비활성화
     * profile 비활성화
     * @param customerId 고객 ID
     */
    suspend fun disableAccount(customerId: String) {
        keycloakAdminService.disableUser(customerId)
        profileAdminService.disableProfile(customerId)
    }
}
