package io.commerce.accountservice.legacy.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.validation.ValidationMessages
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.Instant

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
@MockkBean(KeycloakUserService::class)
class LegacyActivationIT(
    private val keycloakUserService: KeycloakUserService,
    private val verificationRepository: VerificationRepository,
    private val profileRepository: ProfileRepository,
    private val webTestClient: WebTestClient
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
        .attributes { attrs ->
            attrs["sub"] = customerId
        }
    val client = webTestClient
        .mutateWith(opaqueToken)
        .put().uri("/account/legacy/activation/$customerId")
        .contentType(MediaType.APPLICATION_JSON)
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

    Given("v1 회원 계정 로그인 이후 프로필 업데이트") {
        When("인증되지 않은 요청인 경우") {
            Then("status: 401 Unauthorized") {
                webTestClient.put().uri("/account/legacy/activation/$customerId")
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 없는 경우") {
            Then("status: 403 Forbidden") {
                webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = "1234"
                            }
                    )
                    .put().uri("/account/legacy/activation/$customerId")
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("payload 유효성 검사 실패한 경우") {
            val invalidPayload = legacyActivateProfilePayload {
                this.email = " "
                this.name = " "
                this.phoneNumber = " "
                this.password = " "
                this.agreement = agreementPayload {
                    serviceTerm = false
                    privacyTerm = false
                }
            }

            Then("status: 400 Bad Request") {
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("다중 필드 에러") {
                client.bodyValue(invalidPayload).exchange()
                    .expectBody(ErrorResponse::class.java).returnResult()
                    .responseBody.shouldNotBeNull().fields
                    .forExactly(1) {
                        it.field shouldBe "email"
                        it.message shouldBe ValidationMessages.INVALID_FORMAT
                    }
                    .forExactly(1) {
                        it.field shouldBe "name"
                        it.message shouldBe ValidationMessages.INVALID_NAME
                    }
                    .forExactly(1) {
                        it.field shouldBe "phoneNumber"
                        it.message shouldBe ValidationMessages.INVALID_FORMAT
                    }.forExactly(1) {
                        it.field shouldBe "password"
                        it.message shouldBe ValidationMessages.INVALID_PASSWORD
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

        When("고객 ID 조회시 계정이 없는 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 404 Not Found") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNotFound
            }
        }

        When("aegis 계정 attributes 'requiredAction = MIGRATE_ACCOUNT' 이 없는 경우") {
            val invalidUser = userRepresentation {
                this.id = customerId
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        When("다른 계정의 이메일인 경우") {
            val invalidUser = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.createUser
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val invalidPayload = payload.copy(email = AccountFixture.existUser)

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 0
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 0
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "email_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("자신의 이메일이고 인증받지 않은 경우") {
            val invalidUser = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.loginUser
                this.emailVerified = false
                this.enabled = true
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val invalidPayload = payload.copy(email = invalidUser.email)

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 1
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 1
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "email_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("이메일 인증후, 인증 받지 않은 휴대폰 번호인 경우") {
            val invalidUser = userRepresentation {
                this.id = customerId
                this.username = AccountFixture.loginUser
                this.emailVerified = false
                this.enabled = true
                this.attributes = mapOf(
                    "requiredAction" to listOf("MIGRATE_ACCOUNT")
                )
            }
            val invalidPayload = payload.copy(
                email = invalidUser.username,
                phoneNumber = AccountFixture.newPhoneNumber
            )

            beforeTest {
                val verification = verification {
                    this.item = VerificationItem.ACTIVATION
                    this.key = invalidPayload.email
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
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 1
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns invalidUser
                coEvery { keycloakUserService.count(invalidPayload.email) } returns 1
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "phone_number_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("프로필 업데이트 정상 처리된 경우") {

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

            Then("status: 204 No Content") {
                val user = payload.toUserRepresentation().apply {
                    this.id = customerId
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.update(user) } returns Unit
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit

                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
            }

            Then("프로필이 존재한다") {
                val user = payload.toUserRepresentation().apply {
                    this.id = customerId
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.update(user) } returns Unit
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit

                client.bodyValue(payload)
                    .exchange()

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

            Then("인증 정보는 존재하지 않는다") {
                val user = payload.toUserRepresentation().apply {
                    this.id = customerId
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.count(payload.email) } returns 0
                coEvery { keycloakUserService.update(user) } returns Unit
                coEvery { keycloakUserService.addCustomerRoleToUser(user.id) } returns Unit

                client.bodyValue(payload)
                    .exchange()

                verificationRepository.count() shouldBe 0
            }
        }
    }
})
