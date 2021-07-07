package br.com.orangetalents.controller

import br.com.orangetalents.KeyManagerRegistraPixServiceGrpc
import br.com.orangetalents.RegistraChavePixReply
import br.com.orangetalents.RegistraChavePixRequest
import br.com.orangetalents.service.NovaChavePixService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegistraChaveEndpoint(@Inject private val service: NovaChavePixService) :
    KeyManagerRegistraPixServiceGrpc.KeyManagerRegistraPixServiceImplBase() {
    override fun registrar(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixReply>
    ) {
        val novaChavePix = request.toModel()
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