package io.commerce.accountservice.sms

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "sms", ignoreUnknownFields = false)
data class SmsProperties(
    val baseUrl: String,
    val sender: String,
    val userId: String,
    val key: String
)
