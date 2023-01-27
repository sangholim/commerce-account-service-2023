package io.commerce.accountservice.core

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ErrorFieldException(
    val fields: List<SimpleFieldError>
) : ResponseStatusException(HttpStatus.BAD_REQUEST)
