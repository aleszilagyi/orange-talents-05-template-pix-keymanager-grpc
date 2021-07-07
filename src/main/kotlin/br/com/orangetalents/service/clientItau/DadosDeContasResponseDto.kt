package br.com.orangetalents.service.clientItau

import br.com.orangetalents.model.ContaEmbeddable

data class DadosDeContasResponseDto(
    val tipoDeConta: String,
    val instituicao: InstituicaoResponseDto,
    val agencia: String,
    val numero: String,
    val titular: TitularResponseDto
) {
    fun toModel(): ContaEmbeddable {
        return ContaEmbeddable(
            instituicao = this.instituicao.nome,
            cpfDoTitular = this.instituicao.cpf,
            nomeDoTitular = this.titular.nome,
            agencia = this.agencia,
            numero = this.numero
        )
    }
}

data class InstituicaoResponseDto(val nome: String, val cpf: String)
data class TitularResponseDto(val nome: String, val ispb: String)
