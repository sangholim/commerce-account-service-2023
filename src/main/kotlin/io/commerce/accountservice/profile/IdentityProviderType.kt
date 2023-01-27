package io.commerce.accountservice.profile

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 소셜 연동 구분
 */
enum class IdentityProviderType {
    @JsonProperty("naver")
    NAVER,

    @JsonProperty("kakao")
    KAKAO,

    @JsonProperty("facebook")
    FACEBOOK
}
