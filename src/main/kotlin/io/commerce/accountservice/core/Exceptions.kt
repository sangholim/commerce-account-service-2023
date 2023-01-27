package io.commerce.accountservice.core

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class BadRequestException(message: String = "잘못된 요청입니다") : ResponseStatusException(HttpStatus.BAD_REQUEST, message)

class NotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND)

class InternalServerException : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
