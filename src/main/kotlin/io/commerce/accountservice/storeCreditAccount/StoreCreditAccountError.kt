package io.commerce.accountservice.storeCreditAccount

import io.commerce.accountservice.core.BaseError

enum class StoreCreditAccountError(override val message: String) : BaseError {
    ACCOUNT_NOT_FOUND("적립금 계좌를 찾을 수 없습니다")
}
