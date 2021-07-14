package br.com.orangetalents.common.exception

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class) //Para mockar os objetos AOP?, precisa dessa extension
internal class ExceptionHandlerInterceptorTest {

    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(resolver = ExceptionHandlerResolver(handlers = emptyList()))

    @Test
    fun `deve capturar a excecao lancada pelo execucao do metodo, e gerar um erro na resposta gRPC`(@Mock streamObserver: StreamObserver<*>) {
        with(context) {
            `when`(proceed()).thenThrow(RuntimeException("vixi, deu um ruim!"))
            `when`(parameterValues).thenReturn(arrayOf(null, streamObserver))
        }

        interceptor.intercept(context)

        verify(streamObserver).onError(notNull())
    }

    @Test
    fun `se o metodo nao gerar nenhuma excecao, deve apenas retornar a mesma resposta`() {
        val expected = "whatever"

        `when`(context.proceed()).thenReturn(expected)

        assertEquals(expected, interceptor.intercept(context))
    }

}