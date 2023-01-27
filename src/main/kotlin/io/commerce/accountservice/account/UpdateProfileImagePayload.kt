package io.commerce.accountservice.account

import javax.validation.constraints.NotBlank

/**
 * 프로필 이미지 Bucket 경로 수정
 */
data class UpdateProfileImagePayload(

    /**
     * 프로필 이미지 Bucket 경로
     */
    @field:NotBlank(message = "올바르지 않은 형식입니다")
    val image: String
)
