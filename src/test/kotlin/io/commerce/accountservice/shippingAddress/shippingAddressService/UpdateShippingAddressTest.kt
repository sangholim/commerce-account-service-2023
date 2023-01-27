package io.commerce.accountservice.shippingAddress.shippingAddressService

import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import org.bson.types.ObjectId

class UpdateShippingAddressTest : BehaviorSpec({

    val shippingAddressRepository: ShippingAddressRepository = mockk()
    val shippingAddressService = ShippingAddressService(shippingAddressRepository)
    val customerId = AccountFixture.id
    val payload = ShippingAddressFixture.createShippingAddressPayload()

    Given("고객의 배송지에 없는 경우") {
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10)
        val shippingAddressId = ObjectId.get()
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        Then("예외가 발생한다.") {
            shouldThrow<Exception> { shippingAddressService.updateShippingAddress(shippingAddressId, customerId, payload) }
        }
    }

    Given("기본 배송지를 비활성화 하는 경우") {
        val shippingAddress = ShippingAddressFixture.createShippingAddress(customerId, ShippingAddressFixture.createPrimaryShippingAddressPayload())
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10).plus(shippingAddress)
        val shippingAddressId = shippingAddress.id!!
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        Then("예외가 발생한다") {
            shouldThrow<Exception> { shippingAddressService.updateShippingAddress(shippingAddressId, customerId, payload) }
        }
    }

    Given("배송지 수정") {
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10)
        val updateShippingAddress = shippingAddresses.first().update(payload)
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        coEvery { shippingAddressRepository.save(updateShippingAddress) } returns updateShippingAddress
        val result = shippingAddressService.updateShippingAddress(updateShippingAddress.id!!, customerId, payload)
        Then("배송지 결과값은 기대값과 일치한다") {
            result shouldBe updateShippingAddress
        }
    }
})
