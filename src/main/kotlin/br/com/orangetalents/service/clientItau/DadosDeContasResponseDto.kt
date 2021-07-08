package br.com.orangetalents.service.clientItau

import br.com.orangetalents.model.ContaEmbeddable

data class DadosDeContasResponseDto(
    val tipo: String,
    val instituicao: InstituicaoResponseDto,
    val agencia: String,
    val numero: String,
    val titular: TitularResponseDto
) {
    fun toModel(): ContaEmbeddable {
        return ContaEmbeddable(
            instituicao = this.instituicao.nome,
            cpfDoTitular = this.titular.cpf,
            nomeDoTitular = this.titular.nome,
            agencia = this.agencia,
            numero = this.numero
        )
    }
}

data class TitularResponseDto(val nome: String, val cpf: String)
data class InstituicaoResponseDto(val nome: String, val ispb: String)
