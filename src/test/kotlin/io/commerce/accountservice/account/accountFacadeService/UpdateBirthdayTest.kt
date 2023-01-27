package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.updateBirthdayPayload
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileError
import io.commerce.accountservice.profile.ProfileRepository
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
import java.time.format.DateTimeParseException

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class UpdateBirthdayTest(
    private val accountFacadeService: AccountFacadeService,
    private val keycloakUserService: KeycloakUserService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    Given("suspend fun updateBirthday(customerId: String, payload: UpdateBirthdayPayload)") {
        When("회원이 존재하지 않는 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateBirthday(customerId, updateBirthdayPayload())
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

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateBirthday(customerId, updateBirthdayPayload())
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("생일 필드가 유효하지 않우") {

            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("throw DateTimeParseException") {
                val invalid = updateBirthdayPayload {
                    birthday = " "
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                shouldThrow<DateTimeParseException> {
                    accountFacadeService.updateBirthday(customerId, invalid)
                }.should {
                    it.parsedString shouldBe invalid.birthday
                }
            }
        }

        When("프로필 생일 수정 성공한 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 생일 변경") {
                val payload = updateBirthdayPayload {
                    birthday = "1999-01-01"
                }
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit

                accountFacadeService.updateBirthday(user.id, payload)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.birthday.shouldNotBeNull().toString() shouldBe payload.birthday
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
