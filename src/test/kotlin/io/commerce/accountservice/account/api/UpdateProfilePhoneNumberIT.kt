package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.keycloak.phoneNumber
import io.commerce.accountservice.keycloak.phoneNumberVerified
import io.commerce.accountservice.keycloak.updatePhoneNumber
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileError
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
import kotlinx.coroutines.flow.count
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
class UpdateProfilePhoneNumberIT(
    private val keycloakUserService: KeycloakUserService,
    private val profileRepository: ProfileRepository,
    private val verificationRepository: VerificationRepository,
    private val webTestClient: WebTestClient
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
        .attributes { attrs ->
            attrs["sub"] = customerId
        }
    val uri = "/account/profile/$customerId/phone-number"
    val client = webTestClient
        .mutateWith(opaqueToken)
        .put().uri(uri)
        .contentType(MediaType.APPLICATION_JSON)
    val payload = updatePhoneNumberPayload {
        phoneNumber = AccountFixture.newPhoneNumber
    }

    Given("프로필 휴대폰 번호 수정") {
        When("인증되지 않은 요청인 경우") {
            Then("status: 401 Unauthorized") {
                webTestClient.put().uri(uri)
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
                    .put().uri(uri)
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("payload 유효성 검사 실패한 경우") {
            val invalidPayload = updatePhoneNumberPayload {
                this.phoneNumber = " "
            }

            Then("status: 400 Bad Request") {
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("휴대폰 번호 필드 에러") {
                client.bodyValue(invalidPayload).exchange()
                    .expectBody(ErrorResponse::class.java).returnResult()
                    .responseBody.shouldNotBeNull().fields
                    .forExactly(1) {
                        it.field shouldBe "phoneNumber"
                        it.message shouldBe ValidationMessages.INVALID_FORMAT
                    }
            }
        }

        When("수정할 휴대폰 번호 인증을 받지 않은 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                verificationRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe "phone_number_not_verified"
                        it.message shouldBe "인증을 완료해주세요"
                    }
            }
        }

        When("고객 ID 조회시 계정이 없는 경우") {

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

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe AccountError.ACCOUNT_NOT_FOUND.code
                        it.message shouldBe AccountError.ACCOUNT_NOT_FOUND.message
                    }
            }
        }

        When("고객 ID 조회시 프로필이 없는 경우") {

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

            Then("status: 400 Bad Request") {
                val user = userRepresentation {
                    this.id = customerId
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                val user = userRepresentation {
                    this.id = customerId
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe ProfileError.PROFILE_NOT_FOUND.code
                        it.message shouldBe ProfileError.PROFILE_NOT_FOUND.message
                    }
            }
        }

        When("프로필 휴대폰 번호가 수정된 경우") {

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

            Then("status: 204 No Content") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
            }

            Then("프로필의 휴대폰 번호, 휴대폰 번호 인증 여부 필드가 수정 된다") {
                val updatedUser = AccountFixture.createUser(customerId).updatePhoneNumber(payload.phoneNumber)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
                profileRepository.findByCustomerId(customerId).shouldNotBeNull().should {
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
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
                verificationRepository.findAll().count() shouldBe 0
            }
        }
    }
})
