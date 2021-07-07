package br.com.orangetalents.common.validators

import br.com.orangetalents.dto.ChavePixDto
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [ValidTipoChaveValidator::class])
@Retention(AnnotationRetention.RUNTIME)
@Target(
    FIELD,
    CONSTRUCTOR,
    PROPERTY,
    VALUE_PARAMETER,
    CLASS
)
annotation class ValidTipoChave(
    val message: String = "tipo de ({validatedValue}) inválido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

class ValidTipoChaveValidator : ConstraintValidator<ValidTipoChave, ChavePixDto> {
    override fun isValid(
        value: ChavePixDto?,
        annotationMetadata: AnnotationValue<ValidTipoChave>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value == null) return true

        return value.tipoDeChave!!.validate(value.chavePix)
    }
}
