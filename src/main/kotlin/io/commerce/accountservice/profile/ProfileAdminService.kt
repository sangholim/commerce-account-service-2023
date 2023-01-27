package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import org.springframework.stereotype.Service

@Service
class ProfileAdminService(
    private val profileRepository: ProfileRepository
) {
    /**
     * 프로필 비활성화
     * @param customerId 고객 ID
     */
    suspend fun disableProfile(customerId: String) {
        profileRepository.findByCustomerId(customerId)
            ?.let(Profile::disable)
            ?.run {
                profileRepository.save(this)
            } ?: throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)
    }
}
