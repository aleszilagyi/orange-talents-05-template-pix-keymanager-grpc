package br.com.orangetalents.common.exception.customException

class ChavePixExistenteException : RuntimeException {
    constructor() : super("Chave PIX já registrada")
    constructor(message: String) : super(message)
}

