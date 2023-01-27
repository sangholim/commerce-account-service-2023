package io.commerce.accountservice.keycloak

import io.commerce.accountservice.account.ActivateProfilePayload
import io.commerce.accountservice.account.AgreementPayload
import io.commerce.accountservice.account.RegisterPayload
import io.commerce.accountservice.keycloak.KeycloakConstants.UPDATE_PROFILE
import io.commerce.accountservice.legacy.LegacyActivateProfilePayload
import io.commerce.accountservice.profile.IdentityProviderType
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import java.time.LocalDate
import java.util.*

object UserRepresentationFactory {
    /**
     * Keycloak 유저 계정 생성
     * @param payload 회원 가입 데이터 필드
     */
    fun create(payload: RegisterPayload): UserRepresentation = UserRepresentation().apply {
        this.email = payload.email
        this.username = email
        this.isEnabled = true
        this.isEmailVerified = true
        // Issue: 'keycloak admin client' attributes 형태는 String List<String> 값으로 받는다.
        this.attributes = mapOf(
            "name" to listOf(payload.name),
            "phoneNumber" to listOf(payload.phoneNumber),
            "phoneNumberVerified" to listOf("true"),
            "emailAgreed" to listOf(payload.agreement.email.toString()),
            "smsAgreed" to listOf(payload.agreement.sms.toString()),
            "serviceTermAgreed" to listOf(payload.agreement.serviceTerm.toString()),
            "privacyTermAgreed" to listOf(payload.agreement.privacyTerm.toString())
        )

        this.credentials = listOf(
            CredentialRepresentation().apply {
                this.type = "password"
                this.value = payload.password
            }
        )
    }
}

/**
 * 이관 계정 비밀번호
 */
val UserRepresentation.legacyPassword: String?
    get() = attributes["v1_password"]?.get(0)

/**
 * 계정 이름
 */
val UserRepresentation.name: String?
    get() = attributes["name"]?.get(0)

/**
 * 계정 생일
 */
val UserRepresentation.birthday: String?
    get() = attributes["birthday"]?.get(0)

/**
 * 계정 유효한 생일
 */
val UserRepresentation.validBirthday: LocalDate?
    get() = runCatching { LocalDate.parse(birthday) }.getOrNull()

/**
 * 이메일 수신 여부
 */
val UserRepresentation.emailAgreed: String?
    get() = attributes["emailAgreed"]?.get(0)

/**
 * sms 수신 여부
 */
val UserRepresentation.smsAgreed: String?
    get() = attributes["smsAgreed"]?.get(0)

/**
 * 서비스 이용 동의
 */
val UserRepresentation.serviceTermAgreed: String?
    get() = attributes["serviceTermAgreed"]?.get(0)

/**
 * 개인 정보 이용 동의
 */
val UserRepresentation.privacyTermAgreed: String?
    get() = attributes["privacyTermAgreed"]?.get(0)

/**
 * 소셜 연동 리스트
 */
val UserRepresentation.identityProviders: List<IdentityProviderType>?
    get() {
        if (federatedIdentities.isNullOrEmpty()) return null
        return federatedIdentities.map { IdentityProviderType.valueOf(it.identityProvider.uppercase(Locale.getDefault())) }
    }

/**
 * 계정 이미지
 */
val UserRepresentation.image: String?
    get() = attributes["image"]?.get(0)

/**
 * 계정 휴대폰 번호
 */
val UserRepresentation.phoneNumber: String?
    get() = attributes["phoneNumber"]?.get(0)

/**
 * 계정 휴대폰 번호 인증 여부
 */
val UserRepresentation.phoneNumberVerified: String?
    get() = attributes["phoneNumberVerified"]?.get(0)

/**
 * 휴대폰 번호 활성화 여부
 * @param phoneNumber 활성화 휴대폰 번호 대상
 */
fun UserRepresentation.isActivePhoneNumber(phoneNumber: String): Boolean =
    equalPhoneNumber(phoneNumber) && phoneNumberVerified.toBoolean()

/**
 * 프로필 이미지 경로 업데이트
 * @param image 이미지 경로
 */
fun UserRepresentation.updateImage(image: String): UserRepresentation = apply {
    this.attributes["image"] = listOf(image)
}

/**
 * 프로필 이름 업데이트
 * @param name 이름
 */
