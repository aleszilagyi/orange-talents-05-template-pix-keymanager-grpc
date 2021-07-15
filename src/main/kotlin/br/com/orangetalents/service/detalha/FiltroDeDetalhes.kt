package br.com.orangetalents.service.detalha

import br.com.orangetalents.common.exception.customException.ChavePixNaoEncontradaException
import br.com.orangetalents.common.validators.ValidUUID
import br.com.orangetalents.dto.DetalheChavePixDto
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientBcb.BcbClient
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class FiltroDeDetalhes {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): DetalheChavePixDto

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidUUID val clienteId: String,
        @field:NotBlank @field:ValidUUID val pixId: String,
    ) : FiltroDeDetalhes() { // 1

        fun pixIdAsUuid() = UUID.fromString(pixId)
        fun clienteIdAsUuid() = UUID.fromString(clienteId)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): DetalheChavePixDto {
            return repository.findById(pixIdAsUuid())
                .filter { it.pertenceAo(clienteIdAsUuid()) }
                .map(DetalheChavePixDto::of)
                .orElseThrow { ChavePixNaoEncontradaException() }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @field:Size(max = 77) val chave: String) : FiltroDeDetalhes() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): DetalheChavePixDto {
            return repository.findByChave(chave)
                .map(DetalheChavePixDto::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.findByKey(chave)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toDto()
                        else -> throw ChavePixNaoEncontradaException()
                    }
                }
        }
    }

    @Introspected
    class Invalido() : FiltroDeDetalhes() {

        override fun filtra(repository: ChavePixRepository, bcbClient: BcbClient): DetalheChavePixDto {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}