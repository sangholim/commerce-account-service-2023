package io.commerce.accountservice.shippingAddress

import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.Instant

/**
 * 배송지 정보
 */
@Document
data class ShippingAddress(
    /**
     * 배송지 id
     */
    @MongoId
    val id: ObjectId? = null,

    /**
     * 고객 번호
     */
    @Indexed
    val customerId: String,

    /**
     * 배송지명
     */
    val name: String?,

    /**
     * 수령인
     */
    val recipient: String,

    /**
     * 휴대폰 번호
     */
    val primaryPhoneNumber: String,

    /**
     * 연락처
     */
    val secondaryPhoneNumber: String?,

    /**
     * 우편 번호
     */
    val zipCode: String,

    /**
     * 주소
     */
    val line1: String,

    /**
     * 나머지 주소
     */
    val line2: String?,

    /**
     * 기본 배송지 사용 여부
     */
    val primary: Boolean,

    /**
     * 배송지 생성일
     */
    @CreatedDate
    val createdAt: Instant? = null,

    /**
     * 배송지 수정일
     */
    @LastModifiedDate
    val updatedAt: Instant? = null
) {

    /**
     * 기본 배송지 활성화
     */
    fun enablePrimary(): ShippingAddress = copy(primary = true)

    /**
     * 기본 배송지 비활성화
     */
    fun disablePrimary(): ShippingAddress = copy(primary = false)

    /**
     * 기본 배송지 여부
     */
    fun isPrimary(): Boolean = primary

    /**
     * 배송지 업데이트
     * @param payload 배송지 필드 정보
     */
    fun update(payload: ShippingAddressPayload): ShippingAddress = copy(
        name = payload.name,
        recipient = payload.recipient,
        primaryPhoneNumber = payload.primaryPhoneNumber,
        secondaryPhoneNumber = payload.secondaryPhoneNumber,
        zipCode = payload.zipCode,
        line1 = payload.line1,
        line2 = payload.line2,
        primary = payload.primary
    )

    companion object {

        /**
         * 배송지 생성
         * @param customerId 고객 번호
         * @param payload 배송지 필드 정보
         */
        fun of(customerId: String, payload: ShippingAddressPayload): ShippingAddress = ShippingAddress(
            name = payload.name,
            recipient = payload.recipient,
            primaryPhoneNumber = payload.primaryPhoneNumber,
            secondaryPhoneNumber = payload.secondaryPhoneNumber,
            zipCode = payload.zipCode,
            line1 = payload.line1,
            line2 = payload.line2,
            primary = payload.primary,
            customerId = customerId
        )

        /**
         * 기본 배송지 생성
         * @param customerId 고객 번호
         * @param payload 배송지 필드 정보
         */
        fun ofPrimary(customerId: String, payload: ShippingAddressPayload): ShippingAddress = ShippingAddress(
            name = payload.name,
            recipient = payload.recipient,
            primaryPhoneNumber = payload.primaryPhoneNumber,
            secondaryPhoneNumber = payload.secondaryPhoneNumber,
            zipCode = payload.zipCode,
            line1 = payload.line1,
            line2 = payload.line2,
            primary = true,
            customerId = customerId
        )
    }
}
