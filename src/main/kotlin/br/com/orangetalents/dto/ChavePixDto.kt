package br.com.orangetalents.dto

import br.com.orangetalents.common.validators.ValidTipoChave
import br.com.orangetalents.common.validators.ValidUUID
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.ContaEmbeddable
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidTipoChave
@Introspected
data class ChavePixDto(
    @field:NotBlank
    @field:ValidUUID
    val clienteId: String?,
    @field:NotNull
    val tipoDeChave: TipoDeChaveDto?,
    @field:Size(max = 77)
    val chavePix: String?,
    @field:NotNull
    val tipoDeConta: TipoDeContaDto?
) {
    fun toModel(conta: ContaEmbeddable): ChavePixModel {
        return ChavePixModel(
            clienteId = UUID.fromString(this.clienteId),
            tipoDeChave = TipoDeChaveModel.valueOf(this.tipoDeChave!!.name),
            chave = if (this.tipoDeChave == TipoDeChaveDto.ALEATORIA) UUID.randomUUID().toString() else this.chavePix!!,
            tipoDeConta = TipoDeContaModel.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }
}