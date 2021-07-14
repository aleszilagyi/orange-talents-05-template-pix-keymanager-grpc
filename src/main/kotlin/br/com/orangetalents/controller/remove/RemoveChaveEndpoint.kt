package br.com.orangetalents.controller.remove

import br.com.orangetalents.KeyManagerRemovePixServiceGrpc
import br.com.orangetalents.RemoveChavePixReply
import br.com.orangetalents.RemoveChavePixRequest
import br.com.orangetalents.common.exception.ErrorHandler
import br.com.orangetalents.controller.toDto
import br.com.orangetalents.service.remove.RemoveChaveService
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveEndpoint(@Inject private val service: RemoveChaveService) :
    KeyManagerRemovePixServiceGrpc.KeyManagerRemovePixServiceImplBase() {
    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixReply>) {
        val requestDto = request.toDto()
        service.remove(requestDto)

        responseObserver.onNext(
            RemoveChavePixReply.newBuilder() // 1
                .setClienteId(request.clienteId)
                .setPixId(request.pixId)
                .build()
        )
        responseObserver.onCompleted()
    }
}