package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import javax.validation.ConstraintViolationException

@DataMongoTest
@ProfileServiceTest
@EnableReactiveMongoAuditing
class ProfileServiceRegisterByTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val birthday = "1919-02-01"
    Given("suspend fun registerBy(userRepresentation: UserRepresentation): Profile") {
        When("프로필이 이미 존재하는 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.emailVerified = true
                this.enabled = true
                this.attributes = mapOf(
                    "name" to listOf(AccountFixture.name),
                    "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                    "phoneNumberVerified" to listOf("true"),
                    "emailAgreed" to listOf("true"),
                    "smsAgreed" to listOf("true"),
                    "serviceTermAgreed" to listOf("true"),
                    "privacyTermAgreed" to listOf("true")
                )
            }

            beforeTest {
                profileRepository.save(
                    profile {
                        this.customerId = customerId
                        enabled = true
                        email = AccountFixture.createUser
                        emailVerified = true
                        name = AccountFixture.name
                        phoneNumber = AccountFixture.newPhoneNumber
                        phoneNumberVerified = true
                        agreement = agreement {
                            email = true
                            sms = true
                            privacyTerm = true
                            serviceTerm = true
                        }
                    }
                )
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_ALREADY_EXIST)") {
                shouldThrow<ErrorCodeException> { profileService.registerBy(user) }
                    .should {
                        it.errorCode shouldBe ProfileError.PROFILE_ALREADY_EXIST.code
                        it.reason shouldBe ProfileError.PROFILE_ALREADY_EXIST.message
                    }
            }
        }

        When("프로필 필드 데이터가 올바르지 않은 경우") {
            val user = userRepresentation {
                this.id = " "
                this.username = "a"
                this.emailVerified = false
                this.attributes = mapOf(
                    "name" to listOf("1212"),
                    "phoneNumber" to listOf("a"),
                    "phoneNumberVerified" to listOf("false"),
                    "emailAgreed" to listOf("false"),
                    "smsAgreed" to listOf("false"),
                    "serviceTermAgreed" to listOf("false"),
                    "privacyTermAgreed" to listOf("false")
                )
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("필드 에러") {
                shouldThrow<ConstraintViolationException> { profileService.registerBy(user) }
                    .constraintViolations
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "customerId"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "name"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "email"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "emailVerified"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "phoneNumber"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "phoneNumberVerified"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "agreement.serviceTerm"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "agreement.privacyTerm"
                    }
            }
        }

        When("프로필 필수 필드가 저장된 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.emailVerified = true
                this.enabled = true
                this.attributes = mapOf(
                    "name" to listOf(AccountFixture.name),
                    "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                    "phoneNumberVerified" to listOf("true"),
                    "emailAgreed" to listOf("true"),
                    "smsAgreed" to listOf("true"),
                    "serviceTermAgreed" to listOf("true"),
                    "privacyTermAgreed" to listOf("true")
                )
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 데이터 생성") {
                profileService.registerBy(user).should {
                    it.id shouldNotBe null
                    it.customerId shouldBe customerId
                    it.enabled shouldBe true
                    it.email shouldBe AccountFixture.createUser
                    it.emailVerified shouldBe true
                    it.name shouldBe AccountFixture.name
                    it.phoneNumber shouldBe AccountFixture.newPhoneNumber
                    it.phoneNumberVerified shouldBe true
                    it.agreement.sms shouldBe true
                    it.agreement.email shouldBe true
                    it.agreement.serviceTerm shouldBe true
                    it.agreement.privacyTerm shouldBe true
                    it.orderCount shouldNotBe null
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }

        When("aegis 계정의 생일 필드가 올바르지 않은 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.emailVerified = true
                this.enabled = true
                this.attributes = mapOf(
                    "name" to listOf(AccountFixture.name),
                    "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                    "phoneNumberVerified" to listOf("true"),
                    "birthday" to listOf("1919-00-01"),
                    "emailAgreed" to listOf("true"),
                    "smsAgreed" to listOf("true"),
                    "serviceTermAgreed" to listOf("true"),
                    "privacyTermAgreed" to listOf("true")
                )
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 생일 필드는 존재하지 않는다") {
                profileService.registerBy(user).should {
                    it.id shouldNotBe null
                    it.customerId shouldBe customerId
                    it.birthday shouldBe null
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }

        When("프로필 모든 필드가 저장된 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.emailVerified = true
                this.enabled = true
                this.federatedIdentities = listOf(
                    federatedIdentityRepresentation {
                        this.identityProvider = "Kakao"
                        this.userId = "123123123"
                    },
                    federatedIdentityRepresentation {
                        this.identityProvider = "Naver"
                        this.userId = "123"
                    },
                    federatedIdentityRepresentation {
                        this.identityProvider = "Facebook"
                        this.userId = "1234"
                    }
                )
                this.attributes = mapOf(
                    "name" to listOf(AccountFixture.name),
                    "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                    "phoneNumberVerified" to listOf("true"),
                    "birthday" to listOf(birthday),
                    "emailAgreed" to listOf("true"),
                    "smsAgreed" to listOf("true"),
                    "serviceTermAgreed" to listOf("true"),
                    "privacyTermAgreed" to listOf("true")
                )
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 데이터 생성") {
                profileService.registerBy(user).should {
                    it.id shouldNotBe null
                    it.customerId shouldBe customerId
                    it.enabled shouldBe true
                    it.email shouldBe AccountFixture.createUser
                    it.emailVerified shouldBe true
                    it.name shouldBe AccountFixture.name
                    it.phoneNumber shouldBe AccountFixture.newPhoneNumber
                    it.phoneNumberVerified shouldBe true
                    it.birthday shouldNotBe null
                    it.identityProviders.shouldNotBeNull().size shouldBe 3
                    it.agreement.sms shouldBe true
                    it.agreement.email shouldBe true
                    it.agreement.serviceTerm shouldBe true
                    it.agreement.privacyTerm shouldBe true
                    it.orderCount shouldNotBe null
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
