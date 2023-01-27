package io.commerce.accountservice.verification

/**
 * 인증 코드
 */
object VerificationCode {
    private const val SIZE = 6
    private val CHARSET = (0..9)

    /**
     * 인증 번호생성
     */
    fun get(): String =
        List(SIZE) { CHARSET.random() }.joinToString("")
}
