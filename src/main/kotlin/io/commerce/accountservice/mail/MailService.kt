package io.commerce.accountservice.mail

import io.commerce.accountservice.verification.Verification
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import java.nio.charset.StandardCharsets
import java.util.*
import javax.mail.internet.MimeMessage

@Service
class MailService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass)
    private val ko: Locale = Locale.forLanguageTag("ko")
    private val from: String = "contact@commerce.co.kr"
    private val verificationSubject: String = "커머스 이메일 인증하기"

    /**
     * 인증 메일 발송
     * @param verification 인증 데이터
     */
    fun sendVerificationMessage(verification: Verification) {
        val context = Context(ko).apply {
            this.setVariable("code", verification.code)
            this.setVariable("expiredAt", verification.expiredAt.atZone(TimeZone.getTimeZone("Asia/Seoul").toZoneId()))
        }
        sendEmail(verification.key, verificationSubject, templateEngine.process("verification_email", context))
    }

    /**
     * 인증 메일 헤더,바디 생성
     * @param to 수신자
     * @param subject 제목
     * @param body 본문
     */
    private fun sendEmail(to: String, subject: String, body: String) = try {
        val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()
        val message = MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name())
        message.setTo(to)
        message.setFrom(from)
        message.setSubject(subject)
        message.setText(body, true)
        javaMailSender.send(mimeMessage)
    } catch (e: Exception) {
        log.warn("Email could not be sent to user '{}'", to, e)
    }
}