fun UserRepresentation.updateName(name: String): UserRepresentation = apply {
    attributes["name"] = listOf(name)
}

/**
 * 프로필 생일 업데이트
 * @param birthday 생일
 */
fun UserRepresentation.updateBirthday(birthday: String): UserRepresentation = apply {
    attributes["birthday"] = listOf(birthday)
}

/**
 * 프로필 이메일 수신 동의 업데이트
 * @param active 활성화 여부
 */
fun UserRepresentation.updateEmailAgreed(active: Boolean): UserRepresentation = apply {
    this.attributes["emailAgreed"] = listOf(active.toString())
}

/**
 * 프로필 SMS 수신 동의 업데이트
 * @param active 활성화 여부
 */
fun UserRepresentation.updateSmsAgreed(active: Boolean): UserRepresentation = apply {
    this.attributes["smsAgreed"] = listOf(active.toString())
}

/**
 * 휴대폰 번호 업데이트
 * @param phoneNumber 휴대폰 번호
 */
fun UserRepresentation.updatePhoneNumber(phoneNumber: String): UserRepresentation = apply {
    this.attributes["phoneNumber"] = listOf(phoneNumber)
    this.attributes["phoneNumberVerified"] = listOf("true")
}

/**
 * 이메일 업데이트
 * @param email 이메일
 */
fun UserRepresentation.updateEmail(email: String): UserRepresentation = apply {
    this.username = email
    this.email = email
    this.isEmailVerified = true
}

/**
 * 비밀번호 재설정
 * @param password 비밀번호
 */
fun UserRepresentation.updatePassword(password: String): UserRepresentation = this.apply {
    this.credentials = listOf(
        CredentialRepresentation().apply {
            this.type = "password"
            this.value = password
        }
    )
}

/**
 * 계정 비활성화
 */
fun UserRepresentation.disable(): UserRepresentation = this.apply {
    this.isEnabled = false
}

/**
 * 계정과 동일한 이메일 체크
 * @param email 이메일
 */
fun UserRepresentation.equalEmail(email: String): Boolean = this.email.equals(email, true)

/**
 * 계정과 동일한 휴대폰 번호 체크
 * @param phoneNumber 휴대폰 번호
 */
fun UserRepresentation.equalPhoneNumber(phoneNumber: String): Boolean = this.phoneNumber.equals(phoneNumber, true)

/**
 * UPDATE_PROFILE attribute 값 존재 체크
 */
fun UserRepresentation.profileUpdateRequired(): Boolean =
    attributes["requiredAction"]?.get(0) == UPDATE_PROFILE

/**
 * 계정 활성화 업데이트
 * @param payload 계정 활성화 데이터 필드
 */
fun UserRepresentation.update(payload: ActivateProfilePayload): UserRepresentation =
    updateActivation(payload.email, payload.name, payload.phoneNumber, payload.agreement)

/**
 * v1 계정 활성화 업데이트
 * @param payload v1 계정 활성화 데이터 필드
 */
fun UserRepresentation.update(payload: LegacyActivateProfilePayload): UserRepresentation = let { user ->
    user.attributes["v1_password"] = null
    user.credentials = listOf(
        CredentialRepresentation().apply {
            this.type = "password"
            this.value = payload.password
        }
    )
    user.updateActivation(payload.email, payload.name, payload.phoneNumber, payload.agreement)
}

/**
 * 활성화 업데이트
 * @param email 이메일
 * @param name 이름
 * @param phoneNumber 휴대폰 번호
 * @param agreement 동의 항목
 */
private fun UserRepresentation.updateActivation(
    email: String,
    name: String,
    phoneNumber: String,
    agreement: AgreementPayload
): UserRepresentation = apply {
    this.email = email
    this.username = this.email
    this.isEmailVerified = true
    this.attributes["name"] = listOf(name)
    this.attributes["phoneNumber"] = listOf(phoneNumber)
    this.attributes["phoneNumberVerified"] = listOf("true")
    this.attributes["emailAgreed"] = listOf(agreement.email.toString())
    this.attributes["smsAgreed"] = listOf(agreement.sms.toString())
    this.attributes["serviceTermAgreed"] = listOf(agreement.serviceTerm.toString())
    this.attributes["privacyTermAgreed"] = listOf(agreement.privacyTerm.toString())
    this.attributes["requiredAction"] = null
}
