package io.commerce.accountservice.core

import org.springframework.validation.FieldError

data class SimpleFieldError(
    val field: String,
    val message: String?
)

fun FieldError.toCustomFieldError() = SimpleFieldError(
    field = field,
    message = defaultMessage
)
