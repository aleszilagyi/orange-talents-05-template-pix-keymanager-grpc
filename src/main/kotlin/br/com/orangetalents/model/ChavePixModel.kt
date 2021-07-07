package br.com.orangetalents.model

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.persistence.EnumType.STRING
import javax.persistence.GenerationType.AUTO
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chave_pix",
        columnNames = ["chave"]
    )]
)
class ChavePixModel(
    @field:NotNull
    @Column(nullable = false)
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(STRING)
    @Column(nullable = false)
    val tipoDeChave: TipoDeChaveModel,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(STRING)
    @Column(nullable = false)
    val tipoDeConta: TipoDeContaModel,

    @field:Valid
    @Embedded
    val conta: ContaEmbeddable
) {
    @Id
    @GeneratedValue(strategy = AUTO)
    val id: UUID? = null

    @Column(updatable = false, nullable = false)
    @CreationTimestamp
    val criadoEm: LocalDateTime = LocalDateTime.now()

    @Column(updatable = true, nullable = false)
    @UpdateTimestamp
    val atualizadoEm: LocalDateTime = LocalDateTime.now()
}

enum class TipoDeChaveModel {
    CPF,
    CELULAR,
    EMAIL,
    ALEATORIA
}

enum class TipoDeContaModel {
    CONTA_CORRENTE,
    CONTA_POUPANCA
}