package io.commerce.accountservice.verification

import io.commerce.accountservice.validation.ValidationConstants.REGEX_EMAIL
import io.commerce.accountservice.validation.ValidationConstants.REGEX_MOBILE_NUMBER

/**
 * 인증 구분
 */
enum class VerificationType(
    /**
     * 만료 시간
     */
    val expiry: Int,

    /**
     * 구분에 따른 유효성 검사
     */
    val regex: Regex,

    /**
     * 필드명
     */
    val fieldName: String
) {
    EMAIL(3600, REGEX_EMAIL, "email"), SMS(180, REGEX_MOBILE_NUMBER, "phoneNumber");
}
