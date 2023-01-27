package io.commerce.accountservice.membership

import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface MembershipRepository : CoroutineSortingRepository<Membership, ObjectId> {
    suspend fun findFirstByCustomerIdAndStatus(customerId: String, status: MembershipStatus): Membership?
}
