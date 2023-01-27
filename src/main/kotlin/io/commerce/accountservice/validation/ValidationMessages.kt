package io.commerce.accountservice.validation

object ValidationMessages {
    const val INVALID_NAME: String = "한글 또는 영문 2~20자로 입력해주세요"
    const val INVALID_PASSWORD: String = "8~36자 영문, 숫자, 특수문자를 사용하세요"
    const val INVALID_FORMAT: String = "올바르지 않은 형식입니다"
    const val REQUIRED_AGREEMENT: String = "필수 동의 항목입니다"
}
