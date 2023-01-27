package io.commerce.accountservice.legacy

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Tag(name = "Account")
@RestController
@RequestMapping(
    value = ["account/legacy"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class LegacyAccountController(
    private val legacyAccountFacadeService: LegacyAccountFacadeService
) {

    /**
     * v1 회원 계정 이관
     * 400 에러는 클라이언트에서 별도 처리하지 않는다.
     * @param payload v1 계정 로그인 요청 정보
     */
    @Operation(summary = "v1 회원 계정 이관")
    @PostMapping("/migrate", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun legacyLogin(@RequestBody payload: LegacyAccountLoginPayload) = legacyAccountFacadeService.login(payload)

    /**
     * v1 회원 계정 로그인 이후 프로필 업데이트
     * @param customerId 회원 고유 번호
     * @param payload 프로필 업데이트 필드
     */
    @Operation(summary = "v1 회원 계정 로그인 이후 프로필 업데이트", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/activation/{customerId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun legacyActivation(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: LegacyActivateProfilePayload
    ) = legacyAccountFacadeService.updateProfile(customerId.toString(), payload)
}
