package io.commerce.accountservice.verification

import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import(VerificationService::class)
annotation class VerificationServiceTest
