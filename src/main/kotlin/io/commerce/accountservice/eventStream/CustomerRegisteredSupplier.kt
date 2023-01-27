package io.commerce.accountservice.eventStream

import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class CustomerRegisteredSupplier(
        private val streamBridge: StreamBridge
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val BINDING = "customer-registered"
    }

    /**
     * topic: customer-registered 로 이벤트 생성
     * @param customerId 고객 번호
     */
    fun send(customerId: String) {
        try {
            val payload = MessageBuilder
                    .withPayload(CustomerRegisteredEventPayload.from(customerId))
                    .setHeader("customerId", customerId)
                    .build()
            streamBridge.send(BINDING, payload)
        } catch (e: Exception) {
            log.error("$BINDING event 전송에 실패하였습니다", e)
        }
    }
}
