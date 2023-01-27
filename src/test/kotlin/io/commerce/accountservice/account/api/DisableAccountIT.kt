package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.agreement
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.profile
import io.commerce.accountservice.keycloak.KeycloakAdminService
import io.commerce.accountservice.keycloak.KeycloakError
import io.commerce.accountservice.profile.IdentityProviderType
import io.commerce.accountservice.profile.ProfileError
import io.commerce.accountservice.profile.ProfileRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
@MockkBean(KeycloakAdminService::class)
class DisableAccountIT(
    private val webTestClient: WebTestClient,
    private val keycloakAdminService: KeycloakAdminService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val tokenSubject = faker.random.nextUUID()
    val path = "/admin/account/$tokenSubject/disable"

    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = tokenSubject }

    Given("관리자 회원 비활성화 API 인증 실패") {
        When("인증이 없는 경우") {
            val request = webTestClient
                .post()
                .uri(path)

            Then("401 Unauthorized") {
                request
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        When("권한이 'service-admin' 아닌 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken(SecurityConstants.CUSTOMER))
                .post()
                .uri(path)

            Then("403 Forbidden") {
                request
                    .exchange()
                    .expectStatus().isForbidden
            }
        }
    }

    Given("관리자 회원 비활성화 API 인증 이후") {
        When("aegis 회원이 없는 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken())
                .post()
                .uri(path)

            afterTest {
                profileRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } throws ErrorCodeException.of(KeycloakError.USER_NOT_FOUND)
                request.exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } throws ErrorCodeException.of(KeycloakError.USER_NOT_FOUND)
                request.exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe KeycloakError.USER_NOT_FOUND.code
                        it.message shouldBe KeycloakError.USER_NOT_FOUND.message
                    }
            }
        }

        When("Profile 이 없는 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken())
                .post()
                .uri(path)

            afterTest {
                profileRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } returns Unit
                request.exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } returns Unit
                request.exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe ProfileError.PROFILE_NOT_FOUND.code
                        it.message shouldBe ProfileError.PROFILE_NOT_FOUND.message
                    }
            }
        }

        When("비활성화 정상 처리된 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken())
                .post()
                .uri(path)

            beforeTest {
                val profile = profile {
                    this.customerId = tokenSubject
                    this.enabled = true
                    this.email = "test@test.com"
                    this.emailVerified = true
                    this.name = AccountFixture.name
                    this.phoneNumber = AccountFixture.normalPhoneNumber
                    this.phoneNumberVerified = true
                    this.identityProviders = listOf(IdentityProviderType.KAKAO)
                    this.agreement = agreement {
                        this.serviceTerm = true
                        this.privacyTerm = true
                    }
                    this.orderCount = 0
                }

                profileRepository.save(profile)
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("status: 204 No Content") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } returns Unit
                request.exchange()
                    .expectStatus().isNoContent
            }

            Then("프로필 '활성화 여부는' false 이다") {
                coEvery { keycloakAdminService.disableUser(tokenSubject) } returns Unit
                request.exchange()
                profileRepository.findByCustomerId(tokenSubject).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.customerId shouldBe tokenSubject
                    it.enabled shouldBe false
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
