package io.commerce.accountservice.account

import javax.validation.constraints.AssertTrue

/**
 * 회원 가입, 로그인 이후 프로필 업데이트 동의 항목
 */
data class AgreementPayload(

    /**
     * 이메일 수신 동의
     */
    val email: Boolean,

    /**
     * sms 수신 동의
     */
    val sms: Boolean,

    /**
     * 서비스 이용 약관 동의
     */
    @field: AssertTrue(message = "필수 동의 항목입니다")
    val serviceTerm: Boolean,

    /**
     * 개인정보 수집 및 동의
     */
    @field: AssertTrue(message = "필수 동의 항목입니다")
    val privacyTerm: Boolean

)
