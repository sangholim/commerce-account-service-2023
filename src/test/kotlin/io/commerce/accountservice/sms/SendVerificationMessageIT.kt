package io.commerce.accountservice.sms

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.marcinziolo.kotlin.wiremock.equalTo
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returnsJson
import io.commerce.accountservice.fixture.VerificationFixture
import io.commerce.accountservice.verification.VerificationConstants
import io.commerce.accountservice.verification.VerificationSendFailException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

@SpringBootTest
@AutoConfigureWireMock(port = 3535)
@Import(TestChannelBinderConfiguration::class)
class SendVerificationMessageIT(
    private val smsClient: SmsClient,
    private val objectMapper: ObjectMapper,
    private val wireMock: WireMockServer
) : BehaviorSpec({
    val verification = VerificationFixture.createVerification()

    afterEach { clearAllMocks() }

    Given("인증 메세지 발송 에러가 발생하는 경우") {

        When("응답 상태가 400인 경우") {
            val expected = HttpStatus.BAD_REQUEST.value()
            beforeEach {
                wireMock.post {
                    url equalTo "/send"
                } returnsJson {
                    statusCode = expected
                }
            }
            Then("response code: 400") {
                val result = shouldThrow<WebClientResponseException> { smsClient.sendVerificationMessage(verification) }
                result.statusCode.value() shouldBe expected
            }
        }

        When("응답 상태가 500인 경우") {
            val expected = HttpStatus.INTERNAL_SERVER_ERROR.value()
            beforeEach {
                wireMock.post {
                    url equalTo "/send"
                } returnsJson {
                    statusCode = expected
                }
            }
            Then("response code: 500") {
                val result = shouldThrow<WebClientResponseException> { smsClient.sendVerificationMessage(verification) }
                result.statusCode.value() shouldBe expected
            }
        }
    }

    Given("인증 메세지 발송 성공한 경우") {

        When("response body - result code 가 -101인 경우") {
            val expected = AligoView(
                resultCode = -101,
                message = "인증오류입니다."
            )
            beforeEach {
                wireMock.post {
                    url equalTo "/send"
                } returnsJson {
                    statusCode = HttpStatus.OK.value()
                    body = objectMapper.writeValueAsString(expected)
                }
            }

            Then("exception status : 400 BAD_REQUEST") {
                val result =
                    shouldThrow<VerificationSendFailException> { smsClient.sendVerificationMessage(verification) }
                result.status shouldBe HttpStatus.BAD_REQUEST
            }

            Then("exception reason : '인증할 수 없는 번호입니다'") {
                val result =
                    shouldThrow<VerificationSendFailException> { smsClient.sendVerificationMessage(verification) }
                result.reason shouldBe VerificationConstants.VERIFICATION_SEND_FAIL_MESSAGE
            }
        }

        When("response body - result code 가 -201인 경우") {
            val expected = AligoView(
                resultCode = -201,
                message = "보유건수부족(충전금부족) 오류"
            )
            beforeEach {
                wireMock.post {
                    url equalTo "/send"
                } returnsJson {
                    statusCode = HttpStatus.OK.value()
                    body = objectMapper.writeValueAsString(expected)
                }
            }

            Then("exception status : 400 BAD_REQUEST") {
                val result =
                    shouldThrow<VerificationSendFailException> { smsClient.sendVerificationMessage(verification) }
                result.status shouldBe HttpStatus.BAD_REQUEST
            }

            Then("exception reason : '인증할 수 없는 번호입니다'") {
                val result =
                    shouldThrow<VerificationSendFailException> { smsClient.sendVerificationMessage(verification) }
                result.reason shouldBe VerificationConstants.VERIFICATION_SEND_FAIL_MESSAGE
            }
        }

        When("response body - result code 가 1인 경우") {
            val expected = AligoView(
                resultCode = 1,
                message = ""
            )
            beforeEach {
                wireMock.post {
                    url equalTo "/send"
                } returnsJson {
                    statusCode = HttpStatus.OK.value()
                    body = objectMapper.writeValueAsString(expected)
                }
            }

            Then("Unit 타입의 객체를 반환한다") {
                val result = smsClient.sendVerificationMessage(verification)
                result shouldBe Unit
            }
        }
    }
})
