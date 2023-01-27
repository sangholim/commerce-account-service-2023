package io.commerce.accountservice.mail

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.icegreen.greenmail.util.ServerSetup.PROTOCOL_SMTP
import io.commerce.accountservice.fixture.MailMessageFixture
import io.commerce.accountservice.verification.Verification
import io.commerce.accountservice.verification.VerificationItem
import io.commerce.accountservice.verification.VerificationType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import javax.mail.Message

@SpringBootTest
@Import(TestChannelBinderConfiguration::class)
class SendVerificationMessageIT(
    private val mailService: MailService,
    private val mailProperties: MailProperties
) : BehaviorSpec({
    val smtpServer = ServerSetup(mailProperties.port, null, PROTOCOL_SMTP)
    val greenMail = GreenMail(smtpServer).withConfiguration(
        GreenMailConfiguration().withUser(
            mailProperties.username,
            mailProperties.password
        )
    )
    val to = io.commerce.accountservice.fixture.AccountFixture.createUser
    val expiry = VerificationType.EMAIL.expiry

    beforeEach {
        greenMail.start()
    }

    Given("인증 메일 발송") {
        val verification = Verification.of(VerificationItem.REGISTER, expiry, to)
        Then("헤더: From, To, Subject 값이 존재하고 아래와 일치한다") {
            mailService.sendVerificationMessage(verification)
            val message = greenMail.getReceivedMessagesForDomain(to)[0]

            message.from.first().toString() shouldBe MailMessageFixture.FROM
            message.getRecipients(Message.RecipientType.TO).first().toString() shouldBe to
            message.subject shouldBe MailMessageFixture.VERIFICATION_SUBJECT
        }
    }

    afterEach {
        greenMail.stop()
    }
})
