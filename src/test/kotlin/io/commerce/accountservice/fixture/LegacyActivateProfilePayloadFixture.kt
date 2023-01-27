package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AgreementPayload
import io.commerce.accountservice.legacy.LegacyActivateProfilePayload

inline fun legacyActivateProfilePayload(block: LegacyActivateProfilePayloadFixtureBuilder.() -> Unit = {}) =
    LegacyActivateProfilePayloadFixtureBuilder().apply(block).build()

class LegacyActivateProfilePayloadFixtureBuilder {

    var email: String = LegacyAccountFixture.email
    var name: String = "테스터"
    var phoneNumber: String = LegacyAccountFixture.phoneNumber
    var password: String = LegacyAccountFixture.password
    var agreement: AgreementPayload = AgreementPayload(true, true, true, true)

    fun build(): LegacyActivateProfilePayload = LegacyActivateProfilePayload(
        email,
        name,
        phoneNumber,
        password,
        agreement
    )
}

fun LegacyActivateProfilePayload.toUserRepresentation() = userRepresentation {
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
        "requiredAction" to listOf("MIGRATE_ACCOUNT"),
        "v1_password" to listOf("abcd")
    )
    this.password = this@toUserRepresentation.password
    this.federatedIdentities = listOf(
        federatedIdentityRepresentation {
            this.identityProvider = "Kakao"
            this.userId = "123333"
        }
    )
}
