package io.commerce.accountservice.legacy.legacyAccountFacadeService

import io.commerce.accountservice.account.EmailDuplicateException
import io.commerce.accountservice.account.EmailNotVerifiedException
import io.commerce.accountservice.account.PhoneNumberNotVerifiedException
import io.commerce.accountservice.core.NotFoundException
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.legacy.LegacyAccountFacadeService
import io.commerce.accountservice.legacy.LegacyBadRequestException
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.http.HttpStatus
import java.time.Instant

@DataMongoTest
@LegacyAccountFacadeServiceTest
@EnableReactiveMongoAuditing
class UpdateProfileTest(
    private val legacyAccountFacadeService: LegacyAccountFacadeService,
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    Given("suspend fun updateProfile(customerId: String, payload: LegacyActivateProfilePayload)") {
        val customerId = faker.random.nextUUID()

        When("회원 ID로 조회시 계정이 없는 경우") {
            val payload = legacyActivateProfilePayload()

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw NotFoundException()") {
                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns null

                shouldThrow<NotFoundException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.status shouldBe HttpStatus.NOT_FOUND
                }
            }
        }

        When("aegis 계정 attributes 'requiredAction = MIGRATE_ACCOUNT' 이 없는 경우") {
            val payload = legacyActivateProfilePayload()
            val legacyUser = userRepresentation {
                this.id = customerId
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw LegacyBadRequestException()") {
                coEvery {
                    keycloakUserService.findOneByCustomerId(customerId)
                } returns legacyUser

                shouldThrow<LegacyBadRequestException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.status shouldBe HttpStatus.BAD_REQUEST
                }
            }
        }

        When("다른 계정의 이메일인 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val payload = legacyActivateProfilePayload {
                this.email = AccountFixture.existUser
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw EmailDuplicateException()") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 1
                shouldThrow<EmailDuplicateException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.errorCode shouldBe "email_duplicated"
                    it.reason shouldBe "이미 가입된 이메일입니다"
                }
            }
        }

        When("중복되지 않은 이메일이 인증 받지 않은 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val payload = legacyActivateProfilePayload {
                this.email = AccountFixture.existUser
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw EmailNotVerifiedException()") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                shouldThrow<EmailNotVerifiedException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.errorCode shouldBe "email_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("자신의 이메일이고 인증받지 않은 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.loginUser
                this.emailVerified = false
                this.enabled = true
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val payload = legacyActivateProfilePayload {
                this.email = user.email
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("throw EmailNotVerifiedException()") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 1
                shouldThrow<EmailNotVerifiedException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.errorCode shouldBe "email_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("이메일 인증후, 인증 받지 않은 휴대폰 번호인 경우") {
            val user = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.loginUser
                this.emailVerified = false
                this.enabled = true
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val payload = legacyActivateProfilePayload {
                this.email = user.email
                this.phoneNumber = AccountFixture.newPhoneNumber
            }

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.ACTIVATION
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

            Then("throw PhoneNumberNotVerifiedException()") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0

                shouldThrow<PhoneNumberNotVerifiedException> {
                    legacyAccountFacadeService.updateProfile(customerId, payload)
                }.should {
                    it.errorCode shouldBe "phone_number_not_verified"
                    it.reason shouldBe "인증을 완료해주세요"
                }
            }
        }

        When("프로필 업데이트 정상 처리된 경우") {
            val payload = legacyActivateProfilePayload {
                this.email = AccountFixture.createUser
                this.phoneNumber = AccountFixture.normalPhoneNumber
                this.name = AccountFixture.name
                this.password = AccountFixture.password
                this.agreement = agreementPayload {
                    this.email = false
                    this.sms = false
                    this.privacyTerm = true
                    this.serviceTerm = true
                }
            }

            beforeTest {
                val verifications = listOf(
                    verification {
                        this.item = VerificationItem.ACTIVATION
                        this.key = payload.email
                        this.code = VerificationFixture.SUCCESS_CODE
                        this.isVerified = true
                        this.expiredAt = Instant.now().plusSeconds(VerificationType.EMAIL.expiry.toLong())
                    },
                    verification {
                        this.item = VerificationItem.ACTIVATION
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
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("프로필이 존재한다") {
                val user = payload.toUserRepresentation().apply {
                    this.id = customerId
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.update(user) } returns Unit
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit

                legacyAccountFacadeService.updateProfile(customerId, payload)

                profileRepository.findAll().toList().forExactly(1) {
                    it.id shouldNotBe null
                    it.customerId shouldNotBe null
                    it.enabled shouldBe true
                    it.email shouldBe payload.email
                    it.emailVerified shouldBe true
                    it.name shouldBe payload.name
                    it.phoneNumber shouldBe payload.phoneNumber
                    it.phoneNumberVerified shouldBe true
                    it.birthday shouldBe null
                    it.identityProviders.shouldNotBeNull().count() shouldBeGreaterThan 0
                    it.agreement.sms shouldBe payload.agreement.sms
                    it.agreement.email shouldBe payload.agreement.email
                    it.agreement.serviceTerm shouldBe payload.agreement.serviceTerm
                    it.agreement.privacyTerm shouldBe payload.agreement.privacyTerm
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }

            Then("인증 데이터는 존재하지 않는다") {
                val user = payload.toUserRepresentation().apply {
                    this.id = customerId
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.update(user) } returns Unit
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit

                legacyAccountFacadeService.updateProfile(customerId, payload)

                verificationRepository.count() shouldBe 0
            }
        }
    }
})
