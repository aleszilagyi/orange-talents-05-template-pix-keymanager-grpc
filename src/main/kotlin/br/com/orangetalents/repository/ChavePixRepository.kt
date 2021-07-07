package br.com.orangetalents.repository

import br.com.orangetalents.model.ChavePixModel
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePixModel, UUID> {
    fun existsByChave(chavePix: String?): Boolean
}