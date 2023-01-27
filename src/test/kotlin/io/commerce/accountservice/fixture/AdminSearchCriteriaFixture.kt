package io.commerce.accountservice.fixture

import io.commerce.accountservice.profile.AdminSearchCriteria

inline fun adminSearchCriteria(block: AdminSearchCriteriaFixtureBuilder.() -> Unit = {}) =
    AdminSearchCriteriaFixtureBuilder().apply(block).build()

class AdminSearchCriteriaFixtureBuilder {
    var query: String = ""
    var page: Int = 0

    fun build() = AdminSearchCriteria(
        query,
        page
    )
}
