package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AgreementType
import io.commerce.accountservice.account.UpdateAgreementPayload

inline fun updateAgreementPayload(block: UpdateAgreementPayloadFixtureBuilder.() -> Unit = {}) =
    UpdateAgreementPayloadFixtureBuilder().apply(block).build()

class UpdateAgreementPayloadFixtureBuilder {
    var type: AgreementType = AgreementType.EMAIL
    var active: Boolean = false

    fun build() = UpdateAgreementPayload(type, active)
}
