package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.updateNamePayload
import io.commerce.accountservice.fixture.userRepresentation
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

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class UpdateNameTest(
    private val keycloakUserService: KeycloakUserService,
    private val accountFacadeService: AccountFacadeService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val payload = updateNamePayload {
        name = "변경이름"
    }
    val user = userRepresentation {
        this.id = customerId
        this.username = AccountFixture.createUser
        this.enabled = true
        this.emailVerified = true
        this.attributes = mapOf(
            "name" to listOf(payload.name),
            "phoneNumber" to listOf(AccountFixture.newPhoneNumber),
            "phoneNumberVerified" to listOf("true"),
            "emailAgreed" to listOf("false"),
            "smsAgreed" to listOf("false"),
            "serviceTermAgreed" to listOf("true"),
            "privacyTermAgreed" to listOf("true")
        )
    }

    Given("suspend fun updateName(customerId: String, payload: UpdateNamePayload)") {
        When("회원이 존재하지 않는 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateName(customerId, payload)
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
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateName(customerId, payload)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 이름 수정 정상 처리된 경우") {

            beforeTest {
                profileRepository.save(Profile.of(user))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("프로필 조회시 이름이 변경된다") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                accountFacadeService.updateName(customerId, payload)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.name shouldBe payload.name
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
