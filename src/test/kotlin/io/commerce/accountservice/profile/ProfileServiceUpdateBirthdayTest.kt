package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.userRepresentation
import io.commerce.accountservice.keycloak.birthday
import io.commerce.accountservice.keycloak.updateBirthday
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.format.DateTimeParseException

@DataMongoTest
@ProfileServiceTest
@EnableReactiveMongoAuditing
class ProfileServiceUpdateBirthdayTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    Given("suspend fun updateBirthday(userRepresentation: UserRepresentation): Profile") {
        val customerId = faker.random.nextUUID()
        When("생일 필드가 없는 경우") {

            beforeTest {
                profileRepository.save(Profile.of(io.commerce.accountservice.fixture.AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw NullPointerException") {
                val invalidUser = userRepresentation { this.id = customerId }
                shouldThrow<NullPointerException> { profileService.updateBirthday(invalidUser) }
                    .should {
                        it.message shouldBe "text"
                    }
            }
        }

        When("생일 필드가 유효하지 않음") {

            beforeTest {
                profileRepository.save(Profile.of(io.commerce.accountservice.fixture.AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw DateTimeParseException") {
                val invalidUser = io.commerce.accountservice.fixture.AccountFixture.createUser(customerId).updateBirthday("a")
                shouldThrow<DateTimeParseException> { profileService.updateBirthday(invalidUser) }
                    .should {
                        it.parsedString shouldBe invalidUser.birthday
                    }
            }
        }

        When("프로필이 존재하지 않는 경우") {

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                shouldThrow<ErrorCodeException> {
                    profileService.updateBirthday(io.commerce.accountservice.fixture.AccountFixture.createUser(customerId))
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 생일 수정 성공한 경우") {
            beforeTest {
                profileRepository.save(Profile.of(io.commerce.accountservice.fixture.AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 생일 필드 변경") {
                val user = io.commerce.accountservice.fixture.AccountFixture.createUser(customerId).updateBirthday("1999-01-01")
                profileService.updateBirthday(user).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
