package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.keycloak.*
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProfileService(
    private val profileRepository: ProfileRepository
) {
    /**
     * 프로필 생성
     *
     * @param userRepresentation aegis 회원 정보
     */
    @Transactional
    suspend fun registerBy(userRepresentation: UserRepresentation): Profile {
        if (profileRepository.existsByCustomerId(userRepresentation.id)) throw ErrorCodeException.of(ProfileError.PROFILE_ALREADY_EXIST)
        return profileRepository.save(Profile.of(userRepresentation))
    }

    /**
     * profile collection 내에 customerId가 존재하는 경우, 소셜 리스트 업데이트
     * profile collection 내에 customerId가 없는 경우, 프로필 생성
     *
     * @param userRepresentation aegis 회원 정보
     */
    suspend fun upsertIdentityProviders(userRepresentation: UserRepresentation): Profile =
        profileRepository.upsertIdentityProviders(Profile.of(userRepresentation))

    /**
     * 이름 수정
     *
     * @param customerId 고객 ID
     * @param name 이름
     */
    @Transactional
    suspend fun updateName(customerId: String, name: String): Profile =
        getBy(customerId).updateName(name)
            .let { profileRepository.save(it) }

    /**
     * 마케팅 동의 항목 수정
     *
     * @param userRepresentation aegis 회원 정보
     */
    @Transactional
    suspend fun updateAgreement(userRepresentation: UserRepresentation): Profile =
        getBy(userRepresentation.id).updateAgreement(
            userRepresentation.emailAgreed.toBoolean(),
            userRepresentation.smsAgreed.toBoolean()
        ).let { profileRepository.save(it) }

    /**
     * 생일 수정
     *
     * @param userRepresentation aegis 회원 정보
     */
    @Transactional
    suspend fun updateBirthday(userRepresentation: UserRepresentation): Profile =
        getBy(userRepresentation.id).updateBirthday(
            LocalDate.parse(userRepresentation.birthday)
        ).let { profileRepository.save(it) }

    /**
     * 휴대폰 번호 수정
     *
     * @param userRepresentation aegis 회원 정보
     */
    @Transactional
    suspend fun updatePhoneNumber(userRepresentation: UserRepresentation): Profile =
        getBy(userRepresentation.id)
            .updatePhoneNumber(userRepresentation.phoneNumber!!, userRepresentation.phoneNumberVerified.toBoolean())
            .let { profileRepository.save(it) }

    /**
     * 이메일 수정
     *
     * @param userRepresentation aegis 회원 정보
     */
    @Transactional
    suspend fun updateEmail(userRepresentation: UserRepresentation): Profile {
        if (profileRepository.existsByEmail(userRepresentation.email)) throw ErrorCodeException.of(ProfileError.PROFILE_EMAIL_EXIST)
        return getBy(userRepresentation.id)
            .updateEmail(userRepresentation.email, userRepresentation.isEmailVerified)
            .let { profileRepository.save(it) }
    }

    /**
     * 프로필 조회
     *
     * @param customerId 고객 ID
     */
    private suspend fun getBy(customerId: String): Profile =
        profileRepository.findByCustomerId(customerId) ?: throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)
}
