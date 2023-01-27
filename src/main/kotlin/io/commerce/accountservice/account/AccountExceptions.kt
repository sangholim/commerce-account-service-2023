package io.commerce.accountservice.account

import io.commerce.accountservice.core.AbstractErrorCodeException
import org.springframework.http.HttpStatus

class EmailDuplicateException : AbstractErrorCodeException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다", "email_duplicated")

class EmailInvalidException : AbstractErrorCodeException(HttpStatus.BAD_REQUEST, "존재하지 않는 이메일입니다", "email_invalid")

class EmailNotVerifiedException : AbstractErrorCodeException(HttpStatus.BAD_REQUEST, "인증을 완료해주세요", "email_not_verified")

class PhoneNumberNotVerifiedException : AbstractErrorCodeException(HttpStatus.BAD_REQUEST, "인증을 완료해주세요", "phone_number_not_verified")
