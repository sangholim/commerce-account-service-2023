package io.commerce.accountservice.shippingAddress.shippingAddressService

import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddress
import io.commerce.accountservice.shippingAddress.ShippingAddressMaxSizeException
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow

class CreateShippingAddressTest : BehaviorSpec({

    val shippingAddressRepository: ShippingAddressRepository = mockk()
    val shippingAddressService = ShippingAddressService(shippingAddressRepository)
    val customerId = AccountFixture.id
    val payload = ShippingAddressFixture.createShippingAddressPayload()

    Given("생성시 배송지 갯수 초과하는 경우") {
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 20)
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        Then("ShippingAddressMaxSizeException 예외가 발생한다.") {
            shouldThrow<ShippingAddressMaxSizeException> { shippingAddressService.createShippingAddress(customerId, payload) }
        }
    }

    Given("첫 배송지 생성하는 경우") {
        val shippingAddress = ShippingAddress.ofPrimary(customerId, payload)
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns emptyFlow()
        coEvery { shippingAddressRepository.save(shippingAddress) } returns shippingAddress
        val result = shippingAddressService.createShippingAddress(customerId, payload)

        Then("첫 배송지는 기본 배송지이다") {
            result.primary shouldBe true
        }
    }

    Given("배송지 생성하는 경우") {
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10).plus(ShippingAddress.ofPrimary(customerId, payload))
        val shippingAddress = ShippingAddress.of(customerId, payload)
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        coEvery { shippingAddressRepository.save(shippingAddress) } returns shippingAddress
        val result = shippingAddressService.createShippingAddress(customerId, payload)

        Then("배송지 결과값은 기대값과 일치한다") {
            result shouldBe shippingAddress
        }
    }
})
