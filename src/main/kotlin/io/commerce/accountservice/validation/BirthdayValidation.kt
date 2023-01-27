package io.commerce.accountservice.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [BirthdayValidator::class])
annotation class Birthday(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class BirthdayValidator : ConstraintValidator<Birthday, String> {

    private val invalidMessage: String = "올바르지 않은 형식입니다"

    private val unsupportedBirthdayMessage: String = "설정할 수 없는 나이 입니다"

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun initialize(valid: Birthday) {
        super.initialize(valid)
    }

    override fun isValid(value: String, context: ConstraintValidatorContext): Boolean {
        try {
            val date = LocalDate.parse(value, dateFormatter)
            val now = LocalDate.now()
            if (!date.isBefore(now)) {
                context.disableDefaultConstraintViolation()
                context.buildConstraintViolationWithTemplate(unsupportedBirthdayMessage).addConstraintViolation()
                return false
            }
        } catch (e: DateTimeParseException) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate(invalidMessage).addConstraintViolation()
            return false
        }
        return true
    }
}
