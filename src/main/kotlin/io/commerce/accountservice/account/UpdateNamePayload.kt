package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_NAME
import io.commerce.accountservice.validation.ValidationMessages
import javax.validation.constraints.Pattern

/**
 * 이름 수정
 */
data class UpdateNamePayload(

    /**
     * 이름
     */
    @field: Pattern(regexp = PATTERN_NAME, message = ValidationMessages.INVALID_NAME)
    val name: String
)
