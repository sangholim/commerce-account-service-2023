package io.commerce.accountservice.account.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.PhoneNumberNotVerifiedException
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
class UpdateProfilePasswordTests(
    @MockkBean
    private val accountFacadeService: AccountFacadeService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    describe("마이페이지 비밀번호 재설정") {
        val customerId = AccountFixture.id
        context("인증되지 않은 요청인 경우") {
            it("401 Unauthorized") {
                webTestClient
                    .put().uri("/account/profile/$customerId/password")
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
                    .put().uri("/account/profile/$customerId/password")
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        describe("인증 이후") {
            val invalidPayload = AccountFixture.createInvalidUpdatePasswordPayload()
            val payload = AccountFixture.createUpdatePasswordPayload()
            val client = webTestClient
                .mutateWith(
                    SecurityMockServerConfigurers.mockOpaqueToken()
                        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                        .attributes { attrs ->
                            attrs["sub"] = customerId
                        }
                )
                .put().uri("/account/profile/$customerId/password")
                .contentType(MediaType.APPLICATION_JSON)

            context("필드 값 유효성 검사 실패한 경우") {
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
                            fields.first { it.field == "phoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            fields.first { it.field == "password" }.message shouldBe "8~36자 영문, 숫자, 특수문자를 사용하세요"
                        }
                }
            }

            context("인증 받지 않은 휴대폰 번호인 경우") {
                coEvery {
                    accountFacadeService.updatePassword(
                        customerId,
                        payload
                    )
                } throws PhoneNumberNotVerifiedException()

                it("400 Bad Request") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response 에러코드: phone_number_not_verified") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.code shouldBe "phone_number_not_verified"
                }

                it("Error Response 메시지: 인증을 완료해주세요") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>().returnResult().responseBody!!.message shouldBe "인증을 완료해주세요"
                }
            }

            context("고객 번호 조회시 계정이 없는 경우") {
                coEvery {
                    accountFacadeService.updatePassword(
                        customerId,
                        payload
                    )
                } throws Exception()
                it("500 Internal Server Error") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("자신의 계정의 휴대폰 번호와 다른 경우") {
                coEvery {
                    accountFacadeService.updatePassword(
                        customerId,
                        payload
                    )
                } throws Exception()

                it("500 Internal Server Error") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("마이페이지 비밀번호 재설정 완료한 경우") {
                coEvery { accountFacadeService.updatePassword(customerId, payload) } returns Unit

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
