package io.commerce.accountservice.fixture

import io.commerce.accountservice.verification.Verification
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationType
import org.bson.types.ObjectId
import java.time.Instant

object VerificationFixture {

    const val SUCCESS_CODE: String = "000000"


    fun createVerification() = Verification(
        id = ObjectId.get(),
        item = VerificationItem.REGISTER,
        key = AccountFixture.normalPhoneNumber,
        code = SUCCESS_CODE,
        isVerified = false,
        retryCount = 0,
        expiredAt = Instant.now().plusSeconds(VerificationType.SMS.expiry.toLong())
    )
}

inline fun verification(block: VerificationFixtureBuilder.() -> Unit = {}) =
    VerificationFixtureBuilder().apply(block).build()

class VerificationFixtureBuilder {
    var id: ObjectId? = null
    var item: VerificationItem = VerificationItem.REGISTER
    var key: String = ""
    var code: String = ""
    var isVerified: Boolean = false
    var retryCount: Int = 0
    var expiredAt: Instant = Instant.now()
    var createdAt: Instant? = null
    var modifiedAt: Instant? = null
    fun build() = Verification(
        id, item, key, code, isVerified, retryCount, expiredAt, createdAt, modifiedAt
    )
}
