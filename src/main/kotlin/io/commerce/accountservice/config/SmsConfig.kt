package io.commerce.accountservice.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.commerce.accountservice.sms.SmsClient
import io.commerce.accountservice.sms.SmsProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(SmsProperties::class)
class SmsConfig(
    private val smsProperties: SmsProperties,
    private val objectMapper: ObjectMapper
) {
    @Bean
    fun smsClient() = SmsClient(smsProperties, objectMapper)
}
