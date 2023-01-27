package io.commerce.accountservice.shippingAddress.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class DeleteShippingAddressTests(
    @MockkBean
    private val shippingAddressService: ShippingAddressService,
    private val webTestClient: WebTestClient
) : DescribeSpec({
    val customerId = AccountFixture.id
    val invalidShippingAddressId = "abcd"
    val payload = ShippingAddressFixture.createShippingAddressPayload()
    val shippingAddress = ShippingAddressFixture.createShippingAddress(customerId, payload)

    describe("배송지 삭제") {
        context("인증되지 않은 경우") {
            it("401 Unauthorized") {
                webTestClient.delete().uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
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
                    .delete().uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
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
                .delete()
            context("path-variable 유효하지 않은 경우 (배송지 번호)") {
                it("400 Bad Request") {
                    client.uri("/account/$customerId/shipping-addresses/$invalidShippingAddressId")
                        .exchange()
                        .expectStatus().isBadRequest
                }
            }

            context("배송지 번호로 조회시 존재하지 않는 경우") {
                coEvery {
                    shippingAddressService.deleteShippingAddress(
                        shippingAddress.id!!,
                        customerId
                    )
                } throws Exception()
                it("500 Internal Server Error") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("배송지 의 고객 번호가 path-variable(고객 번호)과 다른 경우") {
                coEvery {
                    shippingAddressService.deleteShippingAddress(
                        shippingAddress.id!!,
                        customerId
                    )
                } throws Exception()
                it("500 Internal Server Error") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .exchange()
                        .expectStatus().is5xxServerError
                }
            }

            context("배송지 삭제 완료한 경우") {
                coEvery { shippingAddressService.deleteShippingAddress(shippingAddress.id!!, customerId) } returns Unit
                it("204 No Content") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .exchange()
                        .expectStatus().isNoContent
                }

                it("Response Body: empty") {
                    client.uri("/account/$customerId/shipping-addresses/${shippingAddress.id}")
                        .exchange()
                        .expectBody().isEmpty
                }
            }
        }
    }
})
