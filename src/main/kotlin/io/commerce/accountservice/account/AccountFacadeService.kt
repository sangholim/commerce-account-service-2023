package io.commerce.accountservice.account

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.eventStream.CustomerRegisteredSupplier
import io.commerce.accountservice.keycloak.*
import io.commerce.accountservice.profile.ProfileService
import io.commerce.accountservice.profile.ProfileView
import io.commerce.accountservice.profile.toProfileView
import io.commerce.accountservice.shippingAddress.ShippingAddress
import io.commerce.accountservice.shippingAddress.ShippingAddressService
import io.commerce.accountservice.shippingAddress.toShippingAddressView
import io.commerce.accountservice.verification.*
import io.commerce.accountservice.verification.VerificationItem.*
import io.commerce.accountservice.verification.VerificationType.EMAIL
import io.commerce.accountservice.verification.VerificationType.SMS
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service

/**
 * 회원 통합 서비스
 */
@Service
class AccountFacadeService(
    private val keycloakUserService: KeycloakUserService,
    private val verificationFacadeService: VerificationFacadeService,
    private val shippingAddressService: ShippingAddressService,
    private val customerRegisteredSupplier: CustomerRegisteredSupplier,
    private val profileService: ProfileService
) {

    /**
     * 이메일 회원가입
     * @param payload 회원가입 필드 데이터
     */
    suspend fun register(payload: RegisterPayload) {
        // 사용중인 이메일 검사
        if (!isUniqueEmail(payload.email)) throw EmailDuplicateException()
        // 이메일, 휴대폰 번호 인증 여부 확인
        if (!verificationFacadeService.isVerified(REGISTER, payload.email)) throw EmailNotVerifiedException()
        if (!verificationFacadeService.isVerified(REGISTER, payload.phoneNumber)) throw PhoneNumberNotVerifiedException()
        // 회원 생성
        keycloakUserService.create(UserRepresentationFactory.create(payload))
        // 생성된 계정 활성화
        activateAccount(keycloakUserService.findOneByUsername(payload.email)!!)
        // 인증 데이터 제거
        verificationFacadeService.deleteVerification(REGISTER, payload.email)
        verificationFacadeService.deleteVerification(REGISTER, payload.phoneNumber)
    }

    /**
     * 비밀번호 초기화
     * @param payload 비밀번호 필드 데이터
     */
    suspend fun resetPassword(payload: ResetPasswordPayload) = keycloakUserService.findOneByUsername(payload.email)?.let { user ->
        // 이메일 인증 여부 체크
        if (!verificationFacadeService.isVerified(RESET_PASSWORD, user.email)) {
            throw EmailNotVerifiedException()
        }
        // 비밀번호 업데이트
        keycloakUserService.update(user.updatePassword(password = payload.password))
        // 인증 받은 데이터 제거
        verificationFacadeService.deleteVerification(RESET_PASSWORD, user.email)
    } ?: throw EmailInvalidException()

    /**
     * 회원 계정 업데이트
     * @param customerId 고객 번호
     * @param payload 업데이트 필드 데이터
     */
    suspend fun updateProfile(customerId: String, payload: ActivateProfilePayload) =
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            // attribute UPDATE_PROFILE 없는 경우, 예외
            if (!user.profileUpdateRequired()) throw ErrorCodeException.of(AccountError.UPDATE_PROFILE_NOT_EXIST)
            // 활성화 인증 여부
            isVerifiedForActivation(user, payload.email, payload.phoneNumber)
            // 프로필 업데이트
            val updatedUser = user.update(payload)
            keycloakUserService.update(updatedUser)
            activateAccount(updatedUser)
            // 인증 데이터 제거
            verificationFacadeService.deleteVerification(ACTIVATION, payload.email)
            verificationFacadeService.deleteVerification(ACTIVATION, payload.phoneNumber)
        } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)

    /**
     * 프로필 조회
     * @param customerId 고객 번호
     */
    suspend fun getProfile(customerId: String): ProfileView =
        keycloakUserService.findOneByCustomerId(customerId)
            ?.let {
                val shippingAddresses = shippingAddressService.getShippingAddresses(it.id)
                    .map(ShippingAddress::toShippingAddressView).toList()
                profileService.upsertIdentityProviders(it).toProfileView(shippingAddresses)
            }
            ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)

    /**
     * 인증 메세지 발송
     * @param item 인증 항목
     * @param type 인증 타입
     * @param payload 인증 필드 데이터
     */
    suspend fun sendVerificationMessage(
        item: VerificationItem,
        type: VerificationType,
        payload: VerificationPayload,
        customerId: String? = null
    ): VerificationView {
        when (item) {
            REGISTER, PROFILE -> if (type == EMAIL && !isUniqueEmail(payload.key)) throw EmailDuplicateException()
            RESET_PASSWORD -> if (isUniqueEmail(payload.key)) throw EmailInvalidException()
            ACTIVATION -> keycloakUserService.findOneByCustomerId(customerId!!)?.let { user ->
                if (type == EMAIL && user.isActiveEmail(payload.key)) throw Exception()
                if (type == SMS && user.isActivePhoneNumber(payload.key)) throw Exception()
            } ?: throw Exception()
            UPDATE_PASSWORD -> if (!equalsPhoneNumber(customerId!!, payload.key)) throw Exception()
        }
        return verificationFacadeService.sendVerificationMessage(item, type, payload).toVerificationView(type.expiry)
    }

    /**
     * 이메일 업데이트
     * @param customerId 고객 ID
     * @param payload 이메일 필드 데이터
     */
    suspend fun updateEmail(customerId: String, payload: UpdateEmailPayload) {
        // 사용중인 이메일 예외처리
        if (!isUniqueEmail(payload.email)) throw EmailDuplicateException()
        // 인증 받지 않은 이메일은 예외처리
        if (!verificationFacadeService.isVerified(PROFILE, payload.email)) throw EmailNotVerifiedException()
        keycloakUserService.findOneByCustomerId(customerId)
            ?.updateEmail(payload.email)
            ?.run {
                keycloakUserService.update(this)
                profileService.updateEmail(this)
                verificationFacadeService.deleteVerification(PROFILE, payload.email)
            } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)
    }

    /**
     * 휴대폰 번호 업데이트
     * @param customerId 고객 ID
     * @param payload 휴대폰 필드 데이터
     */
    suspend fun updatePhoneNumber(customerId: String, payload: UpdatePhoneNumberPayload) {
        // 인증 받지 않은 이메일은 예외처리
        if (!verificationFacadeService.isVerified(PROFILE, payload.phoneNumber)) throw PhoneNumberNotVerifiedException()
        keycloakUserService.findOneByCustomerId(customerId)
            ?.updatePhoneNumber(payload.phoneNumber)
            ?.run {
                keycloakUserService.update(this)
                profileService.updatePhoneNumber(this)
                verificationFacadeService.deleteVerification(PROFILE, payload.phoneNumber)
            } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)
    }

    /**
     * 활성화 인증
     * @param user 계정 정보
     * @param email 이메일
     * @param phoneNumber 휴대폰 번호
     */
    suspend fun isVerifiedForActivation(user: UserRepresentation, email: String, phoneNumber: String) {
        // 비활성화 이메일은 인증 받지 않으면 예외
        if (!user.isActiveEmail(email) && !verificationFacadeService.isVerified(ACTIVATION, email)) {
            throw EmailNotVerifiedException()
        }
        // 비활성화 휴대폰 번호는 인증 받지 않으면 예외
        if (!user.isActivePhoneNumber(phoneNumber) && !verificationFacadeService.isVerified(ACTIVATION, phoneNumber)) {
            throw PhoneNumberNotVerifiedException()
        }
    }

    /**
     * 프로필 이미지 경로 업데이트
     * @param customerId 고객 번호
     * @param payload 이미지 경로
     */
    fun updateProfileImage(customerId: String, payload: UpdateProfileImagePayload) {
        require(payload.image.isNotEmpty()) { "프로필 이미지 경로가 존재하지 않습니다" }
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            keycloakUserService.update(user.updateImage(payload.image))
        } ?: throw Exception()
    }

    /**
     * 이름 업데이트
     * @param customerId 고객 번호
     * @param payload 이름 필드 데이터
     */
    suspend fun updateName(customerId: String, payload: UpdateNamePayload) {
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            val updatedUser = user.updateName(payload.name)
            keycloakUserService.update(updatedUser)
            profileService.updateName(updatedUser.id, payload.name)
        } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)
    }

    /**
     * 생일 업데이트
     * @param customerId 고객 번호
     * @param payload 생일 필드 데이터
     */
    suspend fun updateBirthday(customerId: String, payload: UpdateBirthdayPayload) {
        keycloakUserService.findOneByCustomerId(customerId)
            ?.updateBirthday(payload.birthday)
            ?.run {
                keycloakUserService.update(this)
                profileService.updateBirthday(this)
            } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)
    }

    /**
     * 비밀번호 재설정
     * @param customerId 고객 번호
     * @param payload 비밀번호 필드 데이터
     */
    suspend fun updatePassword(customerId: String, payload: UpdatePasswordPayload) {
        // 휴대폰 번호 인증 받지 않으면 예외 처리
        if (!verificationFacadeService.isVerified(UPDATE_PASSWORD, payload.phoneNumber)) throw PhoneNumberNotVerifiedException()
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            // 계정의 휴대폰 번호랑 다르면 예외 처리
            if (!user.equalPhoneNumber(payload.phoneNumber)) throw Exception()
            keycloakUserService.update(user.updatePassword(payload.password))
            // 인증 데이터 제거
            verificationFacadeService.deleteVerification(UPDATE_PASSWORD, payload.phoneNumber)
        }
    }

    /**
     * 동의 항목 수정
     * @param customerId 고객 번호
     * @param payload 동의 항목 필드 데이터
     */
    suspend fun updateAgreement(customerId: String, payload: UpdateAgreementPayload) {
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            when (payload.type) {
                AgreementType.EMAIL -> user.updateEmailAgreed(payload.active)
                AgreementType.SMS -> user.updateSmsAgreed(payload.active)
            }
        }?.run {
            keycloakUserService.update(this)
            profileService.updateAgreement(this)
        } ?: throw ErrorCodeException.of(AccountError.ACCOUNT_NOT_FOUND)
    }

    /**
     * 인증 검증
     * @param item 인증 항목
     * @param payload 인증 필드 데이터
     */
    suspend fun checkVerification(item: VerificationItem, payload: VerificationPayload) =
        verificationFacadeService.checkVerification(item, payload)

    /**
     * 사용가능한 이메일 검사
     * @param email 이메일
     */
    private fun isUniqueEmail(email: String): Boolean = keycloakUserService.count(email) <= 0

    /**
     * 활성화 처리
     * (고객 권한 추가, 프로필 데이터 생성)
     *
     * @param user 회원 정보
     */
    suspend fun activateAccount(user: UserRepresentation) {
        // customerRegisteredSupplier.send(user.id)
        keycloakUserService.addCustomerRoleToUser(user.id)
        profileService.registerBy(user)
    }

    /**
     * 활성화 이메일 여부
     * @param email 활성화 이메일 대상
     */
    private fun UserRepresentation.isActiveEmail(email: String): Boolean {
        // 존재하지 않는 계정은 비활성화
        if (isUniqueEmail(email)) return false
        // 다른 계정의 이메일인 경우 예외
        if (!equalEmail(email)) throw EmailDuplicateException()
        // 계정의 이메일과 일치하고, 인증 받은 경우
        return isEmailVerified
    }

    /**
     * 계정 휴대폰 번호 일치 여부
     * @param customerId 고객 번호
     * @param phoneNumber 휴대폰 번호
     */
    private fun equalsPhoneNumber(customerId: String, phoneNumber: String): Boolean =
        keycloakUserService.findOneByCustomerId(customerId)?.equalPhoneNumber(phoneNumber) ?: false
}
