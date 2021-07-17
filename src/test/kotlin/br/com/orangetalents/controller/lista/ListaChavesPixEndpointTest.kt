package br.com.orangetalents.controller.lista

import br.com.orangetalents.KeyManagerListaPixServiceGrpc
import br.com.orangetalents.ListaChavesPixRequest
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.ContaEmbeddable
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.utils.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel.NAME
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class ListaChavesPixEndpointTest(
    val repository: ChavePixRepository,
    val gRpcClient: KeyManagerListaPixServiceGrpc.KeyManagerListaPixServiceBlockingStub
) {
    companion object {
        val CLIENTE_A_ID = UUID.randomUUID()
        val CLIENTE_B_ID = UUID.randomUUID()
        val CLIENTE_C_ID = UUID.randomUUID()
        val ITAU_ISPB = "60701190"
        val EMAIL_PIX = "teste@gmail.com"
    }

    @BeforeEach
    fun setup() {
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.EMAIL, chave = EMAIL_PIX,
                clienteId = CLIENTE_A_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA, chave = CLIENTE_A_ID.toString(),
                clienteId = CLIENTE_A_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA, chave = CLIENTE_B_ID.toString(),
                clienteId = CLIENTE_B_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA,
                chave = CLIENTE_C_ID.toString(),
                clienteId = CLIENTE_C_ID
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves do cliente A`() {
        val response = gRpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .setClienteId(CLIENTE_A_ID.toString())
                .build()
        )

        with(response.chavesList) {
            MatcherAssert.assertThat(response.chavesList, hasSize(2))
            MatcherAssert.assertThat(
                response.chavesList.map { chave -> Pair(chave.tipo.toString(), chave.chave) },
                containsInAnyOrder(
                    Pair(TipoDeChaveModel.EMAIL.toString(), EMAIL_PIX),
                    Pair(TipoDeChaveModel.ALEATORIA.toString(), CLIENTE_A_ID.toString())
                )
            )
        }
    }

    @Test
    fun `nao deve listar as chaves do cliente quando cliente nao possuir chaves`() {
        val clienteSemChavePix = UUID.randomUUID().toString()

        val response = gRpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .setClienteId(clienteSemChavePix)
                .build()
        )

        assertEquals(0, response.chavesList.size)
    }

    @Test
    fun `nao deve listar quando o clienteId for invalido`() {
        val clienteInvalido = ""

        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.lista(
                ListaChavesPixRequest.newBuilder()
                    .setClienteId(clienteInvalido)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            MatcherAssert.assertThat(
                violations(), containsInAnyOrder(
                    Pair("clienteId", "não deve estar em branco"),
                    Pair("clienteId", "formato inválido de identificador: []")
                )
            )
        }
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(NAME) channel: ManagedChannel): KeyManagerListaPixServiceGrpc.KeyManagerListaPixServiceBlockingStub? {
            return KeyManagerListaPixServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun instanciaChave(
        tipo: TipoDeChaveModel,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePixModel {
        return ChavePixModel(
            clienteId = clienteId,
            tipoDeChave = tipo,
            chave = chave,
            tipoDeConta = TipoDeContaModel.CONTA_CORRENTE,
            conta = ContaEmbeddable(
                instituicao = "ITAÚ UNIBANCO S.A",
                nomeDoTitular = "Yuri Matheus",
                cpfDoTitular = "86135457004",
                agencia = "0001",
                numero = "291900"
            )
        )
    }
}
