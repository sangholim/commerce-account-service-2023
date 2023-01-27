package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AgreementPayload
import io.commerce.accountservice.account.RegisterPayload

inline fun registerPayload(block: RegisterPayloadFixtureBuilder.() -> Unit = {}) =
    RegisterPayloadFixtureBuilder().apply(block).build()

class RegisterPayloadFixtureBuilder {

    var email: String = ""
    var password: String = ""
    var name: String = ""
    var phoneNumber: String = ""
    var agreement: AgreementPayload = AgreementPayload(true, true, true, true)

    fun build(): RegisterPayload = RegisterPayload(
        email,
        password,
        name,
        phoneNumber,
        agreement
    )
}
