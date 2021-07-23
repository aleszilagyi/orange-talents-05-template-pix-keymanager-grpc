package br.com.orangetalents.common.exception.handlers

import com.google.protobuf.Any
import br.com.orangetalents.common.exception.IExceptionHandler
import br.com.orangetalents.common.exception.StatusWithDetails
import com.google.rpc.BadRequest
import com.google.rpc.Code.*
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationExceptionHandler : IExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): StatusWithDetails {

        val details = BadRequest.newBuilder()
            .addAllFieldViolations(e.constraintViolations.map { violation ->
                BadRequest.FieldViolation.newBuilder()
                    .setField(violation.propertyPath.last().name ?: "chavePix")
                    .setDescription(violation.message)
                    .build()
            })
            .build()

        val statusProto = com.google.rpc.Status.newBuilder()
            .setCode(INVALID_ARGUMENT_VALUE)
            .setMessage("Dados inválidos")
            .addDetails(Any.pack(details))
            .build()

        return StatusWithDetails(statusProto)
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }

}