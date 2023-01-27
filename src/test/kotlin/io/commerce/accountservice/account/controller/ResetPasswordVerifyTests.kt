package io.commerce.accountservice.account.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.EmailInvalidException
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.VerificationFixture
import io.commerce.accountservice.verification.*
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
class ResetPasswordVerifyTests(
    @MockkBean
    private val accountFacadeService: AccountFacadeService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    describe("비밀번호 초기화 이메일 인증") {
        val email = AccountFixture.existUser
        val client = webTestClient
            .post().uri("/account/reset-password/verify/email")
            .contentType(MediaType.APPLICATION_JSON)
        describe("인증 번호 발송") {
            val invalidPayload = VerificationPayload("kkk")
            val payload = VerificationPayload(email)
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
                        }
                }
            }

            context("이메일 조회시 계정이 없는 경우") {
                coEvery {
                    accountFacadeService.sendVerificationMessage(
                        VerificationItem.RESET_PASSWORD,
                        VerificationType.EMAIL,
                        payload
                    )
                } throws EmailInvalidException()
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

            context("인증 번호 발송 완료한 경우") {
                coEvery {
                    accountFacadeService.sendVerificationMessage(
                        VerificationItem.RESET_PASSWORD,
                        VerificationType.EMAIL,
                        payload
                    )
                } returns VerificationFixture.createVerification().toVerificationView(
                    VerificationType.EMAIL.expiry
                )

                it("201 Created") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isCreated
                }

                it("Response Body: VerificationView") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<VerificationView>()
                }
            }
        }

        describe("인증 번호 검증") {
            val invalidPayload = VerificationPayload("kkk", "000000")
            val payload = VerificationPayload(email, "000000")

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
                        }
                }
            }
            context("인증 정보가 존재하지 않는 경우") {
                coEvery {
                    accountFacadeService.checkVerification(
                        VerificationItem.RESET_PASSWORD,
                        payload
                    )
                } throws VerificationInvalidException()
                it("400 Bad Request") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response 에러코드: verification_invalid") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.code shouldBe "verification_invalid"
                }

                it("Error Response 메시지: 존재하지 않는 인증 정보입니다") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.message shouldBe "존재하지 않는 인증 정보입니다"
                }
            }

            context("인증 번호가 틀린 경우") {
                coEvery {
                    accountFacadeService.checkVerification(
                        VerificationItem.RESET_PASSWORD,
                        payload
                    )
                } throws VerificationFailException()
                it("400 Bad Request") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response 에러코드: verification_failed") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.code shouldBe "verification_failed"
                }

                it("Error Response 메시지: 인증번호가 일치하지 않습니다") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.message shouldBe "인증번호가 일치하지 않습니다"
                }
            }

            context("인증 횟수 5회 초과한 경우") {
                coEvery {
                    accountFacadeService.checkVerification(
                        VerificationItem.RESET_PASSWORD,
                        payload
                    )
                } throws VerificationExceedLimitException()
                it("400 Bad Request") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response 에러코드: verification_exceed_limit") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.code shouldBe "verification_exceed_limit"
                }

                it("Error Response 메시지: 5번 틀리셨습니다. 인증번호를 다시 받아주세요") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.message shouldBe "5번 틀리셨습니다. 인증번호를 다시 받아주세요"
                }
            }

            context("이메일 인증 번호 검증 완료한 경우") {
                coEvery {
                    accountFacadeService.checkVerification(
                        VerificationItem.RESET_PASSWORD,
                        payload
                    )
                } returns true

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
