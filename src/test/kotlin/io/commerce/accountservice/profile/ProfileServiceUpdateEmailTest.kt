package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.userRepresentation
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
class ProfileServiceUpdateEmailTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun updateEmail(userRepresentation: UserRepresentation): Profile") {
        When("변경 이메일이 이미 사용중인 경우") {
            val user = AccountFixture.createUser(customerId)

            beforeTest {
                profileRepository.save(Profile.of(user))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_EMAIL_EXIST)") {
                val invalid = user.apply {
                    this.id = faker.random.nextUUID()
                }
                shouldThrow<ErrorCodeException> {
                    profileService.updateEmail(invalid)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_EMAIL_EXIST.code
                    it.reason shouldBe ProfileError.PROFILE_EMAIL_EXIST.message
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
                    this.username = AccountFixture.createUser
                    this.emailVerified = true
                }
                shouldThrow<ErrorCodeException> {
                    profileService.updateEmail(user)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("aegis 회원 정보 이메일, 이메일 인증 여부 필드가 올바르지 않은 경우") {

            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                val invalid = userRepresentation {
                    this.id = customerId
                    this.username = "a"
                    this.emailVerified = false
                }

                shouldThrow<ConstraintViolationException> { profileService.updateEmail(invalid) }
                    .constraintViolations
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "email"
                    }
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "emailVerified"
                    }
            }
        }

        When("프로필 이메일 수정 성공한 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 이메일, 이메일 인증여부가 변경된다") {
                val user = userRepresentation {
                    this.id = customerId
                    this.username = AccountFixture.createUser
                    this.emailVerified = true
                }

                profileService.updateEmail(user)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.email shouldBe user.username
                        it.emailVerified shouldBe user.isEmailVerified
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }
    }
})
