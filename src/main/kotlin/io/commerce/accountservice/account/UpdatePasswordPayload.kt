package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_MOBILE_NUMBER
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_PASSWORD
import javax.validation.constraints.Pattern

/**
 * 비밀번호 재설정
 */
data class UpdatePasswordPayload(

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = PATTERN_MOBILE_NUMBER, message = "올바르지 않은 형식입니다")
    val phoneNumber: String,

    /**
     * 비밀번호
     */
    @field: Pattern(regexp = PATTERN_PASSWORD, message = "8~36자 영문, 숫자, 특수문자를 사용하세요")
    val password: String
)
