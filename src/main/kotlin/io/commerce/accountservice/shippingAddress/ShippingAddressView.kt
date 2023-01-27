package io.commerce.accountservice.shippingAddress

import org.bson.types.ObjectId

/**
 * 배송지 조회 결과
 */
data class ShippingAddressView(

    /**
     * 배송지 ID
     */
    val id: ObjectId,

    /**
     * 배송지명
     */
    val name: String? = null,

    /**
     * 수령인
     */
    val recipient: String,

    /**
     * 수령인 연락처
     */
    val primaryPhoneNumber: String,

    /**
     * 수령인 연락처2
     */
    val secondaryPhoneNumber: String? = null,

    /**
     * 우편 번호
     */
    val zipCode: String,

    /**
     * 배송지 주소
     */
    val line1: String,

    /**
     * 배송지 주소 상세
     */
    val line2: String? = null,

    /**
     * 기본 주소지
     */
    val primary: Boolean
)

fun ShippingAddress.toShippingAddressView() = ShippingAddressView(
    id = id!!,
    name = name,
    recipient = recipient,
    primaryPhoneNumber = primaryPhoneNumber,
    secondaryPhoneNumber = secondaryPhoneNumber,
    zipCode = zipCode,
    line1 = line1,
    line2 = line2,
    primary = primary
)
