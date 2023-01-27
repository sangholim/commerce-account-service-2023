package io.commerce.accountservice.profile

import java.time.LocalDate

fun Profile.updateName(name: String) = copy(name = name)

fun Profile.updateAgreement(email: Boolean, sms: Boolean) =
    copy(agreement = agreement.copy(email = email, sms = sms))

fun Profile.updateBirthday(birthday: LocalDate) = copy(birthday = birthday)

fun Profile.updatePhoneNumber(phoneNumber: String, phoneNumberVerified: Boolean): Profile =
    copy(phoneNumber = phoneNumber, phoneNumberVerified = phoneNumberVerified)

fun Profile.updateEmail(email: String, emailVerified: Boolean): Profile =
    copy(email = email, emailVerified = emailVerified)

fun Profile.disable(): Profile =
    copy(enabled = false)

/**
 * 마케팅 동의항목 이메일 키 필드
 */
val agreementEmailKey: String
    get() = Profile::agreement.name + "." + Profile.Agreement::email.name

/**
 * 마케팅 동의항목 SMS 키 필드
 */
val agreementSmsKey: String
    get() = Profile::agreement.name + "." + Profile.Agreement::sms.name
