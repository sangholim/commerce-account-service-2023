package io.commerce.accountservice.fixture

import io.commerce.accountservice.shippingAddress.ShippingAddress
import io.commerce.accountservice.shippingAddress.ShippingAddressPayload
import org.apache.commons.lang3.RandomStringUtils
import org.bson.types.ObjectId
import java.time.Instant

object ShippingAddressFixture {

    private const val length = 10

    fun generateShippingAddresses(customerId: String, count: Int): List<ShippingAddress> {
        val list = mutableListOf<ShippingAddress>()
        for (i in 1..count) {
            list.add(ShippingAddress.of(customerId = customerId, payload = createShippingAddressDTO()).copy(id = ObjectId.get()))
        }
        return list
    }

    fun createShippingAddress(customerId: String, payload: ShippingAddressPayload) = ShippingAddress.of(customerId, payload).copy(id = ObjectId.get())

    fun createPrimaryShippingAddressPayload() = createShippingAddressPayload().copy(primary = true)

    fun createShippingAddressPayload() = ShippingAddressPayload(
        name = RandomStringUtils.randomAlphabetic(length),
        primary = false,
        line1 = RandomStringUtils.randomAlphabetic(length),
        line2 = RandomStringUtils.randomAlphabetic(length),
        recipient = RandomStringUtils.randomAlphabetic(length),
        zipCode = RandomStringUtils.randomNumeric(length),
        primaryPhoneNumber = AccountFixture.normalPhoneNumber,
        secondaryPhoneNumber = AccountFixture.normalPhoneNumber
    )

    fun createShippingAddressDTO() = ShippingAddressPayload(
        name = RandomStringUtils.randomAlphabetic(length),
        primary = false,
        line1 = RandomStringUtils.randomAlphabetic(length),
        line2 = RandomStringUtils.randomAlphabetic(length),
        recipient = RandomStringUtils.randomAlphabetic(length),
        zipCode = RandomStringUtils.randomNumeric(length),
        primaryPhoneNumber = AccountFixture.normalPhoneNumber,
        secondaryPhoneNumber = AccountFixture.normalPhoneNumber
    )

    fun createInvalidShippingAddressDTO() = ShippingAddressPayload(
        name = RandomStringUtils.randomAlphabetic(21),
        primary = false,
        line1 = "",
        line2 = RandomStringUtils.randomAlphabetic(length),
        recipient = RandomStringUtils.randomAlphabetic(21),
        zipCode = "",
        primaryPhoneNumber = RandomStringUtils.randomNumeric(21),
        secondaryPhoneNumber = RandomStringUtils.randomNumeric(21)
    )
}

inline fun shippingAddress(block: ShippingAddressFixtureBuilder.() -> Unit = {}) =
    ShippingAddressFixtureBuilder().apply(block).build()

class ShippingAddressFixtureBuilder {
    var id: ObjectId? = null
    var customerId: String = ""
    var name: String = ""
    var recipient: String = ""
    var primaryPhoneNumber: String = ""
    var secondaryPhoneNumber: String = ""
    var zipCode: String = ""
    var line1: String = ""
    var line2: String = ""
    var primary: Boolean = false
    var createdAt: Instant? = null
    var updatedAt: Instant? = null
    fun build() = ShippingAddress(
        id,
        customerId,
        name,
        recipient,
        primaryPhoneNumber,
        secondaryPhoneNumber,
        zipCode,
        line1,
        line2,
        primary,
        createdAt,
        updatedAt
    )
}
