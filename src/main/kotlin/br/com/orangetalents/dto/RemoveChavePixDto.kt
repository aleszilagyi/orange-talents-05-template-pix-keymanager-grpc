package br.com.orangetalents.dto

import br.com.orangetalents.common.validators.ValidUUID
import javax.validation.constraints.NotBlank

data class RemoveChavePixDto(
    @field:NotBlank
    @field:ValidUUID
    val clienteId: String?,
    @field:NotBlank
    @field:ValidUUID
    val pixId: String?
)