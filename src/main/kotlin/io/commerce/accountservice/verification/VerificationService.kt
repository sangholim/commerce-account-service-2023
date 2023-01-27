package io.commerce.accountservice.verification

import org.springframework.stereotype.Service

@Service
class VerificationService(
    protected val verificationRepository: VerificationRepository
) {

    /**
     * 인증 데이터 생성
     * 인증 데이터가 이미 있는 경우는 재생성
     * @param item 인증 항목
     * @param expiry 만료 시간 (s)
     * @param payload 인증 요청 데이터
     */
    suspend fun createVerification(
        item: VerificationItem,
        expiry: Int,
        payload: VerificationPayload
    ): Verification {
        val verification = verificationRepository.findByItemAndKey(item, payload.key)?.regenerate(expiry)
            ?: Verification.of(item, expiry, payload.key)
        return verificationRepository.save(verification)
    }

    /**
     * 인증 항목, 키가 일치하는 인증 데이터 가져오기
     * @param item 인증 항목
     * @param key 인증 요청 키
     */
    suspend fun getVerification(item: VerificationItem, key: String): Verification? =
        verificationRepository.findByItemAndKey(item, key)

    /**
     * 인증 데이터 업데이트
     * @param verification 인증 정보
     */
    suspend fun updateVerification(verification: Verification): Verification =
        verificationRepository.save(verification)

    /**
     * 인증 여부 확인
     * @param item 인증 항목
     * @param key 인증 키
     */
    suspend fun isVerified(item: VerificationItem, key: String): Boolean =
        verificationRepository.findByItemAndKey(item, key)?.isVerified ?: false

    /**
     * 인증 데이터 제거
     * @param item 인증 항목
     * @param key 인증 키
     */
    suspend fun deleteVerification(item: VerificationItem, key: String) {
        getVerification(item, key)
            ?.let { verification -> verificationRepository.delete(verification) }
    }
}
