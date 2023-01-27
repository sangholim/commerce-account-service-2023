package io.commerce.accountservice.fixture

import io.commerce.accountservice.membership.MembershipType
import io.commerce.accountservice.membership.MembershipView

inline fun membershipView(block: MembershipViewFixtureBuilder.() -> Unit = {}) =
    MembershipViewFixtureBuilder().apply(block).build()

class MembershipViewFixtureBuilder {
    var name: String = MembershipType.MATE.label
    var creditRewardRate: Double = 0.0
    fun build() = MembershipView(
        name,
        creditRewardRate
    )
}
