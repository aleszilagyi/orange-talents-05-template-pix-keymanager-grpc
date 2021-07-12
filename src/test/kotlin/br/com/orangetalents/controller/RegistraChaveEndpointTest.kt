package br.com.orangetalents.controller

import br.com.orangetalents.KeyManagerRegistraPixServiceGrpc
import br.com.orangetalents.RegistraChavePixRequest
import br.com.orangetalents.TipoDeChave
import br.com.orangetalents.TipoDeConta
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.ContaEmbeddable
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientItau.ContasDeClientesItauClient
import br.com.orangetalents.service.clientItau.DadosDeContasResponseDto
import br.com.orangetalents.service.clientItau.InstituicaoResponseDto
import br.com.orangetalents.service.clientItau.TitularResponseDto
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel.NAME
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRegistraPixServiceGrpc.KeyManagerRegistraPixServiceBlockingStub
) {
    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @Inject
    lateinit var itauClient: ContasDeClientesItauClient

    @BeforeEach
    fun initSetup() {
        repository.deleteAll()
    }

    @MockBean(ContasDeClientesItauClient::class)
    fun itauClient(): ContasDeClientesItauClient? {
        return Mockito.mock(ContasDeClientesItauClient::class.java)
    }

    @Factory
    private class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(NAME) channel: ManagedChannel): KeyManagerRegistraPixServiceGrpc.KeyManagerRegistraPixServiceBlockingStub? {
            return KeyManagerRegistraPixServiceGrpc.newBlockingStub(channel)
        }
    }

    @Test
    fun `deve registrar nova chave pix`() {
        // mockar o response do cliente para preparar o cenário de cliente OK
        `when`(itauClient.verificaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoDeConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // fazer o request no GRpc e atribuir na variável Reply
        val reply = grpcClient.registrar(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChavePix("yurimatheus@gmail.com")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        // validar as respostas contidas no Reply
        with(reply) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando chave existente`() {
        // salvar uma chave para ser já existente
        repository.save(
            instaciaChave(
                tipo = TipoDeChaveModel.CPF,
                chave = "63657520325",
                clienteId = CLIENTE_ID
            )
        )

        // fazer a tentativa de registrar a chave existente
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChavePix("63657520325")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        // validação
        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave PIX 63657520325 já existe", status.description)
        }
    }

    private fun instaciaChave(tipo: TipoDeChaveModel, chave: String, clienteId: UUID): ChavePixModel {
        return ChavePixModel(
            clienteId = clienteId,
            tipoDeChave = tipo,
            chave = chave,
            tipoDeConta = TipoDeContaModel.CONTA_CORRENTE,
            conta = ContaEmbeddable(
                instituicao = "UNIBANCO ITAU",
                nomeDoTitular = "Rafael Ponte",
                cpfDoTitular = "63657520325",
                agencia = "1218",
                numero = "291900"
            )
        )
    }

    private fun dadosDaContaResponse(): DadosDeContasResponseDto {
        return DadosDeContasResponseDto(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponseDto("ITAÚ UNIBANCO S.A", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponseDto("Yuri Matheus", "86135457004")
        )
    }
}