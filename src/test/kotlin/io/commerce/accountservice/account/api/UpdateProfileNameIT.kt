package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.updateNamePayload
import io.commerce.accountservice.fixture.userRepresentation
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileError
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.validation.ValidationMessages
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
@MockkBean(KeycloakUserService::class)
class UpdateProfileNameIT(
    private val keycloakUserService: KeycloakUserService,
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
        .put().uri("/account/profile/$customerId/name")
        .contentType(MediaType.APPLICATION_JSON)
    val payload = updateNamePayload {
        name = "이름변경"
    }

    Given("프로필 이름 수정") {
        When("인증되지 않은 요청인 경우") {
            Then("status: 401 Unauthorized") {
                webTestClient.put().uri("/account/profile/$customerId/name")
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
                    .put().uri("/account/profile/$customerId/name")
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("payload 유효성 검사 실패한 경우") {
            val invalidPayload = updateNamePayload {
                this.name = " "
            }

            Then("status: 400 Bad Request") {
                client.bodyValue(invalidPayload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            Then("이름 필드 에러") {
                client.bodyValue(invalidPayload).exchange()
                    .expectBody(ErrorResponse::class.java).returnResult()
                    .responseBody.shouldNotBeNull().fields
                    .forExactly(1) {
                        it.field shouldBe "name"
                        it.message shouldBe ValidationMessages.INVALID_NAME
                    }
            }
        }

        When("고객 ID 조회시 계정이 없는 경우") {

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
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

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
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

        When("프로필 이름 수정된 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("status: 204 No Content") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
            }

            Then("프로필 이름 필드가 변경된다") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
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
