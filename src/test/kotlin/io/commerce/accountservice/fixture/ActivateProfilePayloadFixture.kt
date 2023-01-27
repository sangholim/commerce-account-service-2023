package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.ActivateProfilePayload
import io.commerce.accountservice.account.AgreementPayload
import io.commerce.accountservice.keycloak.KeycloakConstants

inline fun activateProfilePayload(block: ActivateProfilePayloadFixtureBuilder.() -> Unit = {}) =
    ActivateProfilePayloadFixtureBuilder().apply(block).build()

class ActivateProfilePayloadFixtureBuilder {

    var email: String = AccountFixture.loginUser
    var name: String = io.commerce.accountservice.fixture.AccountFixture.name
    var phoneNumber: String = AccountFixture.normalPhoneNumber
    var agreement: AgreementPayload = AgreementPayload(true, true, true, true)

    fun build(): ActivateProfilePayload = ActivateProfilePayload(
        email,
        name,
        phoneNumber,
        agreement
    )
}

fun ActivateProfilePayload.toUserRepresentation() = userRepresentation {
    this.username = email
    this.emailVerified = true
    this.enabled = true
    this.attributes = mapOf(
        "name" to listOf(name),
        "phoneNumber" to listOf(phoneNumber),
        "phoneNumberVerified" to listOf("false"),
        "emailAgreed" to listOf(agreement.email.toString()),
        "smsAgreed" to listOf(agreement.sms.toString()),
        "serviceTermAgreed" to listOf(agreement.serviceTerm.toString()),
        "privacyTermAgreed" to listOf(agreement.privacyTerm.toString()),
        "requiredAction" to listOf(KeycloakConstants.UPDATE_PROFILE)
    )
    this.federatedIdentities = listOf(
        federatedIdentityRepresentation {
            this.identityProvider = "Kakao"
            this.userId = "123333"
        }
    )
}
