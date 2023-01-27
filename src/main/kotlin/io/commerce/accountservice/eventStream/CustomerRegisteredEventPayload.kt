package io.commerce.accountservice.eventStream

import javax.validation.constraints.NotBlank

/**
 * topic: customer-registered
 * 이벤트 생성/구독 데이터 필드
 */
data class CustomerRegisteredEventPayload(
    /**
     * 고객 번호
     */
    @field: NotBlank
    val customerId: String
) {
    companion object {
        fun from(customerId: String) =
            CustomerRegisteredEventPayload(customerId)
    }
}
