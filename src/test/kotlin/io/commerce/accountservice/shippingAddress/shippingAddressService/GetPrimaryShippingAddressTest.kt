package io.commerce.accountservice.shippingAddress.shippingAddressService

import io.commerce.accountservice.core.NotFoundException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk

class GetPrimaryShippingAddressTest : BehaviorSpec({

    val shippingAddressRepository: ShippingAddressRepository = mockk()
    val shippingAddressService = ShippingAddressService(shippingAddressRepository)
    val customerId = AccountFixture.id
    val primaryShippingAddress = ShippingAddressFixture.createShippingAddress(customerId, ShippingAddressFixture.createPrimaryShippingAddressPayload())
    beforeEach {
        clearMocks(shippingAddressRepository)
    }

    Given("기본 배송지가 없는 경우") {
        coEvery { shippingAddressRepository.findFirstByCustomerIdAndPrimary(customerId, true) } throws NotFoundException()
        Then("예외를 발생 한다") {
            shouldThrow<Exception> { shippingAddressService.getPrimaryShippingAddress(customerId) }
        }
    }

    Given("기본 배송지가 존재하는 경우") {
        coEvery { shippingAddressRepository.findFirstByCustomerIdAndPrimary(customerId, true) } returns primaryShippingAddress
        val shippingAddress = shippingAddressService.getPrimaryShippingAddress(customerId)
        Then("배송지는  primaryShippingAddress 과 같다") {
            shippingAddress shouldBe primaryShippingAddress
        }
    }
})
