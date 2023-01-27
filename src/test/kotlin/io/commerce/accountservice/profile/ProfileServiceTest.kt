package io.commerce.accountservice.profile

import io.commerce.accountservice.config.AccountMongoTemplateConfig
import org.springframework.context.annotation.Import
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import(
    ProfileService::class,
    LocalValidatorFactoryBean::class,
    AccountMongoTemplateConfig::class
)
annotation class ProfileServiceTest
