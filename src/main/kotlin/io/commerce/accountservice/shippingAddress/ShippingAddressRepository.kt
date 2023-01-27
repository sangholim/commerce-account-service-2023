package io.commerce.accountservice.shippingAddress

import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

interface ShippingAddressRepository : CoroutineSortingRepository<ShippingAddress, ObjectId> {

    @Query(value = "{customerId: ?0}", sort = "{primary: -1, createdAt: -1}")
    fun findByCustomerId(customerId: String): Flow<ShippingAddress>

    suspend fun findFirstByCustomerIdAndPrimary(customerId: String, primary: Boolean): ShippingAddress?
}
