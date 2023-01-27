package io.commerce.accountservice.storeCreditAccount

import org.bson.types.ObjectId
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface StoreCreditAccountRepository : CoroutineCrudRepository<StoreCreditAccount, ObjectId> {
    suspend fun findByCustomerId(customerId: String): StoreCreditAccountView?
}
