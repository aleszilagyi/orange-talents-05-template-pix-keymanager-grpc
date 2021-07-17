package br.com.orangetalents.controller.detalha

import br.com.orangetalents.DetalhesChavePixRequest
import br.com.orangetalents.FiltroPorPixId
import br.com.orangetalents.KeyManagerDetalhaPixServiceGrpc
import br.com.orangetalents.controller.lista.ListaChavesPixEndpointTest
import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.ContaEmbeddable
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import br.com.orangetalents.repository.ChavePixRepository
import br.com.orangetalents.service.clientBcb.*
import br.com.orangetalents.utils.violations
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class DetalhaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val gRpcClient: KeyManagerDetalhaPixServiceGrpc.KeyManagerDetalhaPixServiceBlockingStub
) {
    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_A_ID = UUID.randomUUID()
        val CLIENTE_B_ID = UUID.randomUUID()
        val CLIENTE_C_ID = UUID.randomUUID()
        val ITAU_ISPB = "60701190"
        val BRADESCO_ISPB = "60746948"
        val EMAIL_PIX = "teste@gmail.com"
        val EMAIL_PIX_CONCORRENTE = "bradesco@bradesco.com"
    }

    @BeforeEach
    fun setup() {
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.EMAIL, chave = ListaChavesPixEndpointTest.EMAIL_PIX,
                clienteId = ListaChavesPixEndpointTest.CLIENTE_A_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA, chave = ListaChavesPixEndpointTest.CLIENTE_A_ID.toString(),
                clienteId = ListaChavesPixEndpointTest.CLIENTE_A_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA, chave = ListaChavesPixEndpointTest.CLIENTE_B_ID.toString(),
                clienteId = ListaChavesPixEndpointTest.CLIENTE_B_ID
            )
        )
        repository.save(
            instanciaChave(
                tipo = TipoDeChaveModel.ALEATORIA,
                chave = ListaChavesPixEndpointTest.CLIENTE_C_ID.toString(),
                clienteId = ListaChavesPixEndpointTest.CLIENTE_C_ID
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve carregar chave por pixId e clienteId`() {
        val chaveExistente = repository.findByChave(EMAIL_PIX).get()

        val response = gRpcClient.detalha(
            DetalhesChavePixRequest.newBuilder()
                .setPixId(
                    FiltroPorPixId.newBuilder()
                        .setPixId(chaveExistente.id.toString())
                        .setClienteId(chaveExistente.clienteId.toString())
                        .build()
                ).build()
        )

        with(response) {
            assertEquals(chaveExistente.id.toString(), response.pixId)
            assertEquals(chaveExistente.clienteId.toString(), response.clienteId)
            assertEquals(chaveExistente.tipoDeChave.name, response.chave.tipo.name)
            assertEquals(chaveExistente.chave, response.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando filtro invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.detalha(
                DetalhesChavePixRequest.newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    ).build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            MatcherAssert.assertThat(
                violations(), Matchers.containsInAnyOrder(
                    Pair("clienteId", "não deve estar em branco"),
                    Pair("clienteId", "formato inválido de identificador: []"),
                    Pair("pixId", "não deve estar em branco"),
                    Pair("pixId", "formato inválido de identificador: []")
                )
            )
        }
    }

    @Test
    fun `nao deve carregar chave por pixId e clienteId quando registro nao existir`() {
        val pixIdNaoExistente = UUID.randomUUID().toString()
        val clienteIdNaoExistente = UUID.randomUUID().toString()
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.detalha(
                DetalhesChavePixRequest.newBuilder()
                    .setPixId(
                        FiltroPorPixId.newBuilder()
                            .setPixId(pixIdNaoExistente)
                            .setClienteId(clienteIdNaoExistente)
                            .build()
                    ).build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave PIX não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro existir no banco da aplicacao`() {
        val chaveExistente = repository.findByChave(EMAIL_PIX).get()

        val response = gRpcClient.detalha(
            DetalhesChavePixRequest.newBuilder()
                .setChave(EMAIL_PIX)
                .build()
        )

        with(response) {
            assertEquals(chaveExistente.id.toString(), this.pixId)
            assertEquals(chaveExistente.clienteId.toString(), response.clienteId)
            assertEquals(chaveExistente.tipoDeChave.name, response.chave.tipo.name)
            assertEquals(chaveExistente.chave, response.chave.chave)
        }
    }

    @Test
    fun `deve carregar chave por valor da chave quando registro nao existir na aplicacao mas existir no BCB`() {
        val bcbResponse = createPixKeyResponse()
        `when`(bcbClient.findByKey(key = EMAIL_PIX_CONCORRENTE))
            .thenReturn(HttpResponse.ok(bcbResponse))

        val response = gRpcClient.detalha(
            DetalhesChavePixRequest.newBuilder()
                .setChave(bcbResponse.key)
                .build()
        )

        with(response) {
            assertEquals("", response.pixId)
            assertEquals("", response.clienteId)
            assertEquals(bcbResponse.keyType.name, response.chave.tipo.name)
            assertEquals(bcbResponse.key, response.chave.chave)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando registro nao existir localmente nem no BCB`() {
        `when`(bcbClient.findByKey(key = "essachavefoilevadaparaoutrouniverso@gmail.com"))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.detalha(
                DetalhesChavePixRequest.newBuilder()
                    .setChave("essachavefoilevadaparaoutrouniverso@gmail.com")
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave PIX não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve carregar chave por valor da chave quando filtro invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.detalha(DetalhesChavePixRequest.newBuilder().setChave("").build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            MatcherAssert.assertThat(
                violations(), Matchers.containsInAnyOrder(
                    Pair("chave", "não deve estar em branco"),
                )
            )
        }
    }

    @Test
    fun `nao deve carregar chave quando filtro invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.detalha(DetalhesChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
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

    private fun createPixKeyResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = EMAIL_PIX_CONCORRENTE,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = BRADESCO_ISPB,
            branch = "0001",
            accountNumber = "291900",
            accountType = BankAccount.AccountType.CACC
        )
    }

    private fun owner(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Yuri Matheus",
            taxIdNumber = "86135457004"
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerDetalhaPixServiceGrpc.KeyManagerDetalhaPixServiceBlockingStub? {
            return KeyManagerDetalhaPixServiceGrpc.newBlockingStub(channel)
        }
    }
}
