package io.commerce.accountservice.keycloak

import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.resource.RealmResource
import org.keycloak.admin.client.resource.UserResource
import org.keycloak.representations.idm.RoleRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import javax.ws.rs.ClientErrorException
import javax.ws.rs.core.Response

@Service
class KeycloakUserService(
    keycloak: Keycloak,
    keycloakProperties: KeycloakProperties
) {
    private val resource: RealmResource = keycloak.realm(keycloakProperties.realm)

    fun create(userRepresentation: UserRepresentation): Response =
        resource.users().create(userRepresentation)

    fun findByUsername(username: String): List<UserRepresentation> = resource.users().search(username, true)

    fun count(username: String): Int = findByUsername(username).size

    fun findOneByCustomerId(id: String): UserRepresentation? = try {
        getUserResource(id).toRepresentation()
    } catch (exception: ClientErrorException) {
        throw ResponseStatusException(exception.response.status, null, exception)
    }
    fun findOneByUsername(username: String): UserRepresentation? {
        val users = findByUsername(username)
        if (users.isEmpty()) {
            return null
        }
        return users.first()
    }

    fun addCustomerRoleToUser(id: String) = getUserResource(id).roles().realmLevel().add(listOf(findCustomerRoleInRealm()))

    private fun findCustomerRoleInRealm(): RoleRepresentation = resource.roles().get(KeycloakConstants.ROLE_CUSTOMER).toRepresentation()

    fun getUserResource(id: String): UserResource = resource.users().get(id)

    fun findAll(): List<UserRepresentation> = resource.users().list()

    fun update(userRepresentation: UserRepresentation) = getUserResource(userRepresentation.id).update(userRepresentation)
}
