package io.commerce.accountservice.account

import io.commerce.accountservice.validation.Birthday

/**
 * 생일 수정
 */
data class UpdateBirthdayPayload(

    /**
     * 생년월일
     */
    @Birthday
    val birthday: String
)
