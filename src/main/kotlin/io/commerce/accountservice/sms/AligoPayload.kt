package io.commerce.accountservice.sms

import com.fasterxml.jackson.annotation.JsonProperty
import io.commerce.accountservice.verification.Verification
import io.commerce.accountservice.verification.VerificationMessage

/**
 * 알리고 요청 필드
 */
data class AligoPayload(
    /**
     * 고유 회원 id
     */
    @JsonProperty("user_id")
    val userId: String,

    /**
     * 고유 키 정보
     */
    val key: String,

    /**
     * 발신자
     */
    val sender: String,

    /**
     * 수신자
     */
    val receiver: String,

    /**
     * 메시지
     */
    @JsonProperty("msg")
    val message: String,

    /**
     * 제목
     */
    val title: String
) {

    companion object {
        /**
         * aligo 요청 필드 생성
         * @param smsProperties sms 설정값
         * @param verification 인증 정보
         */
        fun of(smsProperties: SmsProperties, verification: Verification): AligoPayload = AligoPayload(
            userId = smsProperties.userId,
            key = smsProperties.key,
            sender = smsProperties.sender,
            receiver = verification.key,
            message = VerificationMessage.MESSAGE.format(verification.code),
            title = VerificationMessage.TITLE
        )
    }
}
