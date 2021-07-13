package br.com.orangetalents.common.validators

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import java.util.*
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidUUIDValidator::class])
@Retention(RUNTIME)
@Target(FIELD, CONSTRUCTOR, PROPERTY, VALUE_PARAMETER)
annotation class ValidUUID(
    val message: String = "não é um formato válido de UUID",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class ValidUUIDValidator : ConstraintValidator<ValidUUID, String> {
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidUUID>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.isNullOrBlank()) return false
        try {
            UUID.fromString(value)
        } catch (e: Exception) {
            return false
        }
        return true
    }
}
