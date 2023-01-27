package io.commerce.accountservice.profile

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Criteria

interface ProfilePagingRepository {
    suspend fun getAllBy(criteria: Criteria, pageable: Pageable): Page<Profile>
}
