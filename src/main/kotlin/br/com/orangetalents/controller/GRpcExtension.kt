package br.com.orangetalents.controller

import br.com.orangetalents.*
import br.com.orangetalents.dto.ChavePixDto
import br.com.orangetalents.dto.RemoveChavePixDto
import br.com.orangetalents.dto.TipoDeChaveDto
import br.com.orangetalents.dto.TipoDeContaDto
import br.com.orangetalents.service.detalha.FiltroDeDetalhes
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun RegistraChavePixRequest.toDto(): ChavePixDto {
    return ChavePixDto(
        clienteId = clienteId,
        tipoDeChave = if (tipoDeChave == TipoDeChave.UNKNOWN_CHAVE) null else TipoDeChaveDto.valueOf(tipoDeChave.name),
        chavePix = chavePix,
        tipoDeConta = if (tipoDeConta == TipoDeConta.UNKNOWN_CONTA) null else TipoDeContaDto.valueOf(tipoDeConta.name)
    )
}

fun RemoveChavePixRequest.toDto(): RemoveChavePixDto {
    return RemoveChavePixDto(
        clienteId = clienteId,
        pixId = pixId
    )
}

fun DetalhesChavePixRequest.toModel(validator: Validator): FiltroDeDetalhes {
    val filtro = when (filtroCase!!) {
        DetalhesChavePixRequest.FiltroCase.PIXID -> pixId.let {
            FiltroDeDetalhes.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        DetalhesChavePixRequest.FiltroCase.CHAVE -> FiltroDeDetalhes.PorChave(chave)
        DetalhesChavePixRequest.FiltroCase.FILTRO_NOT_SET -> FiltroDeDetalhes.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filtro
}