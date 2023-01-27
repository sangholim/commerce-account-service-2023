package io.commerce.accountservice.account.controller

import io.commerce.accountservice.account.AccountValidationPayload
import io.commerce.accountservice.account.AgreementValidationPayload
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SimpleFieldError
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.accountValidationPayload
import io.commerce.accountservice.validation.ValidationMessages
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.mockk.clearAllMocks
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
class ValidationTests(
    private val webTestClient: WebTestClient
) : BehaviorSpec({

    beforeTest {
        clearAllMocks()
    }

    fun exchange(payload: AccountValidationPayload) =
        webTestClient
            .post().uri("/account/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchange()

    Given("부분 필드 유효성 검사") {

        When("부분 필드 유효성 검사 실패한 경우") {
            val payload = accountValidationPayload {
                this.email = ""
                this.name = ""
                this.agreement = AgreementValidationPayload(false, null)
            }

            val exchange = exchange(payload)

            val expected = listOf(
                SimpleFieldError("email", ValidationMessages.INVALID_FORMAT),
                SimpleFieldError("name", ValidationMessages.INVALID_NAME),
                SimpleFieldError("agreement.serviceTerm", ValidationMessages.REQUIRED_AGREEMENT)
            )
            Then("status: 400 Bad Request") {
                exchange.expectStatus().isBadRequest
            }
            Then("body: 다중 필드 에러") {
                exchange.expectBody<ErrorResponse>().returnResult().responseBody!!.fields shouldContainAll expected
            }
        }

        When("부분 필드 유효성 검사 성공한 경우") {
            val payload = accountValidationPayload {
                this.email = AccountFixture.loginUser
                this.name = AccountFixture.name
            }

            val exchange = exchange(payload)

            Then("status: 204 No Content") {
                exchange.expectStatus().isNoContent
            }
        }
    }

    Given("모든 필드 유효성 검사") {

        When("모든 필드 유효성 검사 실패한 경우") {
            val payload = accountValidationPayload {
                this.email = ""
                this.name = ""
                this.password = ""
                this.phoneNumber = ""
                this.agreement = AgreementValidationPayload(false, false)
            }

            val exchange = exchange(payload)

            val expected = listOf(
                SimpleFieldError("email", ValidationMessages.INVALID_FORMAT),
                SimpleFieldError("password", ValidationMessages.INVALID_PASSWORD),
                SimpleFieldError("name", ValidationMessages.INVALID_NAME),
                SimpleFieldError("phoneNumber", ValidationMessages.INVALID_FORMAT),
                SimpleFieldError("agreement.serviceTerm", ValidationMessages.REQUIRED_AGREEMENT),
                SimpleFieldError("agreement.privacyTerm", ValidationMessages.REQUIRED_AGREEMENT)
            )
            Then("status: 400 Bad Request") {
                exchange.expectStatus().isBadRequest
            }
            Then("body: 다중 필드 에러") {
                exchange.expectBody<ErrorResponse>().returnResult().responseBody!!.fields shouldContainAll expected
            }
        }

        When("모든 필드 유효성 검사 성공한 경우") {
            val payload = accountValidationPayload {
                this.email = AccountFixture.loginUser
                this.name = AccountFixture.name
                this.password = AccountFixture.password
                this.phoneNumber = AccountFixture.normalPhoneNumber
                this.agreement = AgreementValidationPayload(true, true)
            }

            val exchange = exchange(payload)

            Then("status: 204 No Content") {
                exchange.expectStatus().isNoContent
            }
        }
    }
})
