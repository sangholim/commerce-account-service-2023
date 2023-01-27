package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AccountValidationPayload
import io.commerce.accountservice.account.AgreementValidationPayload

inline fun accountValidationPayload(block: AccountValidationPayloadFixtureBuilder.() -> Unit = {}) =
    AccountValidationPayloadFixtureBuilder().apply(block).build()

class AccountValidationPayloadFixtureBuilder {

    var email: String? = null
    var name: String? = null
    var phoneNumber: String? = null
    var password: String? = null
    var agreement: AgreementValidationPayload? = null

    fun build(): AccountValidationPayload = AccountValidationPayload(
        email,
        password,
        name,
        phoneNumber,
        agreement
    )
}
