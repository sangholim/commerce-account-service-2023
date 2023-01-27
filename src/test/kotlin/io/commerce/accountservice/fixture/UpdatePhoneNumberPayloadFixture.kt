package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.UpdatePhoneNumberPayload

inline fun updatePhoneNumberPayload(block: UpdatePhoneNumberPayloadFixtureBuilder.() -> Unit = {}) =
    UpdatePhoneNumberPayloadFixtureBuilder().apply(block).build()

class UpdatePhoneNumberPayloadFixtureBuilder {
    var phoneNumber: String = ""

    fun build() = UpdatePhoneNumberPayload(phoneNumber)
}
