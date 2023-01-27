package io.commerce.accountservice.fixture

import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.FederatedIdentityRepresentation
import org.keycloak.representations.idm.UserRepresentation

inline fun userRepresentation(block: UserRepresentationFixtureBuilder.() -> Unit = {}) =
    UserRepresentationFixtureBuilder().apply(block).build()

class UserRepresentationFixtureBuilder {
    var id: String = ""
    var username: String = ""
    var enabled: Boolean = false
    var emailVerified: Boolean = false
    var attributes: Map<String, List<String>> = mutableMapOf()
    var federatedIdentities: List<FederatedIdentityRepresentation> = listOf()
    var password: String = ""

    fun build() = UserRepresentation().apply {
        this.id = this@UserRepresentationFixtureBuilder.id
        this.username = this@UserRepresentationFixtureBuilder.username
        this.email = this.username
        this.isEnabled = this@UserRepresentationFixtureBuilder.enabled
        this.isEmailVerified = this@UserRepresentationFixtureBuilder.emailVerified
        this.attributes = this@UserRepresentationFixtureBuilder.attributes
        this.federatedIdentities = this@UserRepresentationFixtureBuilder.federatedIdentities
        this.credentials = listOf(
            CredentialRepresentation().apply {
                this.type = "password"
                this.value = this@UserRepresentationFixtureBuilder.password
            }
        )
    }
}
