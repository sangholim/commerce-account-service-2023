package io.commerce.accountservice.account.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import io.commerce.accountservice.account.AccountAdminService
import io.commerce.accountservice.keycloak.KeycloakAdminService
import io.commerce.accountservice.profile.ProfileAdminServiceTest
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@ProfileAdminServiceTest
@MockkBeans(
    MockkBean(KeycloakAdminService::class)
)
@Import(AccountAdminService::class)
annotation class AccountAdminServiceTest
