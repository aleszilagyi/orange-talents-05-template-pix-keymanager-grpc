package br.com.orangetalents.service.detalha

import br.com.orangetalents.*
import br.com.orangetalents.controller.toModel
import br.com.orangetalents.dto.DetalheChavePixDto
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientBcb.BcbClient
import com.google.protobuf.Timestamp
import io.micronaut.validation.Validated
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Validated
@Singleton
class DetalhaChavesService(
    @Inject private val repository: ChavePixRepository,
    @Inject private val bcbClient: BcbClient,
    @Inject private val validator: Validator
) {
    fun detalha(request: DetalhesChavePixRequest): DetalhesChavePixReply? {
        val filtroDeDetalhes = request.toModel(validator)
        val detalhesDeChave = filtroDeDetalhes.filtra(repository = repository, bcbClient = bcbClient)

        return DetalhesChavePixReply.newBuilder()
            .setClienteId(detalhesDeChave.clienteId?.toString() ?: "")
            .setPixId(detalhesDeChave.pixId?.toString() ?: "")
            .setChave(ChavePix
                .newBuilder()
                .setTipo(TipoDeChave.valueOf(detalhesDeChave.tipo.name))
                .setChave(detalhesDeChave.chave)
                .setConta(
                    ContaInfo.newBuilder()
                    .setTipo(TipoDeConta.valueOf(detalhesDeChave.tipoDeConta.name))
                    .setInstituicao(detalhesDeChave.conta.instituicao)
                    .setNomeDoTitular(detalhesDeChave.conta.nomeDoTitular)
                    .setCpfDoTitular(detalhesDeChave.conta.cpfDoTitular)
                    .setAgencia(detalhesDeChave.conta.agencia)
                    .setNumeroDaConta(detalhesDeChave.conta.numero)
                    .build()
                )
                .setCriadaEm(detalhesDeChave.registradaEm.let { localDateTime ->
                    val createdAt = localDateTime.atZone(ZoneId.of("UTC")).toInstant()
                    return@let Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }
}
