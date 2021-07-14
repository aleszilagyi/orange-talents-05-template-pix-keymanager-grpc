package br.com.orangetalents.controller.registra

import br.com.orangetalents.KeyManagerRegistraPixServiceGrpc
import br.com.orangetalents.RegistraChavePixReply
import br.com.orangetalents.RegistraChavePixRequest
import br.com.orangetalents.common.exception.ErrorHandler
import br.com.orangetalents.controller.toDto
import br.com.orangetalents.service.registra.NovaChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService) :
    KeyManagerRegistraPixServiceGrpc.KeyManagerRegistraPixServiceImplBase() {
    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixReply>
    ) {
        val novaChavePix = request.toDto()
        val chaveRegistrada = service.registra(novaChavePix)

        responseObserver.onNext(
            RegistraChavePixReply.newBuilder()
                .setPixId(chaveRegistrada.id.toString())
                .setClienteId(chaveRegistrada.clienteId.toString())
                .build()
        )

        responseObserver.onCompleted()
    }
}