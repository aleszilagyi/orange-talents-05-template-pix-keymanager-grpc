package br.com.orangetalents.common.exception.customException

class ChavePixNaoEncontradaException: RuntimeException {
    constructor() : super("Chave PIX não encontrada ou não pertence ao cliente")
    constructor(message: String) : super(message)
}
