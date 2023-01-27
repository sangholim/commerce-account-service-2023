package io.commerce.accountservice.shippingAddress

import io.commerce.accountservice.validation.ValidationConstants.PATTERN_EMPTY
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_MOBILE_NUMBER
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_PHONE_NUMBER
import io.commerce.accountservice.validation.ValidationConstants.PATTERN_SHIPPING_ADDRESS_NAME_RECIPIENT
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

/**
 * 배송지 추가/수정
 */
data class ShippingAddressPayload(

    /**
     * 배송지명
     */
    @field: Pattern(regexp = "$PATTERN_EMPTY|$PATTERN_SHIPPING_ADDRESS_NAME_RECIPIENT", message = "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요.")
    val name: String? = null,

    /**
     * 수령인
     */
    @field: Pattern(regexp = PATTERN_SHIPPING_ADDRESS_NAME_RECIPIENT, message = "한글, 영문, 숫자로 구성되고, 20자 이내인지 확인해주세요.")
    val recipient: String,

    /**
     * 휴대폰 번호
     */
    @field: Pattern(regexp = PATTERN_MOBILE_NUMBER, message = "올바르지 않은 형식입니다")
    val primaryPhoneNumber: String,

    /**
     * 연락처
     */
    @field: Pattern(regexp = "$PATTERN_EMPTY|$PATTERN_PHONE_NUMBER", message = "올바르지 않은 형식입니다")
    val secondaryPhoneNumber: String? = null,

    /**
     * 우편 번호
     */
    @field: NotBlank(message = "필수 정보입니다")
    val zipCode: String,

    /**
     * 배송지 주소
     */
    @field: NotBlank(message = "필수 정보입니다")
    val line1: String,

    /**
     * 배송지 주소 상세
     */
    val line2: String? = null,

    /**
     * 기본 주소지
     */
    val primary: Boolean = false
)
