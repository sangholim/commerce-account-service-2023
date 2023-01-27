package io.commerce.accountservice.shippingAddress.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.ErrorResponse
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressMaxSizeException
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
class CreateShippingAddressTests(
    @MockkBean
    private val shippingAddressService: ShippingAddressService,
    private val webTestClient: WebTestClient
) : DescribeSpec({
    val customerId = AccountFixture.id
    val invalidPayload = ShippingAddressFixture.createInvalidShippingAddressDTO()
    val payload = ShippingAddressFixture.createShippingAddressPayload()
    describe("배송지 생성") {
        context("인증되지 않은 경우") {
            it("401 Unauthorized") {
                webTestClient.post().uri("/account/$customerId/shipping-addresses")
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
                    .post().uri("/account/$customerId/shipping-addresses")
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
                .post().uri("/account/$customerId/shipping-addresses")
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
                            fields.first { it.field == "name" }.message shouldBe "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요."
                            fields.first { it.field == "recipient" }.message shouldBe "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요."
                            fields.first { it.field == "primaryPhoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            fields.first { it.field == "secondaryPhoneNumber" }.message shouldBe "올바르지 않은 형식입니다"
                            fields.first { it.field == "zipCode" }.message shouldBe "필수 정보입니다"
                            fields.first { it.field == "line1" }.message shouldBe "필수 정보입니다"
                        }
                }
            }

            context("배송지 갯수가 20개 초과한 경우") {
                coEvery {
                    shippingAddressService.createShippingAddress(
                        customerId,
                        payload
                    )
                } throws ShippingAddressMaxSizeException()
                it("400 Bad Request") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isBadRequest
                }

                it("Error Response 메시지: 이미 가입된 이메일입니다") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectBody<ErrorResponse>()
                        .returnResult().responseBody!!.message shouldBe "배송지는 최대 20개까지 등록할 수 있어요!"
                }
            }

            context("배송지 생성 완료한 경우") {
                coEvery {
                    shippingAddressService.createShippingAddress(
                        customerId,
                        payload
                    )
                } returns ShippingAddressFixture.createShippingAddress(customerId, payload)
                it("201 Created") {
                    client.bodyValue(payload)
                        .exchange()
                        .expectStatus().isCreated
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
