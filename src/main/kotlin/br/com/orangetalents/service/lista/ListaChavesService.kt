package br.com.orangetalents.service.lista

import br.com.orangetalents.ListaChavesPixReply
import br.com.orangetalents.TipoDeChave
import br.com.orangetalents.TipoDeConta
import br.com.orangetalents.common.validators.ValidUUID
import br.com.orangetalents.repository.ChavePixRepository
import com.google.protobuf.Timestamp
import io.micronaut.validation.Validated
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class ListaChavesService(
    @Inject private val repository: ChavePixRepository,
) {
    fun lista(@NotBlank @ValidUUID clienteId: String?): List<ListaChavesPixReply.ChavePix> {
        val clienteIdUUID = UUID.fromString(clienteId)
        return repository.findAllByClienteId(clienteIdUUID).map { chave ->
            ListaChavesPixReply.ChavePix.newBuilder()
                .setPixId(chave.id.toString())
                .setTipo(TipoDeChave.valueOf(chave.tipoDeChave.name))
                .setChave(chave.chave)
                .setTipoDeConta(TipoDeConta.valueOf(chave.tipoDeConta.name))
                .setCriadaEm(chave.criadoEm.let { criadoEm ->
                    val createdAt = criadoEm.atZone(ZoneId.of("UTC")).toInstant()
                    return@let Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }
    }
}