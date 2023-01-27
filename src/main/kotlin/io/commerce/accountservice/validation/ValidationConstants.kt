package io.commerce.accountservice.validation

object ValidationConstants {
    const val PATTERN_PASSWORD: String =
        "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\\-_\\.!@#\$%^&*()+|\\\\=~`<>,?/])[A-Za-z\\d\\-_\\.!@#\$%^&*()+|\\\\=~`<>,?/]{8,36}\$"
    const val PATTERN_EMAIL: String = "^[a-zA-Z0-9_!#\$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+\$"
    const val PATTERN_MOBILE_NUMBER: String = "^0\\d{9,10}\$"
    const val PATTERN_PHONE_NUMBER: String = "^0\\d{8,10}\$"
    const val PATTERN_NAME: String = "^[가-힣a-zA-Z]{2,20}\$"
    const val PATTERN_SHIPPING_ADDRESS_NAME_RECIPIENT = "^[a-zA-Z가-힣0-9]{1,20}\$"
    const val PATTERN_EMPTY = "^$"
    val REGEX_EMAIL: Regex = PATTERN_EMAIL.toRegex()
    val REGEX_MOBILE_NUMBER: Regex = PATTERN_MOBILE_NUMBER.toRegex()
}
