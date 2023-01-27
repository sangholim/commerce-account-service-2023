package io.commerce.accountservice.storeCreditAccount

/**
 * 적립금 계좌 응답 클래스
 */
data class StoreCreditAccountView(
    /**
     * 사용 가능한 총액
     */
    val balance: Int,

    /**
     * 다음달 소멸 예정 금액
     */
    val amountToExpire: Int
)


fun StoreCreditAccount.toStoreCreditAccountView() = StoreCreditAccountView(
    balance = this.balance,
    amountToExpire = this.amountToExpire
)