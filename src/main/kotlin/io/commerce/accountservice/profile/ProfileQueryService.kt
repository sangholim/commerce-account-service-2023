package io.commerce.accountservice.profile

import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Page
import org.springframework.data.mongodb.core.query.and
import org.springframework.data.mongodb.core.query.inValues
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.core.query.where
import org.springframework.stereotype.Service

@Service
class ProfileQueryService(
    private val profileRepository: ProfileRepository
) {
    /**
     * 필터 기반 프로필 리스트을 조회합니다.
     * @param adminFilterCriteria 관리자 프로필 필터링 조건
     */
    suspend fun getAllFiltered(adminFilterCriteria: AdminProfileFilterCriteria): Page<Profile> {
        var criteria = where(Profile::createdAt)
            .gte(adminFilterCriteria.createdAtFrom)
            .lte(adminFilterCriteria.createdAtUntil)

        if (adminFilterCriteria.identityProviders.isNotEmpty()) {
            criteria = criteria.and(Profile::identityProviders).inValues(adminFilterCriteria.identityProviders)
        }

        if (adminFilterCriteria.agreement.email != null) {
            criteria = criteria.and(agreementEmailKey).isEqualTo(adminFilterCriteria.agreement.email)
        }

        if (adminFilterCriteria.agreement.sms != null) {
            criteria = criteria.and(agreementSmsKey).isEqualTo(adminFilterCriteria.agreement.sms)
        }

        if (adminFilterCriteria.enabled != null) {
            criteria = criteria.and(Profile::enabled).isEqualTo(adminFilterCriteria.enabled)
        }

        return profileRepository.getAllBy(criteria, adminFilterCriteria.pageRequest)
    }

    /**
     * 키워드 기반 프로필 검색
     * @param criteria 키워드
     */
    fun searchAll(criteria: AdminSearchCriteria): Flow<Profile> =
        profileRepository.searchAll(criteria.query, criteria.pageRequest)
}
