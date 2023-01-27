package io.commerce.accountservice.verification

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import io.commerce.accountservice.mail.MailService
import io.commerce.accountservice.sms.SmsClient
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@VerificationServiceTest
@MockkBeans(
    MockkBean(SmsClient::class),
    MockkBean(MailService::class)
)
@Import(VerificationFacadeService::class)
annotation class VerificationFacadeServiceTest
