package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
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
class ProfileServiceUpdateNameTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({

    val customerId = faker.random.nextUUID()
    val name = "변경이름"

    Given("suspend fun updateName(customerId: String, name: String): Profile") {

        When("프로필 필드 데이터가 올바르지 않은 경우") {

            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("이름 필드 에러") {
                val invalidName = " "
                shouldThrow<ConstraintViolationException> { profileService.updateName(customerId, invalidName) }
                    .constraintViolations
                    .forExactly(1) {
                        it.propertyPath.toString() shouldBe "name"
                    }
            }
        }

        When("프로필이 존재하지 않는 경우") {

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                shouldThrow<ErrorCodeException> {
                    profileService.updateName(customerId, name)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 이름 수정 성공한 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 데이터 이름 변경") {
                profileService.updateName(customerId, name).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.name shouldBe name
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
