package io.commerce.accountservice.eventStream

import io.commerce.accountservice.eventStream.CustomerRegisteredEventPayload
import io.commerce.accountservice.fixture.AccountFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.messaging.converter.CompositeMessageConverter

@SpringBootTest
@Import(TestChannelBinderConfiguration::class)
class CustomerRegisteredSupplierTest(
    private val target: OutputDestination,
    private val converter: CompositeMessageConverter,
    private val customerRegisteredSupplier: CustomerRegisteredSupplier
) : DescribeSpec({
    describe("send(customerId)") {
        val customerId = AccountFixture.id

        beforeEach {
            customerRegisteredSupplier.send(customerId)
        }

        it("OK") {
            val sourceMessage = target.receive(3000, CustomerRegisteredSupplier.BINDING)
            val payload = converter.fromMessage(
                sourceMessage,
                CustomerRegisteredEventPayload::class.java
            ) as CustomerRegisteredEventPayload
            payload.should {
                it.customerId shouldBe customerId
            }
        }
    }
})
