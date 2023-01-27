package io.commerce.accountservice.membership

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 회원 등급 상태
 */
enum class MembershipStatus {
    /**
     * 등급 만료
     */
    @JsonProperty("expired")
    EXPIRED,

    /**
     * 현재 등급
     */
    @JsonProperty("active")
    ACTIVE,

    /**
     * 예상 등급
     */
    @JsonProperty("expected")
    EXPECTED
}
