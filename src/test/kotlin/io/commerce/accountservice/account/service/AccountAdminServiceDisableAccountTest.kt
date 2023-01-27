package io.commerce.accountservice.account.service

import io.commerce.accountservice.account.AccountAdminService
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.agreement
import io.commerce.accountservice.fixture.profile
import io.commerce.accountservice.keycloak.KeycloakAdminService
import io.commerce.accountservice.keycloak.KeycloakError
import io.commerce.accountservice.profile.IdentityProviderType
import io.commerce.accountservice.profile.ProfileError
import io.commerce.accountservice.profile.ProfileRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@DataMongoTest
@AccountAdminServiceTest
@EnableReactiveMongoAuditing
class AccountAdminServiceDisableAccountTest(
    private val accountAdminService: AccountAdminService,
    private val keycloakAdminService: KeycloakAdminService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun disableAccount(customerId: String)") {
        When("aegis 회원이 없는 경우") {
            afterTest {
                profileRepository.deleteAll()
            }
            Then("throw ErrorCodeException.of(KeycloakError.USER_NOT_FOUND)") {
                coEvery {
                    keycloakAdminService.disableUser(customerId)
                } throws ErrorCodeException.of(KeycloakError.USER_NOT_FOUND)

                shouldThrow<ErrorCodeException> {
                    accountAdminService.disableAccount(customerId)
                }.should {
                    it.errorCode shouldBe KeycloakError.USER_NOT_FOUND.code
                    it.reason shouldBe KeycloakError.USER_NOT_FOUND.message
                }
            }
        }
        When("프로필이 없는 경우") {
            afterTest {
                profileRepository.deleteAll()
            }
            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                coEvery {
                    keycloakAdminService.disableUser(customerId)
                } returns Unit

                shouldThrow<ErrorCodeException> {
                    accountAdminService.disableAccount(customerId)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }
        When("비활성화 정상 처리된 경우") {
            beforeTest {
                val profile = profile {
                    this.customerId = customerId
                    this.enabled = true
                    this.email = "test@test.com"
                    this.emailVerified = true
                    this.name = AccountFixture.name
                    this.phoneNumber = AccountFixture.normalPhoneNumber
                    this.phoneNumberVerified = true
                    this.identityProviders = listOf(IdentityProviderType.KAKAO)
                    this.agreement = agreement {
                        this.serviceTerm = true
                        this.privacyTerm = true
                    }
                    this.orderCount = 0
                }

                profileRepository.save(profile)
            }
            afterTest {
                profileRepository.deleteAll()
            }
            Then("프로필 '활성화 여부는' false 이다") {
                coEvery {
                    keycloakAdminService.disableUser(customerId)
                } returns Unit

                accountAdminService.disableAccount(customerId)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.customerId shouldBe customerId
                    it.enabled shouldBe false
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
