package io.commerce.accountservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class AccountServiceApplication

fun main(args: Array<String>) {
    // Hibernate Validator에서 제공하는 한글 ValidationMessageSource 사용을 위해
    // JVM default locale을 ko_KR로 변경하였습니다
    Locale.setDefault(Locale.KOREA)
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    runApplication<AccountServiceApplication>(*args)
}
