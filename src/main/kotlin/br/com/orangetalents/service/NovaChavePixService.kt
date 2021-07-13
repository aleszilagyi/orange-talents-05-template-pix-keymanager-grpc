package br.com.orangetalents.service

import br.com.orangetalents.common.exception.customException.ChavePixExistenteException
import br.com.orangetalents.dto.ChavePixDto
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientBacen.BacenClient
import br.com.orangetalents.service.clientBacen.CreatePixRequestDto
import br.com.orangetalents.service.clientItau.ContasDeClientesItauClient
import io.micronaut.http.HttpStatus
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
    @Inject val itauClient: ContasDeClientesItauClient,
    @Inject val bacenClient: BacenClient
) {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid novaChavePix: ChavePixDto): ChavePixModel {

        if (repository.existsByChave(novaChavePix.chavePix)) throw ChavePixExistenteException("Chave PIX ${novaChavePix.chavePix} já existe")

        val response = itauClient.verificaContaPorTipo(novaChavePix.clienteId!!, novaChavePix.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itaú")

        val chave = novaChavePix.toModel(conta)
        repository.save(chave)

        val bacenRequest = CreatePixRequestDto.of(chave).also { // 1
            LOGGER.info("Registrando chave Pix no Banco Central do Brasil (BCB): $it")
        }

        val bacenResponse = bacenClient.create(bacenRequest) // 1
        if (bacenResponse.status != HttpStatus.CREATED) // 1
            throw IllegalStateException("Erro ao registrar chave Pix no Banco Central do Brasil (BCB)")

        chave.atualiza(bacenResponse.body()!!.key)

        return chave
    }
}
