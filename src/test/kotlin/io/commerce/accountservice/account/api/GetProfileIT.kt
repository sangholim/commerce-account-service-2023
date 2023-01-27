package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountError
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.keycloak.*
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileRepository
import io.commerce.accountservice.profile.ProfileView
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.kotest.core.spec.style.BehaviorSpec
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
import org.springframework.http.HttpStatus
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
@MockkBean(KeycloakUserService::class)
class GetProfileIT(
    private val keycloakUserService: KeycloakUserService,
    private val profileRepository: ProfileRepository,
    private val shippingAddressRepository: ShippingAddressRepository,
    private val webTestClient: WebTestClient
) : BehaviorSpec({
    val customerId = faker.random.nextUUID()
    val opaqueToken = SecurityMockServerConfigurers.mockOpaqueToken()
        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
        .attributes { attrs ->
            attrs["sub"] = customerId
        }
    val uri = "/account/profile/$customerId"
    val client = webTestClient
        .mutateWith(opaqueToken)
        .get().uri(uri)

    Given("프로필 조회하기") {
        When("인증되지 않은 요청인 경우") {
            Then("status: 401 Unauthorized") {
                webTestClient.get().uri(uri)
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
                    .get().uri(uri)
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        When("고객 ID 조회시 aegis 계정이 없는 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
            }

            Then("status: 400 Bad Request") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.exchange()
                    .expectStatus().isBadRequest
            }

            Then("error response") {
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns null
                client.exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.code shouldBe AccountError.ACCOUNT_NOT_FOUND.code
                        it.message shouldBe AccountError.ACCOUNT_NOT_FOUND.message
                    }
            }
        }

        When("고객 ID 조회한 회원 정보 필드가 유효하지 않고, 프로필이 없는 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                shippingAddressRepository.deleteAll()
            }

            Then("status: 500 Internal Server Error") {
                val user = userRepresentation {
                    this.id = customerId
                    this.username = "a"
                    this.emailVerified = false
                    this.attributes = mapOf(
                        "name" to listOf("1212"),
                        "phoneNumber" to listOf("a"),
                        "phoneNumberVerified" to listOf("false"),
                        "emailAgreed" to listOf("false"),
                        "smsAgreed" to listOf("false"),
                        "serviceTermAgreed" to listOf("false"),
                        "privacyTermAgreed" to listOf("false")
                    )
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectStatus().is5xxServerError
            }

            /*
                2023-01-06 16:39:35.248 ERROR 15548 --- [7 @coroutine#19] i.h.accountservice.ExceptionTranslator   : Unhandled exception

                javax.validation.ConstraintViolationException: upsertIdentityProviders.profile.agreement.privacyTerm: ....
             */
            Then("error: Internal Server Error, status: 500") {
                val user = userRepresentation {
                    this.id = customerId
                    this.username = "a"
                    this.emailVerified = false
                    this.attributes = mapOf(
                        "name" to listOf("1212"),
                        "phoneNumber" to listOf("a"),
                        "phoneNumberVerified" to listOf("false"),
                        "emailAgreed" to listOf("false"),
                        "smsAgreed" to listOf("false"),
                        "serviceTermAgreed" to listOf("false"),
                        "privacyTermAgreed" to listOf("false")
                    )
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectBody<ErrorResponse>()
                    .returnResult().responseBody.shouldNotBeNull().should {
                        it.status shouldBe HttpStatus.INTERNAL_SERVER_ERROR.value()
                        it.error shouldBe HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
                    }
            }
        }

        When("고객 ID 조회시 회원 정보 필드가 유효하고, 프로필이 없는 경우") {
            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                shippingAddressRepository.deleteAll()
            }

            Then("status: 200 Ok") {
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectStatus().isOk
            }

            Then("body: ProfileView") {
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectBody<ProfileView>()
                    .returnResult().responseBody
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }
            }

            Then("프로필이 새로 생성된다") {
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.enabled shouldBe true
                        it.email shouldBe user.username
                        it.emailVerified shouldBe user.isEmailVerified
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.phoneNumberVerified shouldBe user.phoneNumberVerified.toBoolean()
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.identityProviders shouldBe user.identityProviders
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.agreement.serviceTerm shouldBe user.serviceTermAgreed.toBoolean()
                        it.agreement.privacyTerm shouldBe user.privacyTermAgreed.toBoolean()
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }

        When("고객 ID 조회시 프로필은 존재하고, aegis 소셜 정보가 변경된 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                shippingAddressRepository.deleteAll()
            }

            Then("status: 200 Ok") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectStatus().isOk
            }

            Then("body: ProfileView") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectBody<ProfileView>()
                    .returnResult().responseBody
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }
            }

            Then("프로필이 소셜 리스트가 수정된다") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                profileRepository.findByCustomerId(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.id shouldNotBe null
                        it.customerId shouldBe user.id
                        it.identityProviders shouldBe user.identityProviders
                        it.createdAt shouldNotBe null
                        it.updatedAt shouldNotBe null
                    }
            }
        }

        When("고객 ID 조회시 프로필은 존재하고, 배송지가 있는 경우") {
            beforeTest {
                profileRepository.save(Profile.of(AccountFixture.createUser(customerId)))
                shippingAddressRepository.save(
                    shippingAddress {
                        this.customerId = customerId
                        this.name = "김배송"
                        this.recipient = "김수령"
                        this.primaryPhoneNumber = AccountFixture.newPhoneNumber
                        this.secondaryPhoneNumber = AccountFixture.normalPhoneNumber
                        this.zipCode = "00100"
                        this.line1 = "기본 주소"
                        this.line2 = "주소상세"
                        this.primary = true
                    }
                )
            }

            afterTest {
                clearAllMocks()
                profileRepository.deleteAll()
                shippingAddressRepository.deleteAll()
            }

            Then("status: 200 Ok") {
                val user = AccountFixture.createUser(customerId).apply {
                    this.federatedIdentities = listOf(
                        federatedIdentityRepresentation {
                            this.identityProvider = "Kakao"
                            this.userId = "123333"
                        }
                    )
                }

                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns user
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectStatus().isOk
            }

            Then("body: ProfileView") {
                val user = AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.findOneByCustomerId(customerId) } returns AccountFixture.createUser(customerId)
                coEvery { keycloakUserService.update(any()) } returns Unit
                client.exchange()
                    .expectBody<ProfileView>()
                    .returnResult().responseBody
                    .shouldNotBeNull()
                    .should {
                        it.email shouldBe user.username
                        it.name shouldBe user.name
                        it.phoneNumber shouldBe user.phoneNumber
                        it.birthday.shouldNotBeNull().toString() shouldBe user.birthday
                        it.agreement.sms shouldBe user.smsAgreed.toBoolean()
                        it.agreement.email shouldBe user.emailAgreed.toBoolean()
                        it.identityProviders shouldBe user.identityProviders
                        it.shippingAddresses.shouldNotBeNull().size shouldBe 1
                        it.emailAgreed shouldBe it.agreement.email
                        it.smsAgreed shouldBe it.agreement.sms
                    }
            }
        }
    }
})
