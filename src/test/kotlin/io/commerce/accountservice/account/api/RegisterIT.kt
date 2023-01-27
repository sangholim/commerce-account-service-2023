package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.validation.ValidationMessages
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import org.jboss.resteasy.core.ServerResponse
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
@MockkBean(KeycloakUserService::class)
class RegisterIT(
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository,
    private val webTestClient: WebTestClient
) : BehaviorSpec({
    Given("이메일 회원 가입") {
        val client = webTestClient
            .post().uri("/account/register")
            .contentType(MediaType.APPLICATION_JSON)
        val payload = registerPayload {
            email = AccountFixture.createUser
            password = AccountFixture.password
            name = AccountFixture.name
            phoneNumber = AccountFixture.newPhoneNumber
            agreement = agreementPayload {
                this.privacyTerm = true
                this.serviceTerm = true
            }
        }
        When("필드 유효성 검사 실패한 경우") {
            val invalid = registerPayload {
                email = " "
                password = " "
                name = " "
                phoneNumber = " "
                agreement = agreementPayload {
                    this.privacyTerm = false
                    this.serviceTerm = false
                }
            }

            afterTest {
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                client.bodyValue(invalid).exchange()
                    .expectStatus().isBadRequest
            }

            Then("다중 필드 에러") {
                client.bodyValue(invalid).exchange()
                    .expectBody(ErrorResponse::class.java).returnResult()
                    .responseBody.shouldNotBeNull().fields
                    .forExactly(1) {
                        it.field shouldBe "email"
                        it.message shouldBe ValidationMessages.INVALID_FORMAT
                    }
                    .forExactly(1) {
                        it.field shouldBe "password"
                        it.message shouldBe ValidationMessages.INVALID_PASSWORD
                    }
                    .forExactly(1) {
                        it.field shouldBe "name"
                        it.message shouldBe ValidationMessages.INVALID_NAME
                    }
                    .forExactly(1) {
                        it.field shouldBe "phoneNumber"
                        it.message shouldBe ValidationMessages.INVALID_FORMAT
                    }
                    .forExactly(1) {
                        it.field shouldBe "agreement.privacyTerm"
                        it.message shouldBe ValidationMessages.REQUIRED_AGREEMENT
                    }
                    .forExactly(1) {
                        it.field shouldBe "agreement.serviceTerm"
                        it.message shouldBe ValidationMessages.REQUIRED_AGREEMENT
                    }
            }
        }

        When("다른 계정의 이메일인 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.count(payload.email) } returns 1
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.count(payload.email) } returns 1
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "email_duplicated"
                        it.message shouldBe "이미 가입된 이메일입니다"
                    }
            }
        }

        When("인증 받지 않은 이메일인 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>().returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "email_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("이메일는 인증 받고, 휴대폰 번호는 인증 받지 않은 경우") {

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
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "phone_number_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("회원 가입 완료한 경우") {
            val customerId = faker.random.nextUUID()
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

            Then("status: 201 Created") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.findOneByUsername(payload.email) } returns user
                coEvery { keycloakUserService.create(any()) } returns ServerResponse()
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isCreated
            }

            Then("프로필 데이터 생성된다") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.findOneByUsername(payload.email) } returns user
                coEvery { keycloakUserService.create(any()) } returns ServerResponse()
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isCreated

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
                    it.identityProviders shouldBe null
                    it.agreement.sms shouldBe payload.agreement.sms
                    it.agreement.email shouldBe payload.agreement.email
                    it.agreement.serviceTerm shouldBe payload.agreement.serviceTerm
                    it.agreement.privacyTerm shouldBe payload.agreement.privacyTerm
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }

            Then("인증 정보는 제거된다") {
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.findOneByUsername(payload.email) } returns user
                coEvery { keycloakUserService.create(any()) } returns ServerResponse()
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isCreated

                verificationRepository.count() shouldBe 0
            }
        }
    }
})
