package io.commerce.accountservice.account

import io.commerce.accountservice.profile.ProfileView
import io.commerce.accountservice.verification.VerificationItem.*
import io.commerce.accountservice.verification.VerificationPayload
import io.commerce.accountservice.verification.VerificationType
import io.commerce.accountservice.verification.VerificationType.EMAIL
import io.commerce.accountservice.verification.VerificationType.SMS
import io.commerce.accountservice.verification.VerificationView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@Tag(name = "Account")
@RestController
@RequestMapping(
    value = ["account"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class AccountController(
    private val accountFacadeService: AccountFacadeService
) {

    /**
     * 회원 정보 유효성 검사
     * @param payload 질의 필드
     */
    @Operation(summary = "회원 정보 유효성 검사")
    @PostMapping("/validation", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun validation(
        @Valid @RequestBody
        payload: AccountValidationPayload
    ) {}

    /**
     * 이메일 회원 가입
     * @param payload 회원가입 필드 데이터
     */
    @Operation(summary = "이메일 회원 가입")
    @PostMapping("/register", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(CREATED)
    suspend fun register(
        @Valid @RequestBody
        payload: RegisterPayload
    ) = accountFacadeService.register(payload)

    /**
     * 회원 가입시 이메일/sms 인증
     * @param type 인증 수단 (email/sms)
     * @param payload 인증 필드 데이터
     */
    @Operation(
        summary = "회원 가입시 이메일/sms 인증",
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(schema = Schema(implementation = VerificationView::class))]
            ),
            ApiResponse(responseCode = "204")
        ]
    )
    @PostMapping("/register/verify/{type}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun registerVerify(
        @PathVariable type: String,
        @RequestBody payload: VerificationPayload
    ): ResponseEntity<Any> {
        val verificationType = VerificationType.valueOf(type.uppercase())
        payload.validation(verificationType)
        if (payload.code == null) {
            val verificationView = accountFacadeService.sendVerificationMessage(REGISTER, verificationType, payload)
            return ResponseEntity(verificationView, CREATED)
        }

        accountFacadeService.checkVerification(REGISTER, payload)
        return ResponseEntity(NO_CONTENT)
    }

    /**
     * 비밀번호 초기화 이메일 인증
     * @param payload 인증 필드 데이터
     */
    @Operation(
        summary = "비밀번호 초기화 이메일 인증",
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(schema = Schema(implementation = VerificationView::class))]
            ),
            ApiResponse(responseCode = "204")
        ]
    )
    @PostMapping("/reset-password/verify/email", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun resetPasswordVerify(
        @RequestBody payload: VerificationPayload
    ): ResponseEntity<Any> {
        payload.validation(EMAIL)
        if (payload.code == null) {
            val verificationView = accountFacadeService.sendVerificationMessage(RESET_PASSWORD, EMAIL, payload)
            return ResponseEntity(verificationView, CREATED)
        }

        accountFacadeService.checkVerification(RESET_PASSWORD, payload)
        return ResponseEntity(NO_CONTENT)
    }

    /**
     * 비밀번호 초기화
     * @param payload 비밀번호 초기화 필드 데이터
     */
    @Operation(summary = "비밀번호 초기화")
    @PostMapping("/reset-password", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun resetPassword(
        @Valid @RequestBody
        payload: ResetPasswordPayload
    ) = accountFacadeService.resetPassword(payload)

    /**
     * 로그인 이후 프로필 업데이트
     * @param customerId 계정 ID
     * @param payload 프로필 업데이트 request body
     */
    @Operation(summary = "로그인 이후 프로필 업데이트", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/activation/{customerId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun activation(@PathVariable customerId: UUID, @Valid @RequestBody payload: ActivateProfilePayload) =
        accountFacadeService.updateProfile(customerId.toString(), payload)

    /**
     * 로그인 이후 프로필 업데이트 이메일/SMS 번호 인증
     * @param type 인증 수단 (email/sms)
     * @param payload 인증 필드 데이터
     */
    @Operation(
        summary = "로그인 이후 프로필 업데이트 이메일/휴대폰 번호 인증",
        security = [SecurityRequirement(name = "aegis")],
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(schema = Schema(implementation = VerificationView::class))]
            ),
            ApiResponse(responseCode = "204")
        ]
    )
    @PostMapping("/activation/verify/{type}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun activationVerify(
        @PathVariable type: String,
        @RequestBody payload: VerificationPayload,
        authentication: Authentication
    ): ResponseEntity<Any> {
        val verificationType = VerificationType.valueOf(type.uppercase())
        payload.validation(verificationType)
        if (payload.code == null) {
            val verificationView = accountFacadeService.sendVerificationMessage(ACTIVATION, verificationType, payload, authentication.name)
            return ResponseEntity(verificationView, CREATED)
        }

        accountFacadeService.checkVerification(ACTIVATION, payload)
        return ResponseEntity(NO_CONTENT)
    }

    /**
     * 프로필 조회
     * @param customerId 고객 번호
     */
    @Operation(summary = "프로필 조회", security = [SecurityRequirement(name = "aegis")])
    @GetMapping("/profile/{customerId}")
    suspend fun profile(@PathVariable customerId: UUID): ProfileView =
        accountFacadeService.getProfile(customerId.toString())

    /**
     * 마이페이지 수정 이메일/휴대폰 번호 인증
     * @param type 인증 수단 (email/sms)
     * @param payload 인증 필드 데이터
     */
    @Operation(
        summary = "마이페이지 수정 이메일/휴대폰 번호 인증",
        security = [SecurityRequirement(name = "aegis")],
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(schema = Schema(implementation = VerificationView::class))]
            ),
            ApiResponse(responseCode = "204")
        ]
    )
    @PostMapping("/profile/verify/{type}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun profileVerify(
        @PathVariable type: String,
        @RequestBody payload: VerificationPayload,
        authentication: Authentication
    ): ResponseEntity<Any> {
        val verificationType = VerificationType.valueOf(type.uppercase())
        payload.validation(verificationType)
        if (payload.code == null) {
            val verificationView = accountFacadeService.sendVerificationMessage(PROFILE, verificationType, payload)
            return ResponseEntity(verificationView, CREATED)
        }

        accountFacadeService.checkVerification(PROFILE, payload)
        return ResponseEntity(NO_CONTENT)
    }

    /**
     * 마이페이지 휴대폰 번호 수정
     * @param customerId 고객 ID
     * @param payload 휴대폰 번호 수정 request body
     */
    @Operation(summary = "마이페이지 휴대폰 번호 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/phone-number", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfilePhoneNumber(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdatePhoneNumberPayload
    ) = accountFacadeService.updatePhoneNumber(customerId.toString(), payload)

    /**
     * 마이페이지 이메일 수정
     * @param customerId 고객 ID
     * @param payload 이메일 수정 request body
     */
    @Operation(summary = "마이페이지 이메일 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/email", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfileEmail(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdateEmailPayload
    ) = accountFacadeService.updateEmail(customerId.toString(), payload)

    /**
     * 마이페이지 이름 수정
     * @param customerId 고객 ID
     * @param payload 이름 수정 필드 데이터
     */
    @Operation(summary = "마이페이지 이름 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/name", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfileName(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdateNamePayload
    ) = accountFacadeService.updateName(customerId.toString(), payload)

    /**
     * 마이페이지 생일 수정
     * @param customerId 고객 ID
     * @param payload 생일 수정 필드 데이터
     */
    @Operation(summary = "마이페이지 생일 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/birthday", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfileBirthday(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdateBirthdayPayload
    ) = accountFacadeService.updateBirthday(customerId.toString(), payload)

    /**
     * 마이페이지 이미지 경로 수정
     * @param customerId 고객 ID
     * @param payload 이미지 경로
     */
    @Operation(summary = "마이페이지 이미지 경로 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/image", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    fun updateProfileImage(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdateProfileImagePayload
    ) = accountFacadeService.updateProfileImage(customerId.toString(), payload)

    /**
     * 마이페이지 비밀번호 재설정 휴대폰 번호 인증
     * @param payload 인증 필드 데이터
     */
    @Operation(
        summary = "마이페이지 비밀번호 재설정 휴대폰 번호 인증",
        security = [SecurityRequirement(name = "aegis")],
        responses = [
            ApiResponse(
                responseCode = "201",
                content = [Content(schema = Schema(implementation = VerificationView::class))]
            ),
            ApiResponse(responseCode = "204")
        ]
    )
    @PostMapping("/update-password/verify/sms", consumes = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updatePasswordVerify(
        @RequestBody payload: VerificationPayload,
        authentication: Authentication
    ): ResponseEntity<Any> {
        payload.validation(SMS)
        if (payload.code == null) {
            val verificationView = accountFacadeService.sendVerificationMessage(UPDATE_PASSWORD, SMS, payload, authentication.name)
            return ResponseEntity(verificationView, CREATED)
        }

        accountFacadeService.checkVerification(UPDATE_PASSWORD, payload)
        return ResponseEntity(NO_CONTENT)
    }

    /**
     * 마이페이지 비밀번호 재설정
     * @param customerId 고객 ID
     * @param payload 비밀번호 재설정 필드
     */
    @Operation(summary = "마이페이지 비밀번호 재설정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/password", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfilePassword(
        @PathVariable customerId: UUID,
        @Valid @RequestBody
        payload: UpdatePasswordPayload
    ) = accountFacadeService.updatePassword(customerId.toString(), payload)

    /**
     * 마이페이지 마케팅 동의 항목 수정
     * @param customerId 고객 ID
     * @param payload 동의 항목 필드
     */
    @Operation(summary = "마이페이지 마케팅 동의 항목 수정", security = [SecurityRequirement(name = "aegis")])
    @PutMapping("/profile/{customerId}/agreement", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(NO_CONTENT)
    suspend fun updateProfileAgreement(
        @PathVariable customerId: UUID,
        @RequestBody
        payload: UpdateAgreementPayload
    ) = accountFacadeService.updateAgreement(customerId.toString(), payload)
}
