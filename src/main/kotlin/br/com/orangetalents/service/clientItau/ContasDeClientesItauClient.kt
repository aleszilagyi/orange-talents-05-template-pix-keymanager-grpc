package br.com.orangetalents.service.clientItau

import br.com.orangetalents.model.ContaEmbeddable
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${services.itau.contas.url}")
interface ContasDeClientesItauClient {
    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun verificaContaPorTipo(
        @PathVariable clienteId: String,
        @QueryValue(value = "tipo") tipoDeConta: String
    ): HttpResponse<DadosDeContasResponseDto>
}

data class DadosDeContasResponseDto(
    val tipo: String,
    val instituicao: InstituicaoResponseDto,
    val agencia: String,
    val numero: String,
    val titular: TitularResponseDto
) {
    fun toModel(): ContaEmbeddable {
        return ContaEmbeddable(
            instituicao = this.instituicao.nome,
            cpfDoTitular = this.titular.cpf,
            nomeDoTitular = this.titular.nome,
            agencia = this.agencia,
            numero = this.numero
        )
    }
}

data class TitularResponseDto(val nome: String, val cpf: String)
data class InstituicaoResponseDto(val nome: String, val ispb: String)
