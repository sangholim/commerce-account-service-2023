package io.commerce.accountservice.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.servers.ServerVariable
import io.swagger.v3.oas.models.servers.ServerVariables
import org.bson.types.ObjectId
import org.springdoc.core.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("oas")
@SecurityScheme(
    name = "aegis",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@Configuration
class OpenAPIConfig {
    /**
     * SpringDoc Global Configuration
     */
    init {
        SpringDocUtils.getConfig()
            .replaceWithSchema(ObjectId::class.java, StringSchema())
    }

    @Bean
    fun publicAPI(): OpenAPI = OpenAPI().apply {
        info = Info()
            .title("Account API")
            .version("v1.0.0-edge")
        servers = listOf(
            Server()
                .url("{host}")
                .variables(
                    ServerVariables()
                        .addServerVariable(
                            "host",
                            ServerVariable().apply {
                                enum = listOf("api.commerce.io", "api.commerce.co.kr")
                                default = "api.commerce.io"
                            }
                        )
                )
        )
    }
}
