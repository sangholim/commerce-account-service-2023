package io.commerce.accountservice.storeCreditAcount

import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.storeCreditAccount
import io.commerce.accountservice.storeCreditAccount.StoreCreditAccountRepository
import io.commerce.accountservice.storeCreditAccount.StoreCreditAccountService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import

@DataMongoTest
@Import(value = [StoreCreditAccountService::class])
class StoreCreditAccountServiceGetViewByTest(
    private val storeCreditAccountRepository: StoreCreditAccountRepository,
    private val storeCreditAccountService: StoreCreditAccountService

) : BehaviorSpec({

    val customerId = faker.random.nextUUID()

    Given("적립금 계좌 조회하기") {
        When("적립금 계좌가 없는 경우") {
            afterTest {
                storeCreditAccountRepository.deleteAll()
            }

            Then("적립금 계좌는 null 이다") {
                storeCreditAccountService.getViewBy(customerId)
                    .shouldBeNull()
            }
        }

        When("적립금 계좌가 있는 경우") {
            beforeTest {
                storeCreditAccountRepository.save(
                    storeCreditAccount {
                        this.customerId = customerId
                    }
                )
            }

            afterTest {
                storeCreditAccountRepository.deleteAll()
            }

            Then("잔고, 다음달 소멸 예정 금액 값이 조회된다") {
                storeCreditAccountService.getViewBy(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.balance shouldNotBe null
                        it.amountToExpire shouldNotBe null
                    }
            }
        }
    }
})
