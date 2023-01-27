package io.commerce.accountservice.legacy.legacyAccountFacadeService

import io.commerce.accountservice.account.accountFacadeService.AccountFacadeServiceTest
import io.commerce.accountservice.legacy.LegacyAccountFacadeService
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@AccountFacadeServiceTest
@Import(LegacyAccountFacadeService::class)
annotation class LegacyAccountFacadeServiceTest
