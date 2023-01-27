package io.commerce.accountservice.verification

object VerificationConstants {
    const val VERIFICATION_SEND_FAIL_MESSAGE = "인증할 수 없는 번호입니다"
    const val VERIFICATION_DIFFERENT_MESSAGE = "인증번호가 일치하지 않습니다"
    const val VERIFICATION_MAX_RETRY_MESSAGE = "5번 틀리셨습니다. 인증번호를 다시 받아주세요"
    const val VERIFICATION_INVALID_MESSAGE = "존재하지 않는 인증 정보입니다"
    const val MAX_RETRY_COUNT = 4
}
