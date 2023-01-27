package io.commerce.accountservice.account.accountFacadeService

import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.EmailDuplicateException
import io.commerce.accountservice.account.EmailNotVerifiedException
import io.commerce.accountservice.account.PhoneNumberNotVerifiedException
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.jboss.resteasy.core.ServerResponse
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.Instant

@DataMongoTest
@AccountFacadeServiceTest
@EnableReactiveMongoAuditing
class RegisterTest(
    private val accountFacadeService: AccountFacadeService,
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({

    Given("suspend fun register(payload: RegisterPayload)") {
        When("이미 가입된 이메일인 경우") {
            val payload = registerPayload {
                this.email = AccountFixture.existUser
            }

            afterTest {
                clearAllMocks()
                verificationRepository.deleteAll()
                profileRepository.deleteAll()
            }

            Then("throw EmailDuplicateException") {
                coEvery { keycloakUserService.count(payload.email) } returns 1
                shouldThrow<EmailDuplicateException> {
                    accountFacadeService.register(payload)
                }.should {
                    it.errorCode shouldBe "email_duplicated"
                    it.reason shouldBe "이미 가입된 이메일입니다"
                }
            }
        }

        When("인증 받지 않은 이메일인 경우") {
            val payload = registerPayload {
                this.email = AccountFixture.createUser
            }

            afterTest {
                clearAllMocks()
                verificationRepository.deleteAll()
                profileRepository.deleteAll()
            }

            Then("throw EmailNotVerifiedException") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                shouldThrow<EmailNotVerifiedException> {
                    accountFacadeService.register(payload)
                }.should {
                    it.errorCode shouldBe "email_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("이메일 인증후, 인증 받지 않은 휴대폰 번호인 경우") {
            val payload = registerPayload {
                this.email = AccountFixture.createUser
                this.phoneNumber = AccountFixture.newPhoneNumber
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.REGISTER
                    this.key = payload.email
                    this.code = VerificationFixture.SUCCESS_CODE
                    this.isVerified = true
                    this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                }
                verificationRepository.save(verification)
            }

            afterTest {
                clearAllMocks()
                verificationRepository.deleteAll()
                profileRepository.deleteAll()
            }

            Then("throw PhoneNumberNotVerifiedException") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                shouldThrow<PhoneNumberNotVerifiedException> {
                    accountFacadeService.register(payload)
                }.should {
                    it.errorCode shouldBe "phone_number_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("이메일 회원 가입 성공") {
            val customerId = faker.random.nextUUID()
            val payload = registerPayload {
                this.email = AccountFixture.createUser
                this.phoneNumber = AccountFixture.newPhoneNumber
                this.name = AccountFixture.name
                this.password = "test1234!"
                this.agreement = agreementPayload {
                    this.email = false
                    this.sms = false
                    this.privacyTerm = true
                    this.serviceTerm = true
                }
            }
            val user = userRepresentation {
                this.id = customerId
                this.username = payload.email
                this.password = payload.password
                this.emailVerified = true
                this.enabled = true
                this.attributes = mapOf(
                    "name" to listOf(payload.name),
                    "phoneNumber" to listOf(payload.phoneNumber),
                    "phoneNumberVerified" to listOf("true"),
                    "emailAgreed" to listOf(payload.agreement.email.toString()),
                    "smsAgreed" to listOf(payload.agreement.sms.toString()),
                    "serviceTermAgreed" to listOf(payload.agreement.serviceTerm.toString()),
                    "privacyTermAgreed" to listOf(payload.agreement.privacyTerm.toString())
                )
            }

            beforeTest {
                val verifications = listOf(
                    verification {
                        this.item = VerificationItem.REGISTER
                        this.key = payload.email
                        this.code = VerificationFixture.SUCCESS_CODE
                        this.isVerified = true
                        this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                    },
                    verification {
                        this.item = VerificationItem.REGISTER
                        this.key = payload.phoneNumber
                        this.code = VerificationFixture.SUCCESS_CODE
                        this.isVerified = true
                        this.expiredAt = Instant.now().plusSeconds(VerificationType.SMS.expiry.toLong())
                    }
                )
                verificationRepository.saveAll(verifications).collect()
            }

            afterTest {
                clearAllMocks()
                verificationRepository.deleteAll()
                profileRepository.deleteAll()
            }

            Then("프로필 데이터 조회 성공") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.findOneByUsername(payload.email) } returns user
                coEvery { keycloakUserService.create(any()) } returns ServerResponse()
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit
                accountFacadeService.register(payload)
                profileRepository.findAll().toList().forExactly(1) {
                    it.id shouldNotBe null
                    it.customerId shouldBe customerId
                    it.enabled shouldBe true
                    it.email shouldBe payload.email
                    it.emailVerified shouldBe true
                    it.name shouldBe payload.name
                    it.phoneNumber shouldBe payload.phoneNumber
                    it.phoneNumberVerified shouldBe true
                    it.birthday shouldBe null
                    it.identityProviders shouldBe null
                    it.agreement.sms shouldBe payload.agreement.sms
                    it.agreement.email shouldBe payload.agreement.email
                    it.agreement.serviceTerm shouldBe payload.agreement.serviceTerm
                    it.agreement.privacyTerm shouldBe payload.agreement.privacyTerm
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }

            Then("인증 데이터는 존재하지 않는다") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.findOneByUsername(payload.email) } returns user
                coEvery { keycloakUserService.create(any()) } returns ServerResponse()
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit
                accountFacadeService.register(payload)
                verificationRepository.count() shouldBe 0
            }
        }
    }
})
