package io.commerce.accountservice.account.api

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.*
import io.commerce.accountservice.profile.Profile
import io.commerce.accountservice.profile.ProfileQueryService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.forExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * 통합 검색시 mongodb atlas 에서만 지원하는
 * $search operator를 사용하기 떄문에
 * 테스트 케이스에 따라 mock 처리
 */
@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class SearchProfilesIT(
    private val webTestClient: WebTestClient,
    @MockkBean
    private val profileQueryService: ProfileQueryService
) : BehaviorSpec({
    val tokenSubject = faker.random.nextUUID()
    val path = "/admin/account/profiles/search"

    fun getOpaqueToken(authority: String = SecurityConstants.SERVICE_ADMIN) =
        SecurityMockServerConfigurers.mockOpaqueToken()
            .authorities(SimpleGrantedAuthority(authority))
            .attributes { it["sub"] = tokenSubject }

    Given("관리자 프로필 통합 검색 API 인증 실패") {
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

    Given("관리자 프로필 통합 검색 API 인증 이후") {
        When("통합 검색 성공한 경우") {
            Then("status: 200 ok") {
                val criteria = adminSearchCriteria { query = "test" }
                val request = webTestClient
                    .mutateWith(getOpaqueToken())
                    .get()
                    .uri {
                        it.path(path)
                        it.queryParam("query", criteria.query)
                        it.build()
                    }

                coEvery { profileQueryService.searchAll(criteria) } returns emptyFlow()
                request.exchange().expectStatus().isOk
            }

            Then("이메일 'aaa@aaa.com' 검색시, 1개의 프로필 데이터가 존재한다.") {
                val profile = profile {
                    this.customerId = "customerId"
                    this.email = "aaa@aaa"
                    this.name = "홍길동"
                    this.phoneNumber = "01012341234"
                }
                val criteria = adminSearchCriteria { query = "aaa@aaa.com" }
                val request = webTestClient
                    .mutateWith(getOpaqueToken())
                    .get()
                    .uri {
                        it.path(path)
                        it.queryParam("query", criteria.query)
                        it.build()
                    }

                coEvery { profileQueryService.searchAll(criteria) } returns flowOf(profile)
                request.exchange().expectBodyList(Profile::class.java).returnResult().responseBody
                    .shouldNotBeNull().forExactly(1) {
                        it.email shouldBe profile.email
                    }
            }

            Then("고객 id 'customerId' 검색시, 1개의 프로필 데이터가 존재한다.") {
                val profile = profile {
                    this.customerId = "customerId"
                    this.email = "aaa@aaa"
                    this.name = "홍길동"
                    this.phoneNumber = "01012341234"
                }
                val criteria = adminSearchCriteria { query = "customerId" }
                val request = webTestClient
                    .mutateWith(getOpaqueToken())
                    .get()
                    .uri {
                        it.path(path)
                        it.queryParam("query", criteria.query)
                        it.build()
                    }

                coEvery { profileQueryService.searchAll(criteria) } returns flowOf(profile)
                request.exchange().expectBodyList(Profile::class.java).returnResult().responseBody
                    .shouldNotBeNull().forExactly(1) {
                        it.customerId shouldBe profile.customerId
                    }
            }

            Then("이름 '홍길동' 검색시, 1개의 프로필 데이터가 존재한다.") {
                val profile = profile {
                    this.customerId = "customerId"
                    this.email = "aaa@aaa"
                    this.name = "홍길동"
                    this.phoneNumber = "01012341234"
                }
                val criteria = adminSearchCriteria { query = "홍길동" }
                val request = webTestClient
                    .mutateWith(getOpaqueToken())
                    .get()
                    .uri {
                        it.path(path)
                        it.queryParam("query", criteria.query)
                        it.build()
                    }

                coEvery { profileQueryService.searchAll(criteria) } returns flowOf(profile)
                request.exchange().expectBodyList(Profile::class.java).returnResult().responseBody
                    .shouldNotBeNull().forExactly(1) {
                        it.name shouldBe profile.name
                    }
            }

            Then("휴대폰 번호 '01012341234' 검색시, 1개의 프로필 데이터가 존재한다.") {
                val profile = profile {
                    this.customerId = "customerId"
                    this.email = "aaa@aaa"
                    this.name = "홍길동"
                    this.phoneNumber = "01012341234"
                }
                val criteria = adminSearchCriteria { query = "01012341234" }
                val request = webTestClient
                    .mutateWith(getOpaqueToken())
                    .get()
                    .uri {
                        it.path(path)
                        it.queryParam("query", criteria.query)
                        it.build()
                    }

                coEvery { profileQueryService.searchAll(criteria) } returns flowOf(profile)
                request.exchange().expectBodyList(Profile::class.java).returnResult().responseBody
                    .shouldNotBeNull().forExactly(1) {
                        it.phoneNumber shouldBe profile.phoneNumber
                    }
            }
        }
    }
})
