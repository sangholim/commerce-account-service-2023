package io.commerce.accountservice.fixture

import org.keycloak.representations.idm.FederatedIdentityRepresentation

inline fun federatedIdentityRepresentation(block: FederatedIdentityRepresentationFixtureBuilder.() -> Unit = {}) =
    FederatedIdentityRepresentationFixtureBuilder().apply(block).build()

class FederatedIdentityRepresentationFixtureBuilder {
    var identityProvider: String = ""
    var userId: String = ""
    var userName: String = ""

    fun build() = FederatedIdentityRepresentation().apply {
        this.identityProvider = this@FederatedIdentityRepresentationFixtureBuilder.identityProvider
        this.userId = this@FederatedIdentityRepresentationFixtureBuilder.userId
        this.userName = this@FederatedIdentityRepresentationFixtureBuilder.userName
    }
}
