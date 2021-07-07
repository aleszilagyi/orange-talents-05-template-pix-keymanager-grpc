package br.com.orangetalents.service.clientItau

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.net.http.HttpResponse

@Client("\${services.itau.contas.url}")
interface ContasDeClientesItauClient {
    @Get("/api/v1/clientes/{clienteId}/contas{?tipoDeConta}")
        fun verificaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipoDeConta: String): HttpResponse<DadosDeContasResponseDto>
}
