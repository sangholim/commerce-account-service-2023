package io.commerce.accountservice.account.accountFacadeService

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.eventStream.CustomerRegisteredSupplier
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.profile.ProfileServiceTest
import io.commerce.accountservice.shippingAddress.shippingAddressService.ShippingAddressServiceTest
import io.commerce.accountservice.verification.VerificationFacadeServiceTest
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@VerificationFacadeServiceTest
@ShippingAddressServiceTest
@ProfileServiceTest
@MockkBeans(
    MockkBean(CustomerRegisteredSupplier::class),
    MockkBean(KeycloakUserService::class)
)
@Import(AccountFacadeService::class)
annotation class AccountFacadeServiceTest
