package io.commerce.accountservice.fixture

import io.commerce.accountservice.account.AgreementPayload
import io.commerce.accountservice.legacy.LegacyAccountLoginPayload
import io.commerce.accountservice.legacy.LegacyActivateProfilePayload
import org.keycloak.representations.idm.FederatedIdentityRepresentation
import org.keycloak.representations.idm.UserRepresentation

/**
 * v1 이관 유저 Fixture
 *
 */
object LegacyAccountFixture {
    private const val v1UserId = "13412341"
    const val id = "900b4b32-d65a-45fc-9626-e4ac2df10a77"
    const val email = "mockTransfer@test.com"
    const val phoneNumber = "00011111111"
    const val password = "dhalsgud12#"
    private const val passwordSha = "1899fc088514c707ce01193337f149861bb161f38903f8c7af0b967bda2305b5"
    private const val birthday = "1919-01-1"
    private const val image = "https://s3.ap-northeast-2.amazonaws.com/staticdev.commerce.co.kr/profile/basic.png"
    private const val name = "이관인"
    private const val testIdentityProviderType = "facebook"
    private const val testIdentityProviderId = "100776325936888"

    fun createLegacyUser() = UserRepresentation().apply {
        this.id = this@LegacyAccountFixture.id
        this.email = this@LegacyAccountFixture.email
        this.username = this.email
        this.isEnabled = true
        this.isEmailVerified = false
        this.attributes = mapOf(
            "requiredAction" to listOf("MIGRATE_ACCOUNT"),
            "name" to listOf(name),
            "phoneNumber" to listOf(phoneNumber),
            "phoneNumberVerified" to listOf("false"),
            "birthday" to listOf(birthday),
            "image" to listOf(image),
            "v1_user_id" to listOf(v1UserId),
            "v1_password" to listOf(passwordSha)
        )
        this.federatedIdentities = listOf(
            createFacebookIdentityProvider()
        )
    }

    fun createLegacyActivateProfilePayload(): LegacyActivateProfilePayload = LegacyActivateProfilePayload(
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        password = password,
        agreement = AgreementPayload(true, true, true, true)
    )

    fun createInvalidLegacyActivateProfilePayload(): LegacyActivateProfilePayload = LegacyActivateProfilePayload(
        email = "a",
        name = "a",
        phoneNumber = "a",
        password = "a",
        agreement = AgreementPayload(true, true, false, false)
    )

    /**
     * 페이스북 테스트 계정 연동
     */
    private fun createFacebookIdentityProvider() = FederatedIdentityRepresentation().apply {
        this.identityProvider = testIdentityProviderType
        this.userId = testIdentityProviderId
        this.userName = "test@test.com"
    }

    fun createLegacyAccountLoginPayload(): LegacyAccountLoginPayload =
        LegacyAccountLoginPayload(
            email,
            password
        )
}
