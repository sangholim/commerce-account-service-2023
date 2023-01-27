package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.userRepresentation
import io.commerce.accountservice.keycloak.phoneNumber
import io.commerce.accountservice.keycloak.phoneNumberVerified
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
class ProfileServiceUpdatePhoneNumberTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun updatePhoneNumber(userRepresentation: UserRepresentation): Profile") {
        When("aegis 회원 정보 휴대폰 번호, 휴대폰 번호 인증 여부 필드가 올바르지 않은 경우") {

            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("휴대폰 번호, 휴대폰 번호 인증 여부 필드 에러") {
                val user = userRepresentation {
                    this.id = customerId
                    this.attributes = mapOf(
                        "phoneNumber" to listOf("a"),
                        "phoneNumberVerified" to listOf("false")
                    )
                }
                shouldThrow<ConstraintViolationException> { profileService.updatePhoneNumber(user) }
                    .constraintViolations
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "phoneNumber"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "phoneNumberVerified"
                    }
            }
        }

        When("프로필이 존재하지 않는 경우") {

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                val user = userRepresentation {
                    this.id = customerId
                    this.attributes = mapOf(
                        "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                        "phoneNumberVerified" to listOf("true")
                    )
                }
                shouldThrow<ErrorCodeException> {
                    profileService.updatePhoneNumber(user)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 휴대폰 번호 성공한 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 휴대폰 번호, 휴대폰 번호 인증여부가 변경된다") {
                val user = userRepresentation {
                    this.id = customerId
                    this.attributes = mapOf(
                        "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
                        "phoneNumberVerified" to listOf("true")
                    )
                }
                profileService.updatePhoneNumber(user)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.phoneNumber shouldBe user.phoneNumber
                        it.phoneNumberVerified shouldBe user.phoneNumberVerified.toBoolean()
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }
    }
})
