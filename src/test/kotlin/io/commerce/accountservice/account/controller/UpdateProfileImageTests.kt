package io.commerce.accountservice.account.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.UpdateProfileImagePayload
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
class UpdateProfileImageTests(
    @MockkBean
    private val accountFacadeService: AccountFacadeService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    describe("마이페이지 이미지 경로 수정") {
        val customerId = AccountFixture.id
        context("인증되지 않은 요청인 경우") {
            it("401 Unauthorized") {
                webTestClient
                    .put().uri("/account/profile/$customerId/image")
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            it("403 Forbidden") {
                webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = "1234"
                            }
                    )
                    .put().uri("/account/profile/$customerId/image")
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        describe("인증 이후") {
            val payload = AccountFixture.createUpdateProfileImagePayload()
            val client = webTestClient
                .mutateWith(
                    SecurityMockServerConfigurers.mockOpaqueToken()
                        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                        .attributes { attrs ->
                            attrs["sub"] = customerId
                        }
                )
                .put().uri("/account/profile/$customerId/image")
                .contentType(MediaType.APPLICATION_JSON)

            describe("필드 값 유효성 검사") {
                context("공백만 있는 경우") {
                    val invalidPayload = UpdateProfileImagePayload("  ")
                    it("400 Bad Request") {
                        client.bodyValue(invalidPayload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response: 다중 필드 에러") {
                        client.bodyValue(invalidPayload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                            .let { fields ->
                                fields.first { it.field == "image" }.message shouldBe "올바르지 않은 형식입니다"
                            }
                    }
                }

                context("빈값인 경우") {
                    val invalidPayload = UpdateProfileImagePayload("")
                    coEvery {
                        accountFacadeService.updateProfileImage(
                            customerId,
                            payload
                        )
                    } throws IllegalArgumentException()
                    it("Error Response: 다중 필드 에러") {
                        client.bodyValue(invalidPayload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                            .let { fields ->
                                fields.first { it.field == "image" }.message shouldBe "올바르지 않은 형식입니다"
                            }
                    }
                }
            }

            context("고객 번호 조회시 계정이 없는 경우") {
                coEvery { accountFacadeService.updateProfileImage(customerId, payload) } throws Exception()
                it("500 Internal Server Error") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("마이페이지 이미지 경로 수정 완료한 경우") {
                coEvery { accountFacadeService.updateProfileImage(customerId, payload) } returns Unit

                it("204 No Content") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isNoContent
                }

                it("Response Body: empty") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody().isEmpty
                }
            }
        }
    }
})
