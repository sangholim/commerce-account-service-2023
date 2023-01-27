package io.commerce.accountservice.profile

import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.federatedIdentityRepresentation
import io.commerce.accountservice.keycloak.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@DataMongoTest
@ProfileServiceTest
@EnableReactiveMongoAuditing
class ProfileServiceUpsertIdentityProvidersTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun upsertIdentityProviders(userRepresentation: UserRepresentation): Profile") {
        When("프로필 데이터가 존재하지 않는 경우") {
            afterTest {
                profileRepository.deleteAll()
            }

            Then("새로운 프로필 데이터가 생성된다") {
                val user = AccountFixture.createUser(customerId)
                profileService.upsertIdentityProviders(user)
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
                        it.identityProviders.shouldNotBeNull().size shouldBe 3
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.agreement.serviceTerm shouldBe user.serviceTermAgreed.toBoolean()
                        it.agreement.privacyTerm shouldBe user.privacyTermAgreed.toBoolean()
                        it.orderCount shouldNotBe null
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }

        When("프로필 데이터가 있는 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 소셜 리스트만 변경된다") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.id = customerId
                    this.email = AccountFixture.loginUser.plus("a")
                    this.username = AccountFixture.loginUser.plus("a")
                    this.isEnabled = false
                    this.isEmailVerified = false
                    this.attributes = mapOf(
                        "name" to listOf(AccountFixture.name.plus("가")),
                        "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                        "phoneNumberVerified" to listOf("false"),
                        "birthday" to listOf("2001-01-01"),
                        "emailAgreed" to listOf("true"),
                        "smsAgreed" to listOf("true"),
                        "serviceTermAgreed" to listOf("false"),
                        "privacyTermAgreed" to listOf("false")
                    )

                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }

                profileService.upsertIdentityProviders(user)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.enabled shouldNotBe user.isEnabled
                        it.email shouldNotBe user.username
                        it.emailVerified shouldNotBe user.isEmailVerified
                        it.name shouldNotBe user.name
                        it.phoneNumber shouldNotBe user.phoneNumber
                        it.phoneNumberVerified shouldNotBe user.phoneNumberVerified.toBoolean()
                        it.birthday.shouldNotBeNull().toString() shouldNotBe user.birthday
                        it.identityProviders.shouldNotBeNull() shouldBe user.identityProviders
                        it.agreement.sms shouldNotBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldNotBe user.emailAgreed.toBoolean()
                        it.agreement.serviceTerm shouldNotBe user.serviceTermAgreed.toBoolean()
                        it.agreement.privacyTerm shouldNotBe user.privacyTermAgreed.toBoolean()
                        it.orderCount shouldNotBe null
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }
    }
})
