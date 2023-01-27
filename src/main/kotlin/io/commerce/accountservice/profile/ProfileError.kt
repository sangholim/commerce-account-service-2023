package io.commerce.accountservice.profile

import io.commerce.accountservice.core.BaseError

enum class ProfileError(override val message: String) : BaseError {
    PROFILE_ALREADY_EXIST("프로필 정보가 이미 존재합니다"),
    PROFILE_EMAIL_EXIST("사용중인 프로필 이메일입니다"),
    PROFILE_NOT_FOUND("프로필을 찾을수 없습니다")
}
