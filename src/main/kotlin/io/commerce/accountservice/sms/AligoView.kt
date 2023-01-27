package io.commerce.accountservice.sms

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 알리고 요청 결과
 */
data class AligoView(
    @JsonProperty(value = "result_code")
    var resultCode: Int = 0,

    @JsonProperty(value = "message")
    var message: String? = null,

    @JsonProperty(value = "msg_id")
    var msgId: Int = 0,

    @JsonProperty(value = "success_cnt")
    var successCnt: Int = 0,

    @JsonProperty(value = "error_cnt")
    var errorCnt: Int = 0,

    @JsonProperty(value = "msg_type")
    var msgType: String? = null
) {
    fun fail(): Boolean {
        return resultCode != 1
    }
}
