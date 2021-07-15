package br.com.orangetalents.controller.lista

import br.com.orangetalents.KeyManagerListaPixServiceGrpc
import br.com.orangetalents.ListaChavesPixReply
import br.com.orangetalents.ListaChavesPixRequest
import br.com.orangetalents.common.exception.ErrorHandler
import br.com.orangetalents.service.lista.ListaChavesService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChavesPixEndpoint(@Inject private val service: ListaChavesService) :
    KeyManagerListaPixServiceGrpc.KeyManagerListaPixServiceImplBase() {
    override fun lista(request: ListaChavesPixRequest, responseObserver: StreamObserver<ListaChavesPixReply>) {
        val chaves = service.lista(request.clienteId)

        responseObserver.onNext(
            ListaChavesPixReply.newBuilder()
                .setClienteId(request.clienteId.toString())
                .addAllChaves(chaves)
                .build()
        )

        responseObserver.onCompleted()
    }
}