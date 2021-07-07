package br.com.orangetalents.service

import br.com.orangetalents.dto.ChavePixDto
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientItau.ContasDeClientesItauClient
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesItauClient
) {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: ChavePixDto): ChavePixModel {

        if (repository.existsByChave(novaChavePix.chavePix)) throw RuntimeException("chave pix ${novaChavePix.chavePix} existente")

        val response = itauClient.verificaContaPorTipo(novaChavePix.clienteId!!, novaChavePix.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("cliente não encontrado no Itaú")

        val chave = novaChavePix.toModel(conta)
        repository.save(chave)

        return chave
    }
}
