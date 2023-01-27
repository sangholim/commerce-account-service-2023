package io.commerce.accountservice.profile

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.Instant
import javax.validation.constraints.Min
import javax.validation.constraints.Size

/**
 * 관리자 프로필 필터링
 */
data class AdminProfileFilterCriteria(
    /**
     * 회원 생성일 기간
     */
    @field: Size(min = 2, max = 2)
    val createdAtRange: List<Instant>,

    /**
     * 소셜 리스트
     */
    val identityProviders: Set<IdentityProviderType> = emptySet(),

    /**
     * 동의항목
     * 필드가 존재하는 경우, 기본 객체 생성이 필요하고
     * 이후 각 필드들은 setter 를 통해 필드값을 저장한다
     */
    val agreement: AgreementFilterCriteria = AgreementFilterCriteria(),

    /**
     * 계정 활성화 여부
     */
    val enabled: Boolean?,

    @Schema(defaultValue = "0")
    @field: Min(0)
    val page: Int = 0
) {
    /**
     * 관리자 동의항목 필터링
     */
    data class AgreementFilterCriteria(
        /**
         * 이메일 수신 동의
         */
        var email: Boolean? = null,

        /**
         * SMS 수신 동의
         */
        var sms: Boolean? = null
    )

    val createdAtFrom: Instant
        get() = createdAtRange[0]

    val createdAtUntil: Instant
        get() = createdAtRange[1]

    val pageRequest: PageRequest
        get() = PageRequest.of(page, 15, Sort.by(Sort.Direction.DESC, Profile::createdAt.name))
}
