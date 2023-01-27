package io.commerce.accountservice.account.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.EmailInvalidException
import io.commerce.accountservice.account.EmailNotVerifiedException
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.fixture.AccountFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class ResetPasswordTests(
    @MockkBean
    private val accountFacadeService: AccountFacadeService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    describe("비밀번호 초기화") {
        val invalidPayload = AccountFixture.createInvalidResetPasswordPayload()
        val payload = AccountFixture.createResetPasswordPayload()
        val client = webTestClient
            .post().uri("/account/reset-password")
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
                        fields.first { it.field == "email" }.message shouldBe "올바르지 않은 형식입니다"
                        fields.first { it.field == "password" }.message shouldBe "8~36자 영문, 숫자, 특수문자를 사용하세요"
                    }
            }
        }

        context("이메일 조회시 계정이 없는 경우") {
            coEvery { accountFacadeService.resetPassword(payload) } throws EmailInvalidException()
            it("400 Bad Request") {
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            it("Error Response 에러코드: email_invalid") {
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>().returnResult().responseBody!!.code shouldBe "email_invalid"
            }

            it("Error Response 메시지: 존재하지 않는 이메일입니다") {
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>().returnResult().responseBody!!.message shouldBe "존재하지 않는 이메일입니다"
            }
        }

        context("인증 받지 않은 이메일인 경우") {
            coEvery { accountFacadeService.resetPassword(payload) } throws EmailNotVerifiedException()
            it("400 Bad Request") {
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }

            it("Error Response 에러코드: email_not_verified") {
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>().returnResult().responseBody!!.code shouldBe "email_not_verified"
            }

            it("Error Response 메시지: 인증을 완료해주세요") {
                client.bodyValue(payload)
                    .exchange()
                    .expectBody<ErrorResponse>().returnResult().responseBody!!.message shouldBe "인증을 완료해주세요"
            }
        }

        context("비밀번호 초기화 완료한 경우") {
            coEvery { accountFacadeService.resetPassword(payload) } returns Unit
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
})
