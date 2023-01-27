package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.AgreementType
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.updateAgreementPayload
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
class UpdateAgreementTest(
    private val keycloakUserService: KeycloakUserService,
    private val accountFacadeService: AccountFacadeService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun updateAgreement(customerId: String, payload: UpdateAgreementPayload)") {
        When("회원이 존재하지 않는 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateAgreement(customerId, updateAgreementPayload())
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
                    accountFacadeService.updateAgreement(customerId, updateAgreementPayload())
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 마케팅 동의 항목 수정이 정상 처리된 경우") {

            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("프로필 마케팅 동의 항목 '이메일' 필드가 변경된다") {
                val payload = updateAgreementPayload {
                    type = AgreementType.EMAIL
                    active = true
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                accountFacadeService.updateAgreement(customerId, payload)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.agreement.email shouldBe payload.active
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }

            Then("프로필 마케팅 동의 항목 'sms' 필드가 변경된다") {
                val payload = updateAgreementPayload {
                    type = AgreementType.SMS
                    active = true
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                accountFacadeService.updateAgreement(customerId, payload)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.agreement.sms shouldBe payload.active
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
