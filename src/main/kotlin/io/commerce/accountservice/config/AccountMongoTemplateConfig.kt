package io.commerce.accountservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.ConstraintViolationException

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = [
        "io.commerce.accountservice.shippingAddress",
        "io.commerce.accountservice.profile",
        "io.commerce.accountservice.verification"
    ]
)
class AccountMongoTemplateConfig {
    /**
     * ### `javax.validation 기반` Entity Validator
     * `entity`가 `database`에 저장되기 전에 validation 체크를 수행하며,
     * validation 실패시 [ConstraintViolationException] 발생
     */
    @Bean
    fun validatingMongoEventListener(
        localValidatorFactoryBean: LocalValidatorFactoryBean
    ): ValidatingMongoEventListener = ValidatingMongoEventListener(localValidatorFactoryBean)
}
