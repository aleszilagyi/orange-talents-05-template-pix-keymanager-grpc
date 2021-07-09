package br.com.orangetalents.common.exception

import br.com.orangetalents.common.exception.handlers.DefaultExceptionHandler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(
    @Inject private val handlers: List<IExceptionHandler<*>>,
) {
    private var defaultHandler: IExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(handlers: List<IExceptionHandler<Exception>>, defaultHandler: IExceptionHandler<Exception>) : this(
        handlers
    ) {
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): IExceptionHandler<*> {
        val foundHandlers = handlers.filter { handler -> handler.supports(e) }

        if (foundHandlers.size > 1)
            throw IllegalStateException("Too many handlers supporting the same exception '${e.javaClass.name}': $foundHandlers")

        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}
