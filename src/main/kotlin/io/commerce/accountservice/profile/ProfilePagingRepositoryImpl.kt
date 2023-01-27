package io.commerce.accountservice.profile

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.flow
import org.springframework.data.mongodb.core.query
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class ProfilePagingRepositoryImpl(
    @Qualifier("reactiveMongoTemplate")
    private val ops: ReactiveMongoTemplate
) : ProfilePagingRepository {
    /**
     * 코루틴 기반
     * 필터링된 프로필 리스트 조회
     *
     * @param criteria 필드 조건문
     * @param pageable 페이지 정의
     */
    override suspend fun getAllBy(criteria: Criteria, pageable: Pageable): Page<Profile> = coroutineScope {
        val query = Query(criteria)
        val pageableQuery =  Query(criteria).with(pageable)

        /**
         * WARN: Query 객체에 clone 기능이 없어 pageable이 적용된 것과 적용되지 않은 것으로 각각 선언해야 합니다.
         */
        val content = async { ops.query<Profile>().matching(pageableQuery).flow().toList() }
        val totalCount = async { ops.query<Profile>().matching(query).count().awaitSingle() }

        PageImpl(content.await(), pageable, totalCount.await())
    }
}
