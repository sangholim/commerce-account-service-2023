package io.commerce.accountservice.legacy

/**
 * v1 계정 로그인
 */
data class LegacyAccountLoginPayload(

    /**
     * 이메일
     */
    val email: String,

    /**
     * 패스워드
     */
    val password: String
)
