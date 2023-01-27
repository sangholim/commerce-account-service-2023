package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.UpdateBirthdayPayload

inline fun updateBirthdayPayload(block: UpdateBirthdayPayloadFixtureBuilder.() -> Unit = {}) =
    UpdateBirthdayPayloadFixtureBuilder().apply(block).build()

class UpdateBirthdayPayloadFixtureBuilder {
    var birthday: String = ""

    fun build() = UpdateBirthdayPayload(birthday)
}
