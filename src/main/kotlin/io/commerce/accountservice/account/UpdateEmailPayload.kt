package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_EMAIL
import javax.validation.constraints.Pattern

/**
 * 이메일 수정
 */
data class UpdateEmailPayload(

    /**
     * 이메일
     */
    @field: Pattern(regexp = PATTERN_EMAIL, message = "올바르지 않은 형식입니다")
    val email: String
)
