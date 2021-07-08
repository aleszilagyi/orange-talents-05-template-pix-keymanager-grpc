package br.com.orangetalents.service.clientItau

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
