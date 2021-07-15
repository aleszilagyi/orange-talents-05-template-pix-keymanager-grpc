package br.com.orangetalents.dto

import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.ContaEmbeddable
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import java.time.LocalDateTime
import java.util.*

class DetalheChavePixDto(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoDeChaveModel,
    val chave: String,
    val tipoDeConta: TipoDeContaModel,
    val conta: ContaEmbeddable,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePixModel): DetalheChavePixDto {
            return DetalheChavePixDto(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipoDeChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoDeConta,
                conta = chave.conta,
                registradaEm = chave.criadoEm
            )
        }
    }
}