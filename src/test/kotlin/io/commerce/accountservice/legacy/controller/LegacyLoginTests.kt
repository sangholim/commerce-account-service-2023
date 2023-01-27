package io.commerce.accountservice.legacy.controller

import com.ninjasquad.springmockk.MockkBean
import io.commerce.accountservice.fixture.LegacyAccountFixture
import io.commerce.accountservice.legacy.LegacyAccountFacadeService
import io.commerce.accountservice.legacy.LegacyBadRequestException
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.coEvery
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestChannelBinderConfiguration::class)
class LegacyLoginTests(
    @MockkBean
    private val legacyAccountFacadeService: LegacyAccountFacadeService,
    private val webTestClient: WebTestClient

) : DescribeSpec({
    describe("v1 회원 계정 이관") {
        val payload = LegacyAccountFixture.createLegacyAccountLoginPayload()
        val client = webTestClient
            .post().uri("/account/legacy/migrate")
            .contentType(MediaType.APPLICATION_JSON)

        context("keycloak attributes - requiredAction 값이 MIGRATION_ACCOUNT 존재하지 않는 경우") {
            coEvery { legacyAccountFacadeService.login(payload) } throws LegacyBadRequestException()
            it("400 Bad Request") {
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        context("keycloak attributes - v1 비밀번호와 다른 경우") {
            coEvery { legacyAccountFacadeService.login(payload) } throws LegacyBadRequestException()
            it("400 Bad Request") {
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isBadRequest
            }
        }

        context("v1 회원 계정 이관 완료한 경우") {
            coEvery { legacyAccountFacadeService.login(payload) } returns Unit
            it("204 No Content") {
                client.bodyValue(payload)
                    .exchange()
                    .expectStatus().isNoContent
            }
            it("Response Body: empty") {
                client.bodyValue(payload)
                    .exchange()
                    .expectBody().isEmpty
            }
        }
    }
})
