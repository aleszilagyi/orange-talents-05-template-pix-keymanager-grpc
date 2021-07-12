package br.com.orangetalents.model

import br.com.orangetalents.repository.ChavePixRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.inject.Inject

internal class ChavePixModelTest {

    companion object {
        val TIPOS_DE_CHAVES_EXCETO_ALEATORIO = TipoDeChaveModel.values().filterNot { it == TipoDeChaveModel.ALEATORIA }
    }

    @Test
    fun devePertencerAChaveAoCliente() {

        val clienteId = UUID.randomUUID()
        val outroClienteId = UUID.randomUUID()

        with(instanciaNovaChave(tipoDeChave = TipoDeChaveModel.ALEATORIA, clienteId = clienteId)) {
            assertTrue(this.pertenceAo(clienteId))
            assertFalse(this.pertenceAo(outroClienteId))
        }
    }

    @Test
    fun deveChaveSerDoTipoAleatoria() {
        with(instanciaNovaChave(TipoDeChaveModel.ALEATORIA)) {
            assertTrue(this.isRandom())
        }
    }

    @Test
    fun naoDeveChaveSerDoTipoAleatoria() {
        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                assertFalse(instanciaNovaChave(it).isRandom())
            }
    }

    @Test
    fun deveAtualizarChaveQuandoChaveForAleatoria() {
        with(instanciaNovaChave(TipoDeChaveModel.ALEATORIA)) {
            assertTrue(this.atualiza("nova-chave"))
            assertEquals("nova-chave", this.chave)
        }
    }

    @Test
    fun naoDeveAtualizarChaveQuandoChaveForDiferenteDeAleatoria() {

        val original = "<chave-aleatoria-qualquer>"

        TIPOS_DE_CHAVES_EXCETO_ALEATORIO
            .forEach {
                with(instanciaNovaChave(tipoDeChave = it, chave = original)) {
                    assertFalse(this.atualiza("nova-chave"))
                    assertEquals(original, this.chave)
                }
            }
    }

    private fun instanciaNovaChave(
        tipoDeChave: TipoDeChaveModel,
        chave: String = UUID.randomUUID().toString(),
        clienteId: UUID = UUID.randomUUID(),
    ): ChavePixModel {
        return ChavePixModel(
            clienteId = clienteId,
            tipoDeChave = tipoDeChave,
            chave = chave,
            tipoDeConta = TipoDeContaModel.CONTA_CORRENTE,
            conta = ContaEmbeddable(
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeDoTitular = "Yuri Matheus",
                cpfDoTitular = "86135457004",
                agencia = "0001",
                numero = "291900"
            )
        )
    }
}