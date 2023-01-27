package io.commerce.accountservice.profile

import io.commerce.accountservice.fixture.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.shouldForExactly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import java.time.Instant

@DataMongoTest
@ProfileQueryServiceTest
@EnableReactiveMongoAuditing
class ProfileQueryServiceGetAllFilteredTest(
    private val profileQueryService: ProfileQueryService,
    private val profileRepository: ProfileRepository
) : BehaviorSpec({
    Given("suspend fun getAllFiltered(adminFilterCriteria: AdminProfileFilterCriteria): Page<Profile>") {
        When("필터링 프로필 생성일 범위에 해당하지 않는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 리스트 존재하지 않는다") {
                val criteria = adminProfileFilterCriteria {
                    createdAtRange = listOf(
                        Instant.now().minusSeconds(200),
                        Instant.now().minusSeconds(100)
                    )
                    identityProviders = emptySet()
                }

                profileQueryService.getAllFiltered(criteria).content.size shouldBe 0
            }
        }

        When("필터링 조건에 소셜 정보가 없는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.NAVER)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test2@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.FACEBOOK)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 리스트에는 'KAKAO' 'NAVER' 'FACEBOOK' 중 1개는 반드시 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    identityProviders = emptySet()
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.identityProviders.shouldNotBeNull() shouldContain IdentityProviderType.KAKAO
                    }.shouldForExactly(1) {
                        it.identityProviders.shouldNotBeNull() shouldContain IdentityProviderType.NAVER
                    }.shouldForExactly(1) {
                        it.identityProviders.shouldNotBeNull() shouldContain IdentityProviderType.FACEBOOK
                    }
            }
        }
        When("필터링 'sms 수신 동의' 필드가 없는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.sms = false
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.NAVER)
                        this.agreement = agreement {
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 리스트에는 sms 수신동의 여부 'true' 'false' 중 1개는 반드시 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    agreement.sms = null
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.agreement.sms shouldBe false
                    }.shouldForExactly(1) {
                        it.agreement.sms shouldBe true
                    }
            }
        }
        When("필터링 'email 수신 동의' 필드가 없는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.agreement = agreement {
                            this.email = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 리스트에는 email 수신동의 여부 'true' 'false' 중 1개는 반드시 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    agreement.email = null
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.agreement.email shouldBe false
                    }.shouldForExactly(1) {
                        it.agreement.email shouldBe true
                    }
            }
        }
        When("필터링 '활성화' 필드가 없는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = false
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.NAVER)
                        this.agreement = agreement {
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("프로필 리스트에는 활성화 필드 'true' 'false' 중 1개는 반드시 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    enabled = null
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.enabled shouldBe false
                    }.shouldForExactly(1) {
                        it.enabled shouldBe true
                    }
            }
        }
        When("필터링 모든 필드가 존재하는 경우") {
            beforeTest {
                val profiles = listOf(
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test1@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.NAVER)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = false
                        this.email = "test2@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test3@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = false
                            this.sms = true
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    },
                    profile {
                        this.customerId = faker.random.nextUUID()
                        this.enabled = true
                        this.email = "test4@test.com"
                        this.emailVerified = true
                        this.name = AccountFixture.name
                        this.phoneNumber = AccountFixture.normalPhoneNumber
                        this.phoneNumberVerified = true
                        this.identityProviders = listOf(IdentityProviderType.KAKAO)
                        this.agreement = agreement {
                            this.email = true
                            this.sms = false
                            this.serviceTerm = true
                            this.privacyTerm = true
                        }
                        this.orderCount = 0
                    }
                )
                profileRepository.saveAll(profiles).collect()
            }

            afterTest {
                profileRepository.deleteAll()
            }

            Then("활성화 필드 'true' 인 프로필 데이터 1개 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    this.identityProviders = setOf(IdentityProviderType.KAKAO)
                    this.enabled = true
                    this.agreement.sms = true
                    this.agreement.email = true
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.enabled shouldBe true
                    }
            }

            Then("email 수신 동의 필드 'true' 인 프로필 데이터 1개 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    this.identityProviders = setOf(IdentityProviderType.KAKAO)
                    this.enabled = true
                    this.agreement.sms = true
                    this.agreement.email = true
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.agreement.email shouldBe true
                    }
            }

            Then("sms 수신 동의 필드 'true' 인 프로필 데이터 1개 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    this.identityProviders = setOf(IdentityProviderType.KAKAO)
                    this.enabled = true
                    this.agreement.sms = true
                    this.agreement.email = true
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.agreement.sms shouldBe true
                    }
            }

            Then("'KAKAO' 로 연동된 프로필 데이터 1개 존재한다") {
                val criteria = adminProfileFilterCriteria {
                    this.identityProviders = setOf(IdentityProviderType.KAKAO)
                    this.enabled = true
                    this.agreement.sms = true
                    this.agreement.email = true
                }

                profileQueryService.getAllFiltered(criteria).content
                    .shouldForExactly(1) {
                        it.identityProviders.shouldNotBeNull() shouldContain IdentityProviderType.KAKAO
                    }
            }
        }
    }
})
