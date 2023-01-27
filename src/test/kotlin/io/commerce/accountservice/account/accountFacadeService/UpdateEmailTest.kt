package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.EmailDuplicateException
import io.commerce.accountservice.account.EmailNotVerifiedException
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.keycloak.updateEmail
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileError
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
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.Instant

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class UpdateEmailTest(
    private val accountFacadeService: AccountFacadeService,
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()

    Given("suspend fun updateEmail(customerId: String, payload: UpdateEmailPayload)") {
        When("사용중인 계정의 이메일인 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw EmailDuplicateException") {
                val invalid = updateEmailPayload {
                    email = AccountFixture.createUser
                }

                coEvery {
                    keycloakUserService.count(invalid.email)
                } returns 1
                shouldThrow<EmailDuplicateException> {
                    accountFacadeService.updateEmail(customerId, invalid)
                }.should {
                    it.errorCode shouldBe "email_duplicated"
                    it.reason shouldBe "이미 가입된 이메일입니다"
                }
            }
        }

        When("인증 받지 않은 이메일인 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw EmailNotVerifiedException") {
                val invalid = updateEmailPayload {
                    email = AccountFixture.createUser
                }

                coEvery {
                    keycloakUserService.count(invalid.email)
                } returns 0
                shouldThrow<EmailNotVerifiedException> {
                    accountFacadeService.updateEmail(customerId, invalid)
                }.should {
                    it.errorCode shouldBe "email_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("회원 ID로 조회시 계정이 존재하지 않는 경우") {
            val payload = updateEmailPayload {
                email = AccountFixture.createUser
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.email
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                }
                verificationRepository.save(verification)
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)") {
                coEvery {
                    keycloakUserService.count(payload.email)
                } returns 0

                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns null

                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateEmail(customerId, payload)
                }.should {
                    it.errorCode shouldBe AccountError.ACCOUNT_NOT_FOUND.code
                    it.reason shouldBe AccountError.ACCOUNT_NOT_FOUND.message
                }
            }
        }

        When("사용중인 프로필 이메일인 경우") {
            val payload = updateEmailPayload {
                email = AccountFixture.createUser
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.email
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                }
                verificationRepository.save(verification)
                val user = AccountFixture.createUser("test-customerId").updateEmail(payload.email)
                profileRepository.save(Profile.of(user))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_EMAIL_EXIST)") {
                coEvery {
                    keycloakUserService.count(payload.email)
                } returns 0

                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns AccountFixture.createUser(customerId)

                coEvery {
                    keycloakUserService.update(any())
                } returns Unit

                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateEmail(customerId, payload)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_EMAIL_EXIST.code
                    it.reason shouldBe ProfileError.PROFILE_EMAIL_EXIST.message
                }
            }
        }

        When("프로필이 존재하지 않는 경우") {
            val payload = updateEmailPayload {
                email = AccountFixture.createUser
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.email
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                }
                verificationRepository.save(verification)
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                coEvery {
                    keycloakUserService.count(payload.email)
                } returns 0

                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns AccountFixture.createUser(customerId)

                coEvery {
                    keycloakUserService.update(any())
                } returns Unit

                shouldThrow<ErrorCodeException> {
                    accountFacadeService.updateEmail(customerId, payload)
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필이 이메일 수정된 경우") {
            val payload = updateEmailPayload {
                email = AccountFixture.createUser
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.PROFILE
                    this.key = payload.email
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                }
                verificationRepository.save(verification)
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("프로필 이메일, 이메일 인증 여부 필드가 변경된다") {
                val user = AccountFixture.createUser(customerId).updateEmail(payload.email)

                coEvery {
                    keycloakUserService.count(payload.email)
                } returns 0

                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns AccountFixture.createUser(customerId)

                coEvery {
                    keycloakUserService.update(any())
                } returns Unit

                accountFacadeService.updateEmail(customerId, payload)
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.email shouldBe user.username
                    it.emailVerified shouldBe user.isEmailVerified
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }

            Then("인증 데이터가 제거된다") {
                coEvery {
                    keycloakUserService.count(payload.email)
                } returns 0

                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns AccountFixture.createUser(customerId)

                coEvery {
                    keycloakUserService.update(any())
                } returns Unit

                accountFacadeService.updateEmail(customerId, payload)
                verificationRepository.count() shouldBe 0
            }
        }
    }
})
