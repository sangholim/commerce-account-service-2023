package io.commerce.accountservice.shippingAddress.repository

import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.ShippingAddressFixture
import io.commerce.accountservice.shippingAddress.ShippingAddress
import io.commerce.accountservice.shippingAddress.ShippingAddressRepository
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import kotlinx.coroutines.flow.count
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
class ShippingAddressRepositoryTests(
    private val shippingAddressRepository: ShippingAddressRepository
) : DescribeSpec({
    val customerId = AccountFixture.id
    val payload = ShippingAddressFixture.createShippingAddressPayload()

    beforeEach {
        shippingAddressRepository.save(ShippingAddress.of(customerId, payload))
    }

    describe("findByCustomerId()") {
        it("고객 번호가 일치하는 배송지 데이터가 존재해야한다") {
            shippingAddressRepository.findByCustomerId(customerId).count() shouldBeGreaterThan 0
        }
    }

    afterEach {
        shippingAddressRepository.deleteAll()
    }
})
