package io.commerce.accountservice.shippingAddress.shippingAddressService

import io.commerce.accountservice.shippingAddress.ShippingAddressService
import org.springframework.context.annotation.Import

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Import(ShippingAddressService::class)
annotation class ShippingAddressServiceTest
