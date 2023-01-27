package io.commerce.accountservice.shippingAddress.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.core.SecurityConstants
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.commerce.accountservice.shippingAddress.ShippingAddressView
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import kotlinx.coroutines.flow.asFlow
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class GetShippingAddressesTests(
    @MockkBean
    private val shippingAddressService: ShippingAddressService,
    private val webTestClient: WebTestClient
) : DescribeSpec({
    val customerId = AccountFixture.id

    describe("배송지 목록") {
        context("인증되지 않은 경우") {
            it("401 Unauthorized") {
                webTestClient.get().uri("/account/$customerId/shipping-addresses")
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
                    .get().uri("/account/$customerId/shipping-addresses")
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
                .get().uri("/account/$customerId/shipping-addresses")

            context("배송지 목록 처리 완료한 경우") {
                coEvery { shippingAddressService.getShippingAddresses(customerId) } returns ShippingAddressFixture.generateShippingAddresses(
                    customerId,
                    5
                ).asFlow()

                it("200 Ok") {
                    client.exchange()
                        .expectStatus().isOk
                }

                it("Response Body: List<ShippingAddressView>") {
                    client.exchange()
                        .expectBodyList<ShippingAddressView>()
                }
            }
        }
    }
})
