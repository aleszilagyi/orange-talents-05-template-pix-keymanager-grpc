package br.com.orangetalents.model

import br.com.orangetalents.dto.TipoDeChaveDto
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TipoDeChaveTest {
    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando chave ALEATORIA for null ou vazia`() {
            with(TipoDeChaveDto.ALEATORIA) {
                assertTrue(validate(null))
                assertTrue(validate(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave ALEATORIA possuir um valor`() {
            with(TipoDeChaveDto.ALEATORIA) {
                assertFalse(validate("um valor qualquer"))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando chave CPF for valido`() {
            with(TipoDeChaveDto.CPF) {
                assertTrue(validate("35060731332"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave CPF for valido`() {
            with(TipoDeChaveDto.CPF) {
                assertFalse(validate("35060731331"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave CPF nao for informado`() {
            with(TipoDeChaveDto.CPF) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }

        @Test
        fun `nao deve ser valido quando chave CPF possuir letras`() {
            with(TipoDeChaveDto.CPF) {
                assertFalse(validate("3506073133a"))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando chave CELULAR for valido`() {
            with(TipoDeChaveDto.CELULAR) {
                assertTrue(validate("+5511987654321"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave CELULAR for invalido`() {
            with(TipoDeChaveDto.CELULAR) {
                assertFalse(validate("11987654321"))
                assertFalse(validate("+55a11987654321"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave CELULAR nao for informado`() {
            with(TipoDeChaveDto.CELULAR) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando chave EMAIL for valido`() {
            with(TipoDeChaveDto.EMAIL) {
                assertTrue(validate("zup.edu@zup.com.br"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave EMAIL estiver em um formato invalido`() {
            with(TipoDeChaveDto.EMAIL) {
                assertFalse(validate("zup.eduzup.com.br"))
                assertFalse(validate("zup.edu@zup.com."))
            }
        }

        @Test
        fun `nao deve ser valido quando chave EMAIL nao for informado`() {
            with(TipoDeChaveDto.EMAIL) {
                assertFalse(validate(null))
                assertFalse(validate(""))
            }
        }
    }
}