package io.commerce.accountservice.keycloak

import org.keycloak.representations.idm.FederatedIdentityRepresentation
import java.io.Serializable

/**
 * 소셜 미디어
 */
data class IdentityProvider(

    /**
     * 소셜 미디어 타입
     */
    val type: String = "",

    /**
     * 계정 ID
     */
    val userId: String = ""
) : Serializable {

    companion object {
        fun create(federatedIdentityRepresentation: FederatedIdentityRepresentation) = IdentityProvider(
            federatedIdentityRepresentation.identityProvider,
            federatedIdentityRepresentation.userId
        )
    }
}
