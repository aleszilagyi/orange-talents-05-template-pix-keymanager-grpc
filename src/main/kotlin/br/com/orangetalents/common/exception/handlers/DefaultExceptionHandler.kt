package br.com.orangetalents.common.exception.handlers

import br.com.orangetalents.common.exception.IExceptionHandler
import br.com.orangetalents.common.exception.StatusWithDetails
import io.grpc.Status.*

class DefaultExceptionHandler : IExceptionHandler<Exception> {

    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> FAILED_PRECONDITION.withDescription(e.message)
            else -> UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }

}