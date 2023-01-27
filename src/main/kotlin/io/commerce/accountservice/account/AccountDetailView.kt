package io.commerce.accountservice.account

import com.fasterxml.jackson.annotation.JsonInclude
import io.commerce.accountservice.keycloak.*
import io.commerce.accountservice.shippingAddress.ShippingAddress
import io.commerce.accountservice.shippingAddress.ShippingAddressView
import io.commerce.accountservice.shippingAddress.toShippingAddressView
import org.keycloak.representations.idm.UserRepresentation

/**
 * 프로필 조회
 */
@Deprecated("replace with ProfileView", replaceWith = ReplaceWith("ProfileView"))
data class AccountDetailView(

    /**
     * 이메일
     */
    val email: String,

    /**
     * 이름
     */
    val name: String,

    /**
     * 휴대폰 번호
     */
    val phoneNumber: String,

    /**
     * 생년월일
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val birthday: String? = null,

    /**
     * sms 수신 동의
     */
    val smsAgreed: Boolean = false,

    /**
     * 이메일 수신 동의
     */
    val emailAgreed: Boolean = false,

    /**
     * 프로필 이미지
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val image: String? = null,

    /**
     * 소셜 미디어들
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val identityProviders: List<String>? = null,

    /**
     * 계정 조치
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val requiredAction: String? = null,

    /**
     * 배송지들
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val shippingAddresses: List<ShippingAddressView>? = null
)

/**
 * 프로필 조회 결과 객체로 변환
 * @param shippingAddresses 배송지 리스트
 */
fun UserRepresentation.toAccountDetailView(shippingAddresses: List<ShippingAddress>?): AccountDetailView =
    AccountDetailView(
        email = email,
        name = name!!,
        phoneNumber = phoneNumber!!,
        birthday = birthday,
        smsAgreed = smsAgreed.toBoolean(),
        emailAgreed = emailAgreed.toBoolean(),
        image = image,
        identityProviders = federatedIdentities?.map { IdentityProvider.create(it).type },
        requiredAction = attributes["requiredAction"]?.get(0),
        shippingAddresses = shippingAddresses?.map(ShippingAddress::toShippingAddressView)
    )
