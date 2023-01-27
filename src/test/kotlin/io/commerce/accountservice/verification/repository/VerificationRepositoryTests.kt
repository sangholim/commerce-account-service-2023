package io.commerce.accountservice.verification.repository

import io.commerce.accountservice.verification.Verification
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationRepository
import io.commerce.accountservice.verification.VerificationType
import io.commerce.accountservice.fixture.AccountFixture
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest

@DataMongoTest
class VerificationRepositoryTests(
    private val verificationRepository: VerificationRepository
) : DescribeSpec({
    val key = AccountFixture.createUser
    lateinit var verification: Verification

    beforeEach {
        verification =
            verificationRepository.save(Verification.of(VerificationItem.REGISTER, VerificationType.EMAIL.expiry, key))
    }

    describe("save()") {

        it("code가 존재해야한다") {
            verification.code shouldNotBe null
        }
    }

    describe("findByItemAndKey()") {
        it("인증 항목과 인증키가 일치하는 인증 데이터가 존재해야한다") {
            verificationRepository.findByItemAndKey(VerificationItem.REGISTER, key) shouldNotBe null
        }
    }

    afterEach {
        verificationRepository.deleteAll()
    }
})
