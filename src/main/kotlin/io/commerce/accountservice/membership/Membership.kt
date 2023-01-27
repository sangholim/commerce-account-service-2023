package io.commerce.accountservice.membership

import org.bson.types.ObjectId
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.NotBlank

/**
 * 회원등급
 */
data class Membership(
    /**
     * 회원 등급 ID
     */
    val id: ObjectId,

    /**
     * 고객 ID
     */
    @field: NotBlank
    val customerId: String,

    /**
     * 회원등급 구분
     */
    val type: MembershipType,

    /**
     * 회원등급 상태
     */
    val status: MembershipStatus,

    /**
     * 구매 확정(실적금액)시 적립율
     */
    @field:DecimalMin(value = "0", inclusive = false)
    @field:DecimalMax(value = "1", inclusive = false)
    @field:Digits(integer = 1, fraction = 2)
    val creditRewardRate: Double
)
