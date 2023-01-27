package io.commerce.accountservice.fixture

import io.commerce.accountservice.storeCreditAccount.StoreCreditAccount
import org.bson.types.ObjectId

inline fun storeCreditAccount(block: StoreCreditAccountFixtureBuilder.() -> Unit = {}) =
    StoreCreditAccountFixtureBuilder().apply(block).build()

class StoreCreditAccountFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var customerId: String = "test"
    var balance: Int = 0
    var amountToExpire: Int = 0

    fun build() = StoreCreditAccount(
        id, customerId, balance, amountToExpire
    )
}
