package io.commerce.accountservice.account

import io.commerce.accountservice.core.BaseError

enum class AccountError(override val message: String) : BaseError {
    ACCOUNT_NOT_FOUND("계정을 찾을수 없습니다"),
    UPDATE_PROFILE_NOT_EXIST("UPDATE_PROFILE 값이 존재하지 않습니다")
}
