package io.commerce.accountservice.fixture

import io.commerce.accountservice.profile.Profile

inline fun agreement(block: AgreementFixtureBuilder.() -> Unit = {}) =
    AgreementFixtureBuilder().apply(block).build()

class AgreementFixtureBuilder {
    var email: Boolean = false
    var sms: Boolean = false
    var serviceTerm: Boolean = false
    var privacyTerm: Boolean = false

    fun build() = Profile.Agreement(
        email,
        sms,
        serviceTerm,
        privacyTerm
    )
}
