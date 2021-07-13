package br.com.orangetalents.service.clientBacen

import br.com.orangetalents.model.ChavePixModel
import br.com.orangetalents.model.TipoDeChaveModel
import br.com.orangetalents.model.TipoDeContaModel
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bacen.pix.url}")
interface BancoCentralClient {
    @Post("/api/v1/pix/keys", produces = [APPLICATION_XML], consumes = [APPLICATION_XML])
    fun createPixKey(@Body bodyRequest: CreatePixKeyRequestDto): HttpResponse<CreatePixKeyResponseDto>
}

data class CreatePixKeyResponseDto(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class CreatePixKeyRequestDto(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    companion object {
        fun of(pixKey: ChavePixModel): CreatePixKeyRequestDto {
            return CreatePixKeyRequestDto(
                keyType = PixKeyType.by(pixKey.tipoDeChave),
                key = pixKey.chave,
                bankAccount = BankAccount(
                    participant = "60701190",
                    branch = pixKey.conta.agencia,
                    accountNumber = pixKey.conta.numero,
                    accountType = BankAccount.AccountType.by(pixKey.tipoDeConta),
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON, //Only available for Natural_Person
                    name = pixKey.conta.nomeDoTitular,
                    taxIdNumber = pixKey.conta.cpfDoTitular
                )
            )
        }
    }
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

data class BankAccount(
    /**
     * 60701190 ITAÚ UNIBANCO S.A. -> ISPB
     * https://www.bcb.gov.br/pom/spb/estatistica/port/ASTR003.pdf (line 221)
     */
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    /**
     * https://open-banking.pass-consulting.com/json_ExternalCashAccountType1Code.html
     */
    enum class AccountType() {

        CACC, // Current: Account used to post debits and credits when no specific account has been nominated
        SVGS; // Savings: Savings

        // Easier approach on enum instantiation issue
        companion object {
            fun by(domainType: TipoDeContaModel): AccountType {
                return when (domainType) {
                    TipoDeContaModel.CONTA_CORRENTE -> CACC
                    TipoDeContaModel.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }

}

enum class PixKeyType(val domainType: TipoDeChaveModel?) {

    CPF(TipoDeChaveModel.CPF),
    PHONE(TipoDeChaveModel.CELULAR),
    EMAIL(TipoDeChaveModel.EMAIL),
    RANDOM(TipoDeChaveModel.ALEATORIA);

    // Factory strategy, the other one seems easier, this works better with bigger enums though
    companion object {

        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoDeChaveModel): PixKeyType {
            return mapping[domainType]
                ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}