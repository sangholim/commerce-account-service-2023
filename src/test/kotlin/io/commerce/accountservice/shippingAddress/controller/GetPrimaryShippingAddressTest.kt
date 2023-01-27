package io.commerce.accountservice.shippingAddress.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.NotFoundException
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.commerce.accountservice.shippingAddress.ShippingAddressView
import io.commerce.accountservice.shippingAddress.toShippingAddressView
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class GetPrimaryShippingAddressTest(
    @MockkBean
    private val shippingAddressService: ShippingAddressService,
    private val webTestClient: WebTestClient
) : DescribeSpec({

    val customerId = AccountFixture.id

    describe("기본 배송지 조회") {
        context("인증되지 않은 경우") {
            it("401 Unauthorized") {
                webTestClient.get().uri("/account/$customerId/shipping-addresses/primary")
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
                    .get().uri("/account/$customerId/shipping-addresses/primary")
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
                .get().uri("/account/$customerId/shipping-addresses/primary")
            context("기본 배송지가 없는 경우") {
                coEvery { shippingAddressService.getPrimaryShippingAddress(customerId) } throws NotFoundException()
                val response = client.exchange()
                it("404 Not Found") {
                    response.expectStatus().isNotFound
                }
            }

            context("기본 배송지가 있는 경우") {
                val primaryShippingAddress = ShippingAddressFixture.createShippingAddress(
                    customerId,
                    ShippingAddressFixture.createPrimaryShippingAddressPayload()
                )
                coEvery { shippingAddressService.getPrimaryShippingAddress(customerId) } returns primaryShippingAddress
                val response = client.exchange()
                it("200 Ok") {
                    response.expectStatus().isOk
                }

                it("Response Body: primaryShippingAddressView") {
                    response.expectBody<ShippingAddressView>()
                        .returnResult().responseBody shouldBe primaryShippingAddress.toShippingAddressView()
                }
            }
        }
    }
})
