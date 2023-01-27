package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.UpdateEmailPayload

inline fun updateEmailPayload(block: UpdateEmailPayloadFixtureBuilder.() -> Unit = {}) =
    UpdateEmailPayloadFixtureBuilder().apply(block).build()

class UpdateEmailPayloadFixtureBuilder {
    var email: String = ""

    fun build() = UpdateEmailPayload(email)
}
