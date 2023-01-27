package io.commerce.accountservice.storeCreditAccount

import org.springframework.stereotype.Service

@Service
class StoreCreditAccountService(
    private val storeCreditAccountRepository: StoreCreditAccountRepository
) {
    /**
     * 적립금 계좌 조회
     * @param customerId 고객 번호
     */
    suspend fun getViewBy(customerId: String): StoreCreditAccountView? =
        storeCreditAccountRepository.findByCustomerId(customerId)
}
