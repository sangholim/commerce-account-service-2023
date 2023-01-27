package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_EMAIL
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_PASSWORD
import javax.validation.constraints.Pattern

/**
 * 비밀번호 초기화
 */
data class ResetPasswordPayload(

    /**
     * 이메일
     */
    @field: Pattern(regexp = PATTERN_EMAIL, message = "올바르지 않은 형식입니다")
    var email: String,

    /**
     * 비밀번호
     */
    @field: Pattern(regexp = PATTERN_PASSWORD, message = "8~36자 영문, 숫자, 특수문자를 사용하세요")
    var password: String
)
