package io.commerce.accountservice.fixture

import io.commerce.accountservice.profile.IdentityProviderType
import io.commerce.accountservice.profile.Profile
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDate

inline fun profile(block: ProfileFixtureBuilder.() -> Unit = {}) =
    ProfileFixtureBuilder().apply(block).build()

class ProfileFixtureBuilder {
    var id: ObjectId? = null
    var customerId: String = ""
    var enabled: Boolean = true
    var email: String = ""
    var emailVerified: Boolean = false
    var name: String = ""
    var phoneNumber: String = ""
    var phoneNumberVerified: Boolean = false
    var identityProviders: List<IdentityProviderType>? = listOf()
    var birthday: LocalDate? = null
    var agreement: Profile.Agreement = agreement()
    var orderCount: Int = 0
    var createdAt: Instant? = null
    var updatedAt: Instant? = null

    fun build() = Profile(
        id, customerId, enabled, email, emailVerified, name, phoneNumber, phoneNumberVerified, identityProviders, birthday, agreement, orderCount, createdAt, updatedAt
    )
}
