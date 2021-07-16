package br.com.orangetalents.controller.detalha

import br.com.orangetalents.DetalhesChavePixReply
import br.com.orangetalents.DetalhesChavePixRequest
import br.com.orangetalents.KeyManagerDetalhaPixServiceGrpc
import br.com.orangetalents.common.exception.ErrorHandler
import br.com.orangetalents.service.detalha.DetalhaChavesService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DetalhaChavePixEndpoint(private val service: DetalhaChavesService) :
    KeyManagerDetalhaPixServiceGrpc.KeyManagerDetalhaPixServiceImplBase() {

    override fun detalha(request: DetalhesChavePixRequest, responseObserver: StreamObserver<DetalhesChavePixReply>) {
        val detalhes = service.detalha(request)

        responseObserver.onNext(detalhes)
        responseObserver.onCompleted()
    }
}