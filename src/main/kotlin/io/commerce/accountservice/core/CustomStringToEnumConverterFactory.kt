package io.commerce.accountservice.core

import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import java.util.*

/**
 * 문자열을 enum class 변환 커스텀 클래스
 * - 대문자로 치환
 * - '-' -> '_' 로 치환
 */
class CustomStringToEnumConverterFactory : ConverterFactory<String, Enum<*>> {
    override fun <E : Enum<*>> getConverter(clazz: Class<E>): Converter<String, E> {
        return CustomStringToEnumConverter(clazz)
    }

    internal class CustomStringToEnumConverter<T : Enum<*>?>(private val enumClass: Class<T>) : Converter<String, T> {
        override fun convert(source: String): T? {
            if (source.isBlank()) return null
            val convertString = source.uppercase(Locale.getDefault()).replace("-", "_")
            return enumClass.enumConstants.first { it!!.name == convertString }
        }
    }
}
