package br.com.orangetalents.service.remove

import br.com.orangetalents.common.exception.customException.ChavePixNaoEncontradaException
import br.com.orangetalents.dto.RemoveChavePixDto
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientBcb.BcbClient
import br.com.orangetalents.service.clientBcb.DeletePixKeyRequest
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChaveService(
    val repository: ChavePixRepository,
    val bcbClient: BcbClient
) {
    @Transactional
    fun remove(@Valid request: RemoveChavePixDto?) {

        val uuidPixId = UUID.fromString(request?.pixId)
        val uuidClienteId = UUID.fromString(request?.clienteId)

        val chave = repository.findByIdAndClienteId(uuidPixId, uuidClienteId)
            .orElseThrow { ChavePixNaoEncontradaException() }

        repository.delete(chave)

        val deleteRequest = DeletePixKeyRequest(chave.chave)

        val bacenResponse = bcbClient.delete(key = chave.chave, request = deleteRequest)
        if (bacenResponse.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil (BCB/Bacen)")
        }
    }
}