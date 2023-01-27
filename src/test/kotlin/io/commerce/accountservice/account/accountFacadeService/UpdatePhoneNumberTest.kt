package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.PhoneNumberNotVerifiedException
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.keycloak.phoneNumber
import io.commerce.accountservice.keycloak.phoneNumberVerified
import io.commerce.accountservice.keycloak.updatePhoneNumber
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import kotlinx.coroutines.flow.count
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.Instant

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class UpdatePhoneNumberTest(
    private val accountFacadeService: AccountFacadeService,
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val payload = updatePhoneNumberPayload {
        phoneNumber = AccountFixture.newPhoneNumber
    }
    Given("suspend fun updatePhoneNumber(customerId: String, payload: UpdatePhoneNumberPayload)") {
        When("수정할 휴대폰 번호 인증을 받지 않은 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw PhoneNumberNotVerifiedException") {
                shouldThrow<PhoneNumberNotVerifiedException> {
                    accountFacadeService.updatePhoneNumber(customerId, payload)
                }.should {
                    it.errorCode shouldBe "phone_number_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("회원이 존재하지 않는 경우") {

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.phoneNumber
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.SMS.expiry.toLong())
                }
                verificationRepository.save(verification)
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updatePhoneNumber(customerId, payload)
                }.should {
                    it.errorCode shouldBe AccountError.ACCOUNT_NOT_FOUND.code
                    it.reason shouldBe AccountError.ACCOUNT_NOT_FOUND.message
                }
            }
        }

        When("휴대폰 번호 수정 성공한 경우") {

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.phoneNumber
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.SMS.expiry.toLong())
                }
                verificationRepository.save(verification)
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("프로필의 휴대폰 번호, 휴대폰 번호 인증 여부 필드가 수정 된다") {
                val updatedUser = AccountFixture.createUser(customerId).updatePhoneNumber(payload.phoneNumber)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                accountFacadeService.updatePhoneNumber(customerId, payload)
                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.phoneNumber shouldBe updatedUser.phoneNumber
                        it.phoneNumberVerified shouldBe updatedUser.phoneNumberVerified.toBoolean()
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }

            Then("인증 데이터는 제거된다") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                accountFacadeService.updatePhoneNumber(customerId, payload)
                verificationRepository.findAll().count() shouldBe 0
            }
        }
    }
})
