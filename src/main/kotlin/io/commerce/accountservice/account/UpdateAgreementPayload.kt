package io.commerce.accountservice.account

/**
 * 동의 항목 수정 필드 데이터
 */
data class UpdateAgreementPayload(
    /**
     * 동의 항목 구분
     */
    val type: AgreementType,

    /**
     * 활성화 여부
     */
    val active: Boolean
)
