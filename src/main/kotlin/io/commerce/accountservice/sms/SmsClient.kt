package io.commerce.accountservice.sms

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.commerce.accountservice.verification.Verification
import io.commerce.accountservice.verification.VerificationSendFailException
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

class SmsClient(
    private val smsProperties: SmsProperties,
    private val objectMapper: ObjectMapper
) {
    private val log: Logger = LoggerFactory.getLogger(SmsClient::class.java)

    private val connectionProvider = ConnectionProvider.builder("sms")
        .maxConnections(500)
        .maxIdleTime(Duration.ofSeconds(20))
        .maxLifeTime(Duration.ofSeconds(60))
        .pendingAcquireTimeout(Duration.ofSeconds(60))
        .evictInBackground(Duration.ofSeconds(120)).build()

    private val smsClient = WebClient.builder()
        .clientConnector(ReactorClientHttpConnector(HttpClient.create(connectionProvider)))
        .baseUrl(smsProperties.baseUrl).build()

    /**
     * 인증 메시지 발송
     * @param verification 인증 정보
     */
    suspend fun sendVerificationMessage(verification: Verification) =
        AligoPayload.of(smsProperties, verification).toLinkedMultiValueMap().let { payload ->
            log.info("verification sms recipient: ${verification.key}")
            val aligoView = sendMessage(payload)
            if (aligoView.fail()) {
                log.error("[resultCode: ${aligoView.resultCode}] [message: ${aligoView.message}]")
                throw VerificationSendFailException()
            }
        }

    /**
     * 메시지 발송
     * @param payload 요청 바디
     */
    private suspend fun sendMessage(payload: LinkedMultiValueMap<String, String>): AligoView = smsClient
        .post()
        .bodyValue(payload)
        .awaitExchange<String> { response ->
            if (response.statusCode().isError) throw response.createException().awaitFirst()
            response.awaitBody()
        }.let(objectMapper::readValue)

    private fun AligoPayload.toLinkedMultiValueMap(): LinkedMultiValueMap<String, String> =
        objectMapper.convertValue(this, object : TypeReference<Map<String, String>>() {})
            .let { LinkedMultiValueMap<String, String>().apply { setAll(it) } }
}
