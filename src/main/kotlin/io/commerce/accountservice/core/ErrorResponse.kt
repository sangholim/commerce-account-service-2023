package io.commerce.accountservice.core

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatus
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ResponseStatusException
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ErrorResponse private constructor(
    val timestamp: Long = Instant.now().toEpochMilli(),

    /**
     * Http Status Code
     */
    val status: Int,

    /**
     * Http Status Reason
     */
    val error: String,

    /**
     * 에러 코드
     */
    val code: String? = null,

    /**
     * 에러 내용
     */
    val message: String? = null,

    /**
     * Input field 에러 리스트
     */
    val fields: List<SimpleFieldError> = emptyList()
) {
    companion object {
        fun from(exception: WebExchangeBindException) = ErrorResponse(
            status = exception.status.value(),
            error = exception.status.reasonPhrase,
            message = "잘못된 요청입니다",
            fields = exception.bindingResult.fieldErrors.map { it.toCustomFieldError() }
        )

        fun from(exception: ErrorFieldException) = ErrorResponse(
            status = exception.status.value(),
            error = exception.status.reasonPhrase,
            message = "잘못된 요청입니다",
            fields = exception.fields
        )

        fun from(exception: AbstractErrorCodeException) = ErrorResponse(
            status = exception.status.value(),
            error = exception.status.reasonPhrase,
            code = exception.errorCode,
            message = exception.reason
        )

        fun from(exception: ResponseStatusException) = ErrorResponse(
            status = exception.status.value(),
            error = exception.status.reasonPhrase,
            message = exception.reason
        )

        fun ofBadRequest() = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = "잘못된 요청입니다"
        )

        fun ofInternal() = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
        )
    }
}
