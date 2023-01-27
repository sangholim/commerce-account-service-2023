package io.commerce.accountservice.base

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.annotation.AliasFor
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Tag(name = "Admin")
@SecurityRequirement(name = "aegis")
@RestController
@RequestMapping("/admin/account", produces = [APPLICATION_JSON_VALUE])
annotation class RestAdminController(
    @get: AliasFor(annotation = RestController::class)
    val value: String = ""
)
