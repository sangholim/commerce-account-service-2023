package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.ResetPasswordPayload
import io.commerce.accountservice.account.UpdatePasswordPayload
import io.commerce.accountservice.account.UpdateProfileImagePayload
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation

object AccountFixture {
    const val id: String = "d5f01c47-3447-41cb-9a4d-9146045bc15c"
    const val normalPhoneNumber: String = "09999999999"
    const val newPhoneNumber: String = "02020202020"
    const val loginUser: String = "mock-account-service@stage.commerce"
    const val existUser: String = "duplicate@stage.commerce"
    const val createUser: String = "create@stage.commerce"
    const val password: String = "test1234!"
    const val name: String = "mockName"

    fun createUser(customerId: String? = null) = UserRepresentation().apply {
        this.id = customerId
        this.email = loginUser
        this.username = loginUser
        this.isEnabled = true
        this.isEmailVerified = true
        this.attributes = mapOf(
            "name" to listOf(name),
            "phoneNumber" to listOf(normalPhoneNumber),
            "phoneNumberVerified" to listOf("true"),
            "birthday" to listOf("1999-01-01"),
            "image" to listOf("test-image"),
            "emailAgreed" to listOf("false"),
            "smsAgreed" to listOf("false"),
            "serviceTermAgreed" to listOf("true"),
            "privacyTermAgreed" to listOf("true")
        )

        this.credentials = listOf(
            CredentialRepresentation().apply {
                this.type = "password"
                this.value = password
            }
        )
        this.federatedIdentities = listOf(
            federatedIdentityRepresentation {
                this.identityProvider = "Kakao"
                this.userId = "123333"
            },
            federatedIdentityRepresentation {
                this.identityProvider = "naVer"
                this.userId = "444"
            },
            federatedIdentityRepresentation {
                this.identityProvider = "faceBook"
                this.userId = "44455"
            }
        )
    }

    fun createInvalidResetPasswordPayload(): ResetPasswordPayload = ResetPasswordPayload(
        email = "abc",
        password = "abc"
    )

    fun createResetPasswordPayload(): ResetPasswordPayload = ResetPasswordPayload(
        email = existUser,
        password = password
    )

    fun createInvalidUpdatePasswordPayload(): UpdatePasswordPayload = UpdatePasswordPayload(
        phoneNumber = "1",
        password = "2"
    )

    fun createUpdatePasswordPayload(): UpdatePasswordPayload = UpdatePasswordPayload(
        phoneNumber = normalPhoneNumber,
        password = password
    )

    fun createUpdateProfileImagePayload(): UpdateProfileImagePayload = UpdateProfileImagePayload(
        image = "http://test.test"
    )
}
