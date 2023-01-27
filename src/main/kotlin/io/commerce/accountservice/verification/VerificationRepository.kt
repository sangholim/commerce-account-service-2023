package io.commerce.accountservice.verification

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface VerificationRepository : CoroutineSortingRepository<Verification, ObjectId> {
    @Query(value = "{ item: ?0, key: ?1, expiredAt: { \$gt: ISODate() } }")
    suspend fun findByItemAndKey(item: VerificationItem, key: String): Verification?
}
