package br.com.orangetalents.controller

import br.com.orangetalents.RegistraChavePixRequest
import br.com.orangetalents.TipoDeChave
import br.com.orangetalents.TipoDeConta
import br.com.orangetalents.dto.ChavePixDto
import br.com.orangetalents.dto.TipoDeChaveDto
import br.com.orangetalents.dto.TipoDeContaDto

fun RegistraChavePixRequest.toModel(): ChavePixDto {
    return ChavePixDto(
        clienteId = clienteId,
        tipoDeChave = if (tipoDeChave == TipoDeChave.UNKNOWN_CHAVE) null else TipoDeChaveDto.valueOf(tipoDeChave.name),
        chavePix = chavePix,
        tipoDeConta = if (tipoDeConta == TipoDeConta.UNKNOWN_CONTA) null else TipoDeContaDto.valueOf(tipoDeConta.name)
    )
}