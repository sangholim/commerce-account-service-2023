package io.commerce.accountservice.profile

import io.commerce.accountservice.base.RestAdminController
import io.commerce.accountservice.core.PagedView
import io.commerce.accountservice.core.toPagedView
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.flow.Flow
import org.springdoc.api.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import javax.validation.Valid

@RestAdminController
class AdminProfileController(
    private val profileQueryService: ProfileQueryService
) {
    /**
     * 필터 기반 프로필 리스트을 조회합니다.
     */
    @Operation(summary = "관리자 전체 프로필 조회")
    @GetMapping("/profiles", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun allFilteredProfiles(
        @Valid @ParameterObject
        adminFilterCriteria: AdminProfileFilterCriteria
    ): PagedView<Profile> = profileQueryService.getAllFiltered(adminFilterCriteria).toPagedView()

    /**
     * 주어진 키워드로 프로필을 검색합니다.
     */
    @Operation(summary = "관리자 프로필 통합 검색")
    @GetMapping("/profiles/search", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun searchProfiles(
        @Valid @ParameterObject
        criteria: AdminSearchCriteria
    ): Flow<Profile> = profileQueryService.searchAll(criteria)
}
