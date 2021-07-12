package br.com.orangetalents.common.exception.handlers

import br.com.orangetalents.common.exception.IExceptionHandler
import br.com.orangetalents.common.exception.StatusWithDetails
import br.com.orangetalents.common.exception.customException.ChavePixExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : IExceptionHandler<ChavePixExistenteException> {
    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}