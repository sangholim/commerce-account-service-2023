package io.commerce.accountservice.core

interface BaseError {
    /**
     * 에러 이름
     */
    val name: String

    /**
     * 에러 메세지
     */
    val message: String

    /**
     * 에러 코드
     */
    val code: String
        get() = name.lowercase()
}
