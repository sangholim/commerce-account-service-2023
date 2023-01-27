package io.commerce.accountservice.verification

import io.commerce.accountservice.core.ErrorFieldException
import io.commerce.accountservice.core.SimpleFieldError

/**
 * 인증 요청/검증
 */
data class VerificationPayload(

    /**
     * 인증 수단 (이메일/sms)
     */
    val key: String,

    /**
     * 인증 코드
     */
    val code: String? = null
) {

    fun validation(type: VerificationType) {
        if (type.regex.find(this.key) != null) return
        throw ErrorFieldException(listOf(SimpleFieldError(type.fieldName, "올바르지 않은 형식입니다")))
    }
}
