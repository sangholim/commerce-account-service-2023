package io.commerce.accountservice.account.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.account.EmailDuplicateException
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.VerificationFixture
import io.commerce.accountservice.verification.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThan
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
class ActivationVerifyTests(
    @MockkBean
    private val accountFacadeService: AccountFacadeService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    describe("로그인 이후 프로필 업데이트 이메일 인증") {
        context("인증되지 않은 요청인 경우") {
            it("401 Unauthorized") {
                webTestClient
                    .post().uri("/account/activation/verify/email")
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }
        describe("인증 이후") {
            val email = AccountFixture.loginUser
            val customerId = AccountFixture.id
            describe("이메일 인증 번호 발송") {
                val invalidPayload = VerificationPayload("kkk")
                val payload = VerificationPayload(email)
                val client = webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = customerId
                            }
                    )
                    .post().uri("/account/activation/verify/email")
                    .contentType(MediaType.APPLICATION_JSON)

                context("필드 값 유효성 검사 실패한 경우") {
                    it("400 Bad Request") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response: 다중 필드 에러") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                            .let { fields ->
                                fields.first { it.field == "email" }.message shouldBe "올바르지 않은 형식입니다"
                            }
                    }
                }

                context("고객 번호 조회시 계정이 없는 경우") {
                    coEvery {
                        accountFacadeService.sendVerificationMessage(
                            VerificationItem.ACTIVATION,
                            VerificationType.EMAIL,
                            payload,
                            customerId
                        )
                    } throws Exception()
                    it("500 Internal Server Error") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectStatus().is5xxServerError
                    }
                }

                context("변경할 이메일이 인증 받은 자신의 이메일인 경우") {
                    coEvery {
                        accountFacadeService.sendVerificationMessage(
                            VerificationItem.ACTIVATION,
                            VerificationType.EMAIL,
                            payload,
                            customerId
                        )
                    } throws Exception()
                    it("500 Internal Server Error") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectStatus().is5xxServerError
                    }
                }

                context("변경할 이메일을 다른 계정이 사용중인 경우") {
                    coEvery {
                        accountFacadeService.sendVerificationMessage(
                            VerificationItem.ACTIVATION,
                            VerificationType.EMAIL,
                            payload,
                            customerId
                        )
                    } throws EmailDuplicateException()
                    it("400 Bad Request") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response 에러코드: email_duplicated") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.code shouldBe "email_duplicated"
                    }

                    it("Error Response 메시지: 이미 가입된 이메일입니다") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.message shouldBe "이미 가입된 이메일입니다"
                    }
                }

                context("이메일 인증 번호 발송 완료 하는 경우") {
                    it("201 Created") {
                        coEvery {
                            accountFacadeService.sendVerificationMessage(
                                VerificationItem.ACTIVATION,
                                VerificationType.EMAIL,
                                payload,
                                customerId
                            )
                        } returns VerificationFixture.createVerification()
                            .toVerificationView(VerificationType.EMAIL.expiry)
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().isCreated
                    }

                    it("Response Body: VerificationView") {
                        coEvery {
                            accountFacadeService.sendVerificationMessage(
                                VerificationItem.ACTIVATION,
                                VerificationType.EMAIL,
                                payload,
                                customerId
                            )
                        } returns VerificationFixture.createVerification()
                            .toVerificationView(VerificationType.EMAIL.expiry)
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectBody<VerificationView>().returnResult().responseBody!!
                            .let { response ->
                                response.expiredIn shouldBeGreaterThan 0
                                response.expiredAt shouldBeGreaterThan 0
                            }
                    }
                }
            }

            describe("이메일 인증 번호 검증") {
                val invalidPayload = VerificationPayload("kkk", "000000")
                val payload = VerificationPayload(email, "000000")
                val client = webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = customerId
                            }
                    )
                    .post().uri("/account/activation/verify/email")
                    .contentType(MediaType.APPLICATION_JSON)

                context("필드 값 유효성 검사 실패한 경우") {
                    it("400 Bad Request") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response: 다중 필드 에러") {
                        client
                            .bodyValue(invalidPayload)
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
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationInvalidException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationFailException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationExceedLimitException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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
                    it("204 No Content") {
                        coEvery {
                            accountFacadeService.checkVerification(
                                VerificationItem.ACTIVATION,
                                payload
                            )
                        } returns true
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().isNoContent
                    }

                    it("Response Body: empty") {
                        client
                            .exchange()
                            .expectBody().isEmpty
                    }
                }
            }
        }
    }

    describe("로그인 이후 프로필 업데이트 휴대폰 번호 인증") {
        context("인증되지 않은 요청인 경우") {
            it("401 Unauthorized") {
                webTestClient
                    .post().uri("/account/activation/verify/sms")
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }

        describe("인증 이후") {
            val phoneNumber = AccountFixture.normalPhoneNumber
            val customerId = AccountFixture.id

            describe("휴대폰 번호 인증 번호 발송") {
                val invalidPayload = VerificationPayload("kkk")
                val payload = VerificationPayload(phoneNumber)
                val client = webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = customerId
                            }
                    )
                    .post().uri("/account/activation/verify/sms")
                    .contentType(MediaType.APPLICATION_JSON)

                context("필드 값 유효성 검사 실패한 경우") {
                    it("400 Bad Request") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response: 다중 필드 에러") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                            .let { fields ->
                                fields.first { it.field == "phoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            }
                    }
                }

                context("고객 번호 조회시 계정이 없는 경우") {
                    coEvery {
                        accountFacadeService.sendVerificationMessage(
                            VerificationItem.ACTIVATION,
                            VerificationType.SMS,
                            payload,
                            customerId
                        )
                    } throws Exception()
                    it("500 Internal Server Error") {
                        client.bodyValue(payload)
                            .exchange()
                            .expectStatus().is5xxServerError
                    }
                }

                context("변경할 휴대폰 번호가 이미 인증 받은 자신의 휴대폰 번호인 경우") {
                    coEvery {
                        accountFacadeService.sendVerificationMessage(
                            VerificationItem.ACTIVATION,
                            VerificationType.SMS,
                            payload,
                            customerId
                        )
                    } throws Exception()
                    it("500 Internal Sever Error") {
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().is5xxServerError
                    }
                }

                context("휴대폰 번호 인증 번호 발송 완료한 경우") {
                    it("201 Created") {
                        coEvery {
                            accountFacadeService.sendVerificationMessage(
                                VerificationItem.ACTIVATION,
                                VerificationType.SMS,
                                payload,
                                customerId
                            )
                        } returns VerificationFixture.createVerification()
                            .toVerificationView(VerificationType.SMS.expiry)
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().isCreated
                    }
                    it("Response Body: VerificationView") {
                        coEvery {
                            accountFacadeService.sendVerificationMessage(
                                VerificationItem.ACTIVATION,
                                VerificationType.SMS,
                                payload,
                                customerId
                            )
                        } returns VerificationFixture.createVerification()
                            .toVerificationView(VerificationType.SMS.expiry)
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectBody<VerificationView>().returnResult().responseBody!!
                            .let { response ->
                                response.expiredIn shouldBeGreaterThan 0
                                response.expiredAt shouldBeGreaterThan 0
                            }
                    }
                }
            }

            describe("휴대폰 번호 인증 번호 검증") {
                val invalidPayload = VerificationPayload("kkk", "000000")
                val payload = VerificationPayload(phoneNumber, "000000")
                val client = webTestClient
                    .mutateWith(
                        SecurityMockServerConfigurers.mockOpaqueToken()
                            .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                            .attributes { attrs ->
                                attrs["sub"] = customerId
                            }
                    )
                    .post().uri("/account/activation/verify/sms")
                    .contentType(MediaType.APPLICATION_JSON)
                context("필드 값 유효성 검사 실패한 경우") {
                    it("400 Bad Request") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectStatus().isBadRequest
                    }

                    it("Error Response: 다중 필드 에러") {
                        client
                            .bodyValue(invalidPayload)
                            .exchange()
                            .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                            .let { fields ->
                                fields.first { it.field == "phoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            }
                    }
                }

                context("인증 정보가 존재하지 않는 경우") {
                    coEvery {
                        accountFacadeService.checkVerification(
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationInvalidException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationFailException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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
                            VerificationItem.ACTIVATION,
                            payload
                        )
                    } throws VerificationExceedLimitException()
                    it("400 Bad Request") {
                        client
                            .bodyValue(payload)
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

                context("휴대폰 번호 인증 번호 검증 완료한 경우") {
                    it("204 No Content") {
                        coEvery {
                            accountFacadeService.checkVerification(
                                VerificationItem.ACTIVATION,
                                payload
                            )
                        } returns true
                        client
                            .bodyValue(payload)
                            .exchange()
                            .expectStatus().isNoContent
                    }

                    it("Response Body: empty") {
                        client
                            .exchange()
                            .expectBody().isEmpty
                    }
                }
            }
        }
    }
})
