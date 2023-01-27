package io.commerce.accountservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor

@Configuration
class ValidatorConfig {
    /**
     * Spring이 제공하는 Bean Validation Provider 설정 주입
     *
     * **참고**: [Configuring a Bean Validation Provider](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation-spring)
     */
    @Bean
    fun validator() = LocalValidatorFactoryBean()

    /**
     * Spring-driven Method Validation 활성화
     *
     * **참고**: [Spring-driven Method Validation](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation-spring-method)
     */
    @Bean
    fun validationPostProcessor() = MethodValidationPostProcessor()
}
