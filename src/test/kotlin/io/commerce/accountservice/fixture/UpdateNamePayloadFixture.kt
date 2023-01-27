package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.UpdateNamePayload

inline fun updateNamePayload(block: UpdateNamePayloadFixtureBuilder.() -> Unit = {}) =
    UpdateNamePayloadFixtureBuilder().apply(block).build()

class UpdateNamePayloadFixtureBuilder {
    var name: String = ""

    fun build() = UpdateNamePayload(name)
}
