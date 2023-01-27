package io.commerce.accountservice.legacy

import io.commerce.accountservice.account.AccountFacadeService
import io.commerce.accountservice.core.NotFoundException
import io.commerce.accountservice.keycloak.KeycloakUserService
import io.commerce.accountservice.keycloak.legacyPassword
import io.commerce.accountservice.keycloak.update
import io.commerce.accountservice.keycloak.updatePassword
import io.commerce.accountservice.verification.VerificationFacadeService
import io.commerce.accountservice.verification.VerificationItem.ACTIVATION
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * v1 이관 유저 처리 로직 관리 클래스
 */
@Service
class LegacyAccountFacadeService(
    private val keycloakUserService: KeycloakUserService,
    private val verificationFacadeService: VerificationFacadeService,
    private val accountFacadeService: AccountFacadeService
) {
    private val legacyPasswordEncoder = LegacyPasswordEncoder()
    private val legacyUserRequiredAction = "MIGRATE_ACCOUNT"

    /**
     * 이관 유저 로그인
     * @param payload 로그인 필드 정보
     */
    fun login(payload: LegacyAccountLoginPayload) =
        keycloakUserService.findOneByUsername(payload.email)?.let { user ->
            // requiredAction MIGRATION_ACCOUNT 존재하지 않으면 예외처리
            if (!user.existMigrateAccount()) throw LegacyBadRequestException()
            // 비밀번호가 일치하지 않는 경우 예외 처리
            if (!legacyPasswordEncoder.matches(payload.password, user.legacyPassword)) throw LegacyBadRequestException()
            // 로그인 처리가 끝나면 실제 비밀번호로 업데이트
            keycloakUserService.update(user.updatePassword(payload.password))
        } ?: throw LegacyBadRequestException()

    /**
     * 프로필 업데이트
     * @param customerId 고객 번호
     * @param payload 프로필 업데이트 필드
     */
    suspend fun updateProfile(customerId: String, payload: LegacyActivateProfilePayload) =
        keycloakUserService.findOneByCustomerId(customerId)?.let { user ->
            // requiredAction MIGRATION_ACCOUNT 존재하지 않으면 예외처리
            if (!user.existMigrateAccount()) throw LegacyBadRequestException()
            // 비활성화 이메일은 인증 받지 않으면 예외
            accountFacadeService.isVerifiedForActivation(user, payload.email, payload.phoneNumber)
            val updatedUser = user.update(payload)
            keycloakUserService.update(updatedUser)
            accountFacadeService.activateAccount(updatedUser)
            // 인증 데이터 제거
            verificationFacadeService.deleteVerification(ACTIVATION, payload.email)
            verificationFacadeService.deleteVerification(ACTIVATION, payload.phoneNumber)
        } ?: throw NotFoundException()

    /**
     * MIGRATE_ACCOUNT 존재 여부 체크
     */
    private fun UserRepresentation.existMigrateAccount(): Boolean = attributes["requiredAction"]?.get(0) == legacyUserRequiredAction

    /**
     * 이관 계정 비밀번호 인코더
     */
    internal class LegacyPasswordEncoder : PasswordEncoder {
        private val privateKey = "abchobby#@!".toByteArray(StandardCharsets.UTF_8)
        private val algorithm = "HmacSHA256"

        override fun encode(rawPassword: CharSequence): String {
            val secretKey = SecretKeySpec(privateKey, algorithm)
            val hmacSha256 = Mac.getInstance(algorithm)
            hmacSha256.init(secretKey)
            val bytes: ByteArray = hmacSha256.doFinal(rawPassword.toString().toByteArray(StandardCharsets.UTF_8))
            val builder = StringBuilder()
            for (b in bytes) {
                builder.append(String.format("%02x", b))
            }
            return builder.toString()
        }

        override fun matches(rawPassword: CharSequence, encodedPassword: String?): Boolean {
            return encode(rawPassword) == encodedPassword
        }
    }
}
