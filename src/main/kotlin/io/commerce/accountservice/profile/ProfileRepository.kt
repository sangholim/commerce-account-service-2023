package io.commerce.accountservice.profile

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProfileRepository : CoroutineCrudRepository<Profile, ObjectId>, ProfileSyncRepository,
    ProfilePagingRepository {
    suspend fun existsByCustomerId(customerId: String): Boolean

    suspend fun existsByEmail(email: String): Boolean

    suspend fun findByCustomerId(customerId: String): Profile?

    @Aggregation(
        """
            {
              ${'$'}search: {
                index: 'search-profiles',
                compound: {
                  must: [{
                    text: {
                      query: ?0,
                      path: {
                        wildcard: '*'
                      }
                    }
                  }]
                }              
              }
            }
        """
    )
    fun searchAll(query: String, pageRequest: Pageable): Flow<Profile>
}
