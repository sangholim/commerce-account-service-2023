package io.commerce.accountservice.account

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 동의 항목 구분
 */
enum class AgreementType {
    @field: JsonProperty("email")
    EMAIL,

    @field: JsonProperty("sms")
    SMS
}
