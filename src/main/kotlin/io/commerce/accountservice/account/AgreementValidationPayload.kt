package io.commerce.accountservice.account

import io.commerce.accountservice.validation.ValidationMessages
import javax.validation.constraints.AssertTrue

/**
 * 회원 가입, 로그인 이후 프로필 업데이트 동의 항목
 */
data class AgreementValidationPayload(
    /**
     * 서비스 이용 약관 동의
     */
    @field: AssertTrue(message = ValidationMessages.REQUIRED_AGREEMENT)
    val serviceTerm: Boolean?,

    /**
     * 개인정보 수집 및 동의
     */
    @field: AssertTrue(message = ValidationMessages.REQUIRED_AGREEMENT)
    val privacyTerm: Boolean?

)
