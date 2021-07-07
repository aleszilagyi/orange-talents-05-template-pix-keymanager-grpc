package br.com.orangetalents.dto

import io.micronaut.validation.validator.constraints.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator


enum class TipoDeChaveDto {
    CPF {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            if (!chave.matches("^[0-9]{11}\$".toRegex())) return false
            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    CELULAR {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun validate(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false
            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },
    ALEATORIA {
        override fun validate(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    };

    abstract fun validate(chave: String?): Boolean
}