package io.commerce.accountservice.core

import org.springframework.http.HttpStatus

class ErrorCodeException private constructor(reason: String, errorCode: String) :
    AbstractErrorCodeException(HttpStatus.BAD_REQUEST, reason, errorCode) {
    companion object {
        fun of(error: BaseError) = ErrorCodeException(error.message, error.code)
    }
}
