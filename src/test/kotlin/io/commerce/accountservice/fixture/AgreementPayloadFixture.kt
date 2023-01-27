package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AgreementPayload

inline fun agreementPayload(block: AgreementPayloadFixtureBuilder.() -> Unit = {}) =
    AgreementPayloadFixtureBuilder().apply(block).build()

class AgreementPayloadFixtureBuilder {
    var email: Boolean = false
    var sms: Boolean = false
    var serviceTerm: Boolean = false
    var privacyTerm: Boolean = false
    fun build() = AgreementPayload(
        email, sms, serviceTerm, privacyTerm
    )
}
