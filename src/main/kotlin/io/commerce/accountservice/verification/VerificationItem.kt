package io.commerce.accountservice.verification

/**
 * 인증 항목
 */
enum class VerificationItem {

    /**
     * 이메일 회원 가입
     */
    REGISTER,

    /**
     * 비밀번호 초기화
     */
    RESET_PASSWORD,

    /**
     * 비밀번호 재설정
     */
    UPDATE_PASSWORD,

    /**
     * 활성화
     */
    ACTIVATION,

    /**
     * 프로필 수정
     */
    PROFILE
}
