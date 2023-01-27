package io.commerce.accountservice.shippingAddress.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressService
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
class UpdateShippingAddressTests(
    @MockkBean
    private val shippingAddressService: ShippingAddressService,
    private val webTestClient: WebTestClient
) : DescribeSpec({
    val customerId = AccountFixture.id
    val invalidShippingAddressId = "abcd"
    val invalidPayload = ShippingAddressFixture.createInvalidShippingAddressDTO()
    val payload = ShippingAddressFixture.createShippingAddressPayload()
    val shippingAddress = ShippingAddressFixture.createShippingAddress(customerId, payload)

    describe("배송지 수정") {
        context("인증되지 않은 경우") {
            it("401 Unauthorized") {
                webTestClient.put().uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
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
                    .put().uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                    .exchange()
                    .expectStatus().isForbidden
            }
        }

        describe("인증 이후") {
            val client = webTestClient
                .mutateWith(
                    SecurityMockServerConfigurers.mockOpaqueToken()
                        .authorities(SimpleGrantedAuthority(SecurityConstants.CUSTOMER))
                        .attributes { attrs ->
                            attrs["sub"] = customerId
                        }
                )
                .put()

            context("path-variable 유효하지 않은 경우 (배송지 번호)") {
                it("400 Bad Request") {
                    client.uri("/account/$customerId/shipping-addresses/$invalidShippingAddressId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .exchange()
                        .expectStatus().isBadRequest
                }
            }

            context("필드 값 유효성 검사 실패한 경우") {

                it("400 Bad Request") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invalidPayload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response: 다중 필드 에러") {
                    client.bodyValue(invalidPayload)
                        .exchange()
                        .expectBody<ErrorResponse>().returnResult().responseBody!!.fields
                        .let { fields ->
                            fields.first { it.field == "name" }.message shouldBe "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요."
                            fields.first { it.field == "recipient" }.message shouldBe "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요."
                            fields.first { it.field == "primaryPhoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            fields.first { it.field == "secondaryPhoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            fields.first { it.field == "zipCode" }.message shouldBe "필수 정보입니다"
                            fields.first { it.field == "line1" }.message shouldBe "필수 정보입니다"
                        }
                }
            }

            context("배송지 번호로 조회시 존재하지 않는 경우") {
                coEvery {
                    shippingAddressService.updateShippingAddress(
                        shippingAddress.id!!,
                        customerId,
                        payload
                    )
                } throws Exception()
                it("500 Internal Server Error") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("배송지 의 고객 번호가 path-variable(고객 번호)과 다른 경우") {
                coEvery {
                    shippingAddressService.updateShippingAddress(
                        shippingAddress.id!!,
                        customerId,
                        payload
                    )
                } throws Exception()
                it("500 Internal Server Error") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("배송지 수정 완료한 경우") {
                coEvery {
                    shippingAddressService.updateShippingAddress(
                        shippingAddress.id!!,
                        customerId,
                        payload
                    )
                } returns shippingAddress
                it("204 No Content") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .exchange()
                        .expectStatus().isNoContent
                }

                it("Response Body: empty") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(payload)
                        .exchange()
                        .expectBody().isEmpty
                }
            }
        }
    }
})
