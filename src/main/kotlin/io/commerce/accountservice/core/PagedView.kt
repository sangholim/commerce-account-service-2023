package io.commerce.accountservice.core

import org.springframework.data.domain.Page

/**
 * Pagination Response View
 */
data class PagedView<T>(
    /**
     * 데이터
     */
    val content: List<T>,

    /**
     * Pagination 정보
     */
    val page: PageMetadata
) {
    data class PageMetadata(
        /**
         * 총 페이지 개수
         */
        val totalPages: Int,

        /**
         * 총 데이터 개수
         */
        val totalElements: Long,

        /**
         * 마지막 페이지 여부
         */
        val last: Boolean,

        /**
         * 페이지 사이즈
         */
        val size: Int,

        /**
         * 현재 페이지 번호
         */
        val number: Int,

        /**
         * 첫번째 페이지 여부
         */
        val first: Boolean
    )
}

fun <T> Page<T>.toPagedView() = PagedView(
    content = content,
    page = PagedView.PageMetadata(
        totalPages = totalPages,
        totalElements = totalElements,
        last = isLast,
        size = size,
        number = number,
        first = isFirst
    )
)
