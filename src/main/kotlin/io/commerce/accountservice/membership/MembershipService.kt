package io.commerce.accountservice.membership

import org.springframework.stereotype.Service

@Service
class MembershipService(
    private val membershipRepository: MembershipRepository
) {
    /**
     * 활성화 회원 등급 조회
     *
     * @param customerId 고객 ID
     */
    suspend fun getViewBy(customerId: String): MembershipView? =
        membershipRepository.findFirstByCustomerIdAndStatus(customerId, MembershipStatus.ACTIVE)?.toMembershipView()
}
