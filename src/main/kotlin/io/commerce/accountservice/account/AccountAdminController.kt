package io.commerce.accountservice.account

import io.commerce.accountservice.base.RestAdminController
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@RestAdminController
class AccountAdminController(
    private val accountAdminService: AccountAdminService
) {
    /**
     * 관리자 권한 회원 비활성화
     */
    @Operation(summary = "관리자 권한 회원 비활성화")
    @PostMapping("/{customerId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun disableAccount(@PathVariable customerId: String) {
        accountAdminService.disableAccount(customerId)
    }
}
