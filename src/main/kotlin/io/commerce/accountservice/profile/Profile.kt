package io.commerce.accountservice.profile

import io.commerce.accountservice.keycloak.*
import io.commerce.accountservice.validation.ValidationConstants
import org.bson.types.ObjectId
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.Instant
import java.time.LocalDate
import javax.validation.Valid
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import javax.validation.constraints.PositiveOrZero

/**
 * 회원 프로필
 */
@Document
data class Profile(
    /**
     * 프로필 ID
     */
    @MongoId
    val id: ObjectId? = null,

    /**
     * 고객 ID
     */
    @field:NotBlank
    @Indexed(unique = true)
    val customerId: String,

    /**
     * 활성화 여부
     */
    val enabled: Boolean,

    /**
     * 이메일
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_EMAIL)
    @Indexed(unique = true)
    val email: String,

    /**
     * 이메일 인증 여부
     */
    @field: AssertTrue
    val emailVerified: Boolean,

    /**
     * 이름
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_NAME)
    val name: String,

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = ValidationConstants.PATTERN_MOBILE_NUMBER)
    val phoneNumber: String,

    /**
     * 휴대폰 번호 인증여부
     */
    @field: AssertTrue
    val phoneNumberVerified: Boolean,

    /**
     * 소셜 연동 리스트
     */
    val identityProviders: List<IdentityProviderType>?,

    /**
     * 생년 월일
     */
    val birthday: LocalDate?,

    /**
     * 동의 항목
     */
    @field: Valid
    val agreement: Agreement,

    /**
     * 마감일 기준 주문 건수
     */
    @field: PositiveOrZero
    val orderCount: Int,

    /**
     * 프로필 생성일
     */
    @CreatedDate
    val createdAt: Instant? = null,

    /**
     * 프로필 수정일
     */
    @LastModifiedDate
    val updatedAt: Instant? = null
) {
    /**
     * 동의 항목
     */
    data class Agreement(
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
        @field: AssertTrue
        val serviceTerm: Boolean,

        /**
         * 개인정보 수집 및 동의
         */
        @field: AssertTrue
        val privacyTerm: Boolean
    ) {
        companion object {
            /**
             * 동의 항목 생성
             * @param user 회원 정보
             */
            fun of(user: UserRepresentation) = Agreement(
                user.emailAgreed.toBoolean(),
                user.smsAgreed.toBoolean(),
                user.serviceTermAgreed.toBoolean(),
                user.privacyTermAgreed.toBoolean()
            )
        }
    }

    companion object {
        /**
         * 프로필 생성
         * @param user 회원 정보
         */
        fun of(user: UserRepresentation) = Profile(
            customerId = user.id,
            enabled = user.isEnabled,
            email = user.username,
            emailVerified = user.isEmailVerified,
            name = user.name!!,
            phoneNumber = user.phoneNumber!!,
            phoneNumberVerified = user.phoneNumberVerified.toBoolean(),
            identityProviders = user.identityProviders,
            birthday = user.validBirthday,
            agreement = Agreement.of(user),
            orderCount = 0
        )
    }
}
