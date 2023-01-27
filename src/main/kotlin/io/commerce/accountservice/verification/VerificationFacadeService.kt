package io.commerce.accountservice.verification

import io.commerce.accountservice.mail.MailService
import io.commerce.accountservice.sms.SmsClient
import io.commerce.accountservice.verification.VerificationType.EMAIL
import io.commerce.accountservice.verification.VerificationType.SMS
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import org.springframework.stereotype.Service

/**
 * 인증 통합 서비스
 */
@Service
class VerificationFacadeService(
    private val mailService: MailService,
    private val smsClient: SmsClient,
    private val verificationService: VerificationService
) {

    /**
     * 인증 메시지 발송
     * @param item 인증 항목
     * @param type 인증 구분
     * @param payload 인증 데이터 필드
     */
    suspend fun sendVerificationMessage(
        item: VerificationItem,
        type: VerificationType,
        payload: VerificationPayload
    ): Verification = verificationService.createVerification(item, type.expiry, payload)
        .also { verification ->
            sendVerificationMessage(type, verification).collect()
        }

    /**
     * 인증 정보 검사
     * @param item 인증 항목
     * @param payload 인증 데이터 필드
     */
    suspend fun checkVerification(item: VerificationItem, payload: VerificationPayload): Boolean {
        val verification = verificationService.getVerification(item, payload.key)?.let { verification ->
            // 인증 번호 재시도 횟수 초과
            if (verification.isExceedRetryCount()) throw VerificationExceedLimitException()
            // 인증 코드 검사 결과 저장
            verificationService.updateVerification(verification.checkVerifyCode(payload.code!!))
        } ?: throw VerificationInvalidException()
        if (!verification.isVerified) throw VerificationFailException()
        return true
    }

    /**
     * 인증 여부 확인
     * @param item 인증 항목
     * @param key 인증 키
     */
    suspend fun isVerified(item: VerificationItem, key: String): Boolean = verificationService.isVerified(item, key)

    /**
     * 인증 데이터 제거
     * @param item 인증 항목
     * @param key 인증키
     */
    suspend fun deleteVerification(item: VerificationItem, key: String) =
        verificationService.deleteVerification(item, key)

    /**
     * 인증 메세지 발송
     * @param type 발송 구분
     * @param verification 인증 데이터
     */
    private suspend fun sendVerificationMessage(type: VerificationType, verification: Verification) =
        flowOf(
            when (type) {
                EMAIL -> mailService.sendVerificationMessage(verification)
                SMS -> smsClient.sendVerificationMessage(verification)
            }
        ).catch { throw it }
}
