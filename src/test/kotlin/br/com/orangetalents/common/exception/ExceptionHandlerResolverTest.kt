package br.com.orangetalents.common.exception

import br.com.orangetalents.common.exception.handlers.DefaultExceptionHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ExceptionHandlerResolverTest {

    lateinit var illegalArgumentExceptionHandler: IExceptionHandler<IllegalArgumentException>

    lateinit var resolver: ExceptionHandlerResolver

    @BeforeEach
    fun setup() {
        //Criando um Handler para o IllegalArgumentException
        illegalArgumentExceptionHandler = object : IExceptionHandler<IllegalArgumentException> {

            override fun handle(e: IllegalArgumentException): StatusWithDetails {
                TODO("Sem necessidade de implementar isso, não há retorno de detalhes nos teste, apenas conferir se o handler é invocado")
            }

            override fun supports(e: Exception) = e is java.lang.IllegalArgumentException
        }

        resolver = ExceptionHandlerResolver(handlers = listOf(illegalArgumentExceptionHandler))
    }

    @Test
    fun `deve retornar o ExceptionHandler especifico para o tipo de excecao`() {
        val resolved = resolver.resolve(IllegalArgumentException())

        assertSame(illegalArgumentExceptionHandler, resolved)
    }

    @Test
    fun `deve retornar o ExceptionHandler padrao quando nenhum handler suportar o tipo da excecao`() {
        val resolved = resolver.resolve(RuntimeException())

        assertTrue(resolved is DefaultExceptionHandler)
    }

    @Test
    fun `deve lancar um erro caso encontre mais de um ExceptionHandler que suporte a mesma excecao`() {
        resolver = ExceptionHandlerResolver(listOf(illegalArgumentExceptionHandler, illegalArgumentExceptionHandler))

        assertThrows<IllegalStateException> { resolver.resolve(IllegalArgumentException()) }
    }
}