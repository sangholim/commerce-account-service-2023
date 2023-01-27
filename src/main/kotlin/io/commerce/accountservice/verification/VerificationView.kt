package io.commerce.accountservice.verification

/**
 * 인증 요청 결과
 */
data class VerificationView(

    /**
     * 유효 시간(s)
     */
    val expiredIn: Int,

    /**
     * 만료 시간 timestamp
     */
    val expiredAt: Long
)

/**
 * 인증 요청 결과 변환
 * @param expiredIn 만료 시간
 */
fun Verification.toVerificationView(expiredIn: Int): VerificationView =
    VerificationView(
        expiredIn = expiredIn,
        expiredAt = expiredAt.toEpochMilli()
    )
