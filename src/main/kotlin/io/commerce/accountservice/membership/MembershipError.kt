package io.commerce.accountservice.membership

import io.commerce.accountservice.core.BaseError

enum class MembershipError(override val message: String) : BaseError {
    MEMBERSHIP_NOT_FOUND("회원 등급이 존재하지 않습니다")
}
