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
import br.com.orangetalents.service.clientBacen.*
import br.com.orangetalents.service.clientItau.ContasDeClientesItauClient
import br.com.orangetalents.service.clientItau.DadosDeContasResponseDto
import br.com.orangetalents.service.clientItau.InstituicaoResponseDto
import br.com.orangetalents.service.clientItau.TitularResponseDto
import br.com.orangetalents.utils.violations
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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RegistraChaveEndpointTest(
    val repository: ChavePixRepository,
    val gRpcClient: KeyManagerRegistraPixServiceGrpc.KeyManagerRegistraPixServiceBlockingStub
) {
    @Inject
    lateinit var bacenClient: BancoCentralClient
    @Inject
    lateinit var itauClient: ContasDeClientesItauClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val ITAU_ISPB = "60701190"
        val EMAIL_PIX = "teste@gmail.com"
    }

    @BeforeEach
    fun initSetup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {
        // mockar o response do cliente para preparar o cenário de cliente OK
        `when`(itauClient.verificaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoDeConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        `when`(bacenClient.createPixKey(createPixKeyRequestDto()))
            .thenReturn(HttpResponse.created(createPixKeyResponseDto()))

        // fazer o request no GRpc e atribuir na variável Reply
        val reply = gRpcClient.registrar(
            RegistraChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChavePix(EMAIL_PIX)
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
                chave = "86135457004",
                clienteId = CLIENTE_ID
            )
        )

        // fazer a tentativa de registrar a chave existente
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChavePix("86135457004")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave PIX 86135457004 já existe", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        // mockar um notfound pro ItauClient
        `when`(itauClient.verificaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoDeConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // Tentar registrar e tomar falha na cara
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChavePix(EMAIL_PIX)
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no Itaú", status.description)
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.registrar(RegistraChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(
                violations(), containsInAnyOrder(
                    Pair("clienteId", "não deve estar em branco"),
                    Pair("clienteId", "não é um formato válido de UUID"),
                    Pair("tipoDeConta", "não deve ser nulo"),
                    Pair("tipoDeChave", "não deve ser nulo"),
                )
            )
        }
    }

    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos - chave invalida`() {
        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChavePix("123.456.ABCD$#K0tlinIsScuffed--.789-10")
                    .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertThat(
                violations(), containsInAnyOrder(
                    Pair(
                        "?? key ??",
                        "tipo de (CPF) inválido"
                    ), //TODO: Ajustar essa parte do fieldError, talvez retornar como "global error?" sem field específico, ou pegar no Payload
                )
            )
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao for possivel registrar chave no Bacen`() {
        `when`(itauClient.verificaContaPorTipo(clienteId = CLIENTE_ID.toString(), tipoDeConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))
        `when`(bacenClient.createPixKey(createPixKeyRequestDto()))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            gRpcClient.registrar(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChavePix(EMAIL_PIX)
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao registrar chave Pix no Banco Central do Brasil (BCB/Bacen)", status.description)
        }
    }

    private fun instaciaChave(tipo: TipoDeChaveModel, chave: String, clienteId: UUID): ChavePixModel {
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

    private fun dadosDaContaResponse(): DadosDeContasResponseDto {
        return DadosDeContasResponseDto(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponseDto("ITAÚ UNIBANCO S.A", ITAU_ISPB),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponseDto("Yuri Matheus", "86135457004")
        )
    }

    private fun createPixKeyRequestDto(): CreatePixKeyRequestDto {
        return CreatePixKeyRequestDto(
            keyType = PixKeyType.EMAIL,
            key = EMAIL_PIX,
            bankAccount = bankAccount(),
            owner = owner()
        )
    }

    private fun createPixKeyResponseDto(): CreatePixKeyResponseDto {
        return CreatePixKeyResponseDto(
            keyType = PixKeyType.EMAIL,
            key = EMAIL_PIX,
            bankAccount = bankAccount(),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun bankAccount(): BankAccount {
        return BankAccount(
            participant = ITAU_ISPB,
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

    @MockBean(BancoCentralClient::class)
    fun bacenClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
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
}