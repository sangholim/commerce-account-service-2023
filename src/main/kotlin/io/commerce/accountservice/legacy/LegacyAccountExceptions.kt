package io.commerce.accountservice.legacy

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class LegacyBadRequestException : ResponseStatusException(HttpStatus.BAD_REQUEST)
