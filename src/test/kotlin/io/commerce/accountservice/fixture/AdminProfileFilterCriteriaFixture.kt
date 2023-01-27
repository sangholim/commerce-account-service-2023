package io.commerce.accountservice.fixture

import io.commerce.accountservice.profile.AdminProfileFilterCriteria
import io.commerce.accountservice.profile.IdentityProviderType
import java.time.Instant

inline fun adminProfileFilterCriteria(block: AdminProfileFilterCriteriaBuilder.() -> Unit = {}) =
    AdminProfileFilterCriteriaBuilder().apply(block).build()

class AdminProfileFilterCriteriaBuilder {
    var createdAtRange: List<Instant> = listOf(Instant.now().minusSeconds(60), Instant.now().plusSeconds(60))
    var identityProviders: Set<IdentityProviderType> = emptySet()
    var agreement: AdminProfileFilterCriteria.AgreementFilterCriteria = AdminProfileFilterCriteria.AgreementFilterCriteria()
    var enabled: Boolean? = null
    var page: Int = 0

    fun build() = AdminProfileFilterCriteria(
        createdAtRange,
        identityProviders,
        agreement,
        enabled,
        page
    )
}
