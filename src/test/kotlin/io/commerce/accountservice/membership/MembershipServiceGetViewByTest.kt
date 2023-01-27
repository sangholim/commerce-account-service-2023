package io.commerce.accountservice.membership

import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.fixture.membership
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import

@DataMongoTest
@Import(value = [MembershipService::class])
class MembershipServiceGetViewByTest(
    private val membershipRepository: MembershipRepository,
    private val membershipService: MembershipService
) : BehaviorSpec({

    val customerId = faker.random.nextUUID()

    Given("활성화 회원 등급 조회") {
        When("회원 등급이 존재하지 않는 경우") {
            Then("회원 등급은 nul 이다") {
                membershipService.getViewBy(customerId).shouldBeNull()
            }
        }

        When("회원 등급 조회 성공한 경우") {
            beforeTest {
                membershipRepository.save(
                    membership {
                        this.customerId = customerId
                        this.type = MembershipType.MATE
                        this.status = MembershipStatus.ACTIVE
                        this.creditRewardRate = 0.01
                    }
                )
            }

            afterTest {
                membershipRepository.deleteAll()
            }
            Then("type: 메이트, 적립률: 0.01 인 회원 등급 조회") {
                membershipService.getViewBy(customerId)
                    .shouldNotBeNull()
                    .should {
                        it.name shouldBe MembershipType.MATE.label
                        it.creditRewardRate shouldBe 0.01
                    }
            }
        }
    }
})
