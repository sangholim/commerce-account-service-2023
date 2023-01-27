package io.commerce.accountservice.account.api

import io.commerce.accountservice.core.PagedView
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.profile.IdentityProviderType
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class AllFilteredProfilesIT(
    private val webTestClient: WebTestClient,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    val tokenSubject = faker.random.nextUUID()
    val path = "/admin/account/profiles"

    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = tokenSubject }

    Given("관리자 전체 프로필 조회 API 인증 실패") {
        When("인증이 없는 경우") {
            val request = webTestClient
                .get()
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
                .get()
                .uri(path)

            Then("403 Forbidden") {
                request
                    .exchange()
                    .expectStatus().isForbidden
            }
        }
    }

    Given("관리자 전체 프로필 조회 API 인증 이후") {
        When("모든 필터 조건이 존재하는 경우") {
            val request = webTestClient
                .mutateWith(getOpaqueToken())
                .get()
                .uri {
                    it.path(path)
                    it.queryParam(
                        "createdAtRange",
                        listOf(Instant.now().minusSeconds(10), Instant.now().plusSeconds(10))
                    )
                    it.queryParam("identityProviders", setOf(IdentityProviderType.KAKAO))
                    it.queryParam("enabled", true)
                    it.queryParam("agreement.sms", true)
                    it.queryParam("agreement.email", true)
                    it.build()
                }

            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.NAVER)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = false
                        this.email = "test2@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test3@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = false
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test4@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = false
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }
            Then("status: 200 Ok") {
                request.exchange().expectStatus().isOk
            }

            Then("페이지 번호는 0이다") {
                request.exchange()
                    .expectBody(object : ParameterizedTypeReference<PagedView<Profile>>() {})
                    .returnResult().responseBody.shouldNotBeNull()
                    .should {
                        it.page.number shouldBe 0
                    }
            }

            Then("활성화 필드 'true' 인 프로필 데이터 1개 존재한다") {
                request.exchange()
                    .expectBody(object : ParameterizedTypeReference<PagedView<Profile>>() {})
                    .returnResult().responseBody.shouldNotBeNull()
                    .content.forExactly(1) {
                        it.enabled shouldBe true
                    }
            }

            Then("email 수신 동의 필드 'true' 인 프로필 데이터 1개 존재한다") {
                request.exchange()
                    .expectBody(object : ParameterizedTypeReference<PagedView<Profile>>() {})
                    .returnResult().responseBody.shouldNotBeNull()
                    .content.forExactly(1) {
                        it.agreement.email shouldBe true
                    }
            }

            Then("sms 수신 동의 필드 'true' 인 프로필 데이터 1개 존재한다") {
                request.exchange()
                    .expectBody(object : ParameterizedTypeReference<PagedView<Profile>>() {})
                    .returnResult().responseBody.shouldNotBeNull()
                    .content.forExactly(1) {
                        it.agreement.sms shouldBe true
                    }
            }

            Then("'KAKAO' 로 연동된 프로필 데이터 1개 존재한다") {
                request.exchange()
                    .expectBody(object : ParameterizedTypeReference<PagedView<Profile>>() {})
                    .returnResult().responseBody.shouldNotBeNull()
                    .content.forExactly(1) {
                        it.identityProviders.shouldNotBeNull() shouldContain IdentityProviderType.KAKAO
                    }
            }
        }
    }
})
