package io.commerce.accountservice.profile

import io.commerce.accountservice.core.ErrorCodeException
import io.commerce.accountservice.fixture.AccountFixture
import io.commerce.accountservice.fixture.agreement
import io.commerce.accountservice.fixture.faker
import io.commerce.accountservice.keycloak.updateEmailAgreed
import io.commerce.accountservice.keycloak.updateSmsAgreed
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing

@DataMongoTest
@ProfileServiceTest
@EnableReactiveMongoAuditing
class ProfileUpdateAgreementTest(
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({

    val customerId = faker.random.nextUUID()
    val agreement = agreement {
        email = true
        sms = true
        privacyTerm = true
        serviceTerm = true
    }

    Given("suspend fun updateAgreement(customerId: String, payload: UpdateAgreementPayload)") {

        When("프로필이 존재하지 않는 경우") {

            afterTest {
                profileRepository.deleteAll()
            }

            Then("throw ErrorCodeException.of(ProfileError.PROFILE_NOT_FOUND)") {
                shouldThrow<ErrorCodeException> {
                    profileService.updateAgreement(AccountFixture.createUser(customerId = customerId))
                }.should {
                    it.errorCode shouldBe ProfileError.PROFILE_NOT_FOUND.code
                    it.reason shouldBe ProfileError.PROFILE_NOT_FOUND.message
                }
            }
        }

        When("프로필 동의 항목 수정 성공한 경우") {
            val user = AccountFixture.createUser(customerId)
            beforeTest {
                profileRepository.save(Profile.of(user))
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 데이터 동의 항목 변경") {
                val updatedUser = user.updateEmailAgreed(agreement.email).updateSmsAgreed(agreement.sms)
                profileService.updateAgreement(updatedUser).shouldNotBeNull().should {
                    it.id shouldNotBe null
                    it.agreement.email shouldBe agreement.email
                    it.agreement.sms shouldBe agreement.sms
                    it.createdAt shouldNotBe null
                    it.updatedAt shouldNotBe null
                }
            }
        }
    }
})
