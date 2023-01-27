package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_EMAIL
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_MOBILE_NUMBER
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_NAME
import io.commerce.accountservice.validation.ValidationMessages
import javax.validation.Valid
import javax.validation.constraints.Pattern

/**
 * 로그인 이후 프로필 업데이트
 */
data class ActivateProfilePayload(
    /**
     * 이메일
     */
    @field: Pattern(regexp = PATTERN_EMAIL, message = "올바르지 않은 형식입니다")
    val email: String,

    /**
     * 이름
     */
    @field: Pattern(regexp = PATTERN_NAME, message = ValidationMessages.INVALID_NAME)
    val name: String,

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = PATTERN_MOBILE_NUMBER, message = "올바르지 않은 형식입니다")
    val phoneNumber: String,

    /**
     * 동의 항목
     */
    @field:Valid
    val agreement: AgreementPayload

)
