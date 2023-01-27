package io.commerce.accountservice.verification

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.Instant
import javax.validation.constraints.Max

/**
 * EMAIL, SMS 인증
 */
@Document
data class Verification(
    /**
     * 인증 고유 번호
     */
    @MongoId
    val id: ObjectId? = null,

    /**
     * 인증 항목
     */
    val item: VerificationItem,

    /**
     * 인증 키
     */
    val key: String,

    /**
     * 인증 코드
     */
    val code: String,

    /**
     * 인증 여부
     */
    val isVerified: Boolean,

    /**
     * 재시도 횟수
     */
    @field:Max(value = 4)
    val retryCount: Int,

    /**
     * 만료일
     */
    @Indexed(expireAfterSeconds = 0)
    val expiredAt: Instant,

    /**
     * 인증 데이터 생성일
     */
    @CreatedDate
    val createdAt: Instant? = null,

    /**
     * 인증 데이터 수정일
     */
    @LastModifiedDate
    val modifiedAt: Instant? = null
) {

    /**
     * 코드 인증
     * 성공시 인증 flag 변경, 만료시간 1시간 추가
     * 실패시 미인증 상태로 변경, 재시도 횟수 증가
     */
    fun checkVerifyCode(code: String): Verification =
        if (this.code == code) copy(isVerified = true, expiredAt = Instant.now().plusSeconds(3600L))
        else copy(isVerified = false, retryCount = retryCount.inc())

    /**
     * 재시도 횟수 초과 체크
     */
    fun isExceedRetryCount(): Boolean = retryCount >= VerificationConstants.MAX_RETRY_COUNT

    /**
     * 인증 코드 재발급
     * 만료일, 발급 횟수, 인증 여부, 인증 코드 업데이트
     * @param expiry 만료시간(s)
     */
    fun regenerate(expiry: Int): Verification = copy(
        code = VerificationCode.get(),
        isVerified = false,
        retryCount = 0,
        expiredAt = Instant.now().plusSeconds(expiry.toLong())
    )

    companion object {

        /**
         * 인증 코드 발급
         * @param item 인증 항목
         * @param expiry 만료시간(s)
         * @param key 인증키
         */
        fun of(item: VerificationItem, expiry: Int, key: String): Verification =
            Verification(
                item = item,
                key = key,
                code = VerificationCode.get(),
                isVerified = false,
                retryCount = 0,
                expiredAt = Instant.now().plusSeconds(expiry.toLong())
            )
    }
}
