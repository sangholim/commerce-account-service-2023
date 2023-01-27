package io.commerce.accountservice.shippingAddress.shippingAddressService

import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.asFlow
import org.bson.types.ObjectId

class DeleteShippingAddressTest : BehaviorSpec({

    val shippingAddressRepository: ShippingAddressRepository = mockk()
    val shippingAddressService = ShippingAddressService(shippingAddressRepository)
    val customerId = AccountFixture.id

    Given("배송지 번호가 고객의 배송지에 없는 경우") {
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10)
        val shippingAddressId = ObjectId.get()
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        Then("예외가 발생한다.") {
            shouldThrow<Exception> { shippingAddressService.deleteShippingAddress(shippingAddressId, customerId) }
        }
    }

    Given("배송지 삭제한 경우") {
        val deleteShippingAddress = ShippingAddressFixture.createShippingAddress(customerId, ShippingAddressFixture.createPrimaryShippingAddressPayload())
        val shippingAddresses = ShippingAddressFixture.generateShippingAddresses(customerId, 10).plus(deleteShippingAddress)
        val shippingAddress = shippingAddresses.first { it.id != deleteShippingAddress.id }.enablePrimary()
        coEvery { shippingAddressRepository.findByCustomerId(customerId) } returns shippingAddresses.asFlow()
        coEvery { shippingAddressRepository.delete(deleteShippingAddress) } returns Unit
        coEvery { shippingAddressRepository.save(shippingAddress) } returns shippingAddress
        val result = shippingAddressService.deleteShippingAddress(deleteShippingAddress.id!!, customerId)
        Then("반환값이 존재하지 않는다.") {
            result.shouldBeInstanceOf<Unit>()
        }
    }
})
