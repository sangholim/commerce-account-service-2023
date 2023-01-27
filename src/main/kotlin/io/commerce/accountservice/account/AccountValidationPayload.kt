package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants
import io.commerce.accountservice.validation.ValidationMessages
import javax.validation.Valid
import javax.validation.constraints.Pattern

/**
 * 회원 정보 유효성 질의 필드
 */
data class AccountValidationPayload(
    /**
     * 이메일
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_EMAIL, message = ValidationMessages.INVALID_FORMAT)
    val email: String?,

    /**
     * 패스워드
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_PASSWORD, message = ValidationMessages.INVALID_PASSWORD)
    val password: String?,

    /**
     * 이름
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_NAME, message = ValidationMessages.INVALID_NAME)
    val name: String?,

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_MOBILE_NUMBER, message = ValidationMessages.INVALID_FORMAT)
    val phoneNumber: String?,

    /**
     * 동의 항목
     */
    @field: Valid
    val agreement: AgreementValidationPayload?
)
