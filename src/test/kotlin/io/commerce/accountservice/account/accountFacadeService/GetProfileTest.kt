package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.federatedIdentityRepresentation
import io.commerce.accountservice.fixture.shippingAddress
import io.commerce.accountservice.keycloak.*
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class GetProfileTest(
    private val keycloakUserService: KeycloakUserService,
    private val accountFacadeService: AccountFacadeService,
    private val shippingAddressRepository: ShippingAddressRepository,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun getProfile(customerId: String): AccountDetailView") {
        When("aegis 회원이 존재하지 않는 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.getProfile(customerId)
                }.should {
                    it.errorCode shouldBe AccountError.ACCOUNT_NOT_FOUND.code
                    it.reason shouldBe AccountError.ACCOUNT_NOT_FOUND.message
                }
            }
        }

        When("프로필이 존재하지 않는 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("새로운 프로필 데이터 생성후, AccountDetailView 객체가 반환된다") {
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                accountFacadeService.getProfile(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }

                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.enabled shouldBe true
                        it.email shouldBe user.username
                        it.emailVerified shouldBe user.isEmailVerified
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.phoneNumberVerified shouldBe user.phoneNumberVerified.toBoolean()
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.identityProviders shouldBe user.identityProviders
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.agreement.serviceTerm shouldBe user.serviceTermAgreed.toBoolean()
                        it.agreement.privacyTerm shouldBe user.privacyTermAgreed.toBoolean()
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }

        When("프로필이 존재하는 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("프로필 소셜 리스트 변경후, AccountDetailView 객체가 반환된다") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                accountFacadeService.getProfile(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }

                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.identityProviders shouldBe user.identityProviders
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }

        When("배송지가 존재하는 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
                shippingAddressRepository.save(
                    shippingAddress {
                        this.customerId = customerId
                        this.name = "김배송"
                        this.recipient = "김수령"
                        this.primaryPhoneNumber = AccountFixture.newPhoneNumber
                        this.secondaryPhoneNumber = AccountFixture.normalPhoneNumber
                        this.zipCode = "00100"
                        this.line1 = "기본 주소"
                        this.line2 = "주소상세"
                        this.primary = true
                    }
                )
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("AccountDetailView 객체내 배송지 정보가 존재한다") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                accountFacadeService.getProfile(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.shippingAddresses.shouldNotBeNull().size shouldBe 1
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }

                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.identityProviders shouldBe user.identityProviders
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }
    }
})
