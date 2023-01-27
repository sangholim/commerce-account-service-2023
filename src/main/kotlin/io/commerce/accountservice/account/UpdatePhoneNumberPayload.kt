package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_MOBILE_NUMBER
import javax.validation.constraints.Pattern

/**
 * 휴대폰 번호 수정
 */
data class UpdatePhoneNumberPayload(

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = PATTERN_MOBILE_NUMBER, message = "올바르지 않은 형식입니다")
    val phoneNumber: String
)
