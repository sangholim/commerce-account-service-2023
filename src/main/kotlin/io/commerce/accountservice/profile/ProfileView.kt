package io.commerce.accountservice.profile

import com.fasterxml.jackson.annotation.JsonFormat
import io.commerce.accountservice.shippingAddress.ShippingAddressView
import java.time.LocalDate

data class ProfileView(
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
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val birthday: LocalDate? = null,

    /**
     * sms 수신 동의
     */
    @Deprecated("replace with agreement.sms", replaceWith = ReplaceWith("agreement.sms"))
    val smsAgreed: Boolean = false,

    /**
     * 이메일 수신 동의
     */
    @Deprecated("replace with agreement.email", replaceWith = ReplaceWith("agreement.email"))
    val emailAgreed: Boolean = false,

    /**
     * 마케팅 동의 항목
     */
    val agreement: AgreementView,

    /**
     * 소셜 미디어 리스트
     */
    val identityProviders: List<IdentityProviderType>? = null,

    /**
     * 배송지 리스트
     */
    val shippingAddresses: List<ShippingAddressView>? = null
) {
    data class AgreementView(
        /**
         * sms 수신 동의
         */
        val sms: Boolean,

        /**
         * 이메일 수신 동의
         */
        val email: Boolean
    )
}

fun Profile.toProfileView(shippingAddresses: List<ShippingAddressView>?): ProfileView =
    ProfileView(
        email = email,
        name = name,
        phoneNumber = phoneNumber,
        birthday = birthday,
        agreement = this.agreement.toAgreementView(),
        smsAgreed = agreement.sms,
        emailAgreed = agreement.email,
        identityProviders = identityProviders,
        shippingAddresses = shippingAddresses
    )

fun Profile.Agreement.toAgreementView(): ProfileView.AgreementView =
    ProfileView.AgreementView(
        sms = sms,
        email = email
    )
