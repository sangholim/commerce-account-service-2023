package io.commerce.accountservice.storeCreditAccount

import org.bson.types.ObjectId
import javax.validation.constraints.NotBlank

/**
 * 적립금 계좌
 */
data class StoreCreditAccount(
    /**
     * 적립금 계좌 ID
     */
    val id: ObjectId,

    /**
     * 고객 ID
     */
    @field: NotBlank
    val customerId: String,

    /**
     * 사용 가능한 총액
     */
    val balance: Int,

    /**
     * 다음달 소멸 예정 금액
     */
    val amountToExpire: Int
)
