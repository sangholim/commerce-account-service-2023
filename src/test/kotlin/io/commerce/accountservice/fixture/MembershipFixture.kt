package io.commerce.accountservice.fixture

import io.commerce.accountservice.membership.Membership
import io.commerce.accountservice.membership.MembershipStatus
import io.commerce.accountservice.membership.MembershipType
import org.bson.types.ObjectId

inline fun membership(block: MembershipFixtureBuilder.() -> Unit = {}) =
    MembershipFixtureBuilder().apply(block).build()

class MembershipFixtureBuilder {
    var id: ObjectId = ObjectId.get()
    var customerId: String = AccountFixture.id
    var type: MembershipType = MembershipType.MATE
    var creditRewardRate: Double = 0.0
    var status: MembershipStatus = MembershipStatus.ACTIVE

    fun build() = Membership(
        id, customerId, type, status, creditRewardRate
    )
}
