package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import javax.inject.Inject

class TransferFundsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        amount: Double,
        currency: String,
        timestamp: Long = System.currentTimeMillis(),
        note: String? = null,
        outflowMerchantText: String,
        inflowMerchantText: String
    ): Pair<TransactionEntity, TransactionEntity> {
        val outflowTx = TransactionEntity(
            amount = amount,
            merchant = outflowMerchantText,
            currency = currency,
            timestamp = timestamp,
            category = "TRANSFER",
            rawSms = null,
            isIncome = false,
            note = note,
            accountId = fromAccount.id
        )

        val inflowTx = TransactionEntity(
            amount = amount,
            merchant = inflowMerchantText,
            currency = currency,
            timestamp = timestamp + 1,
            category = "TRANSFER",
            rawSms = null,
            isIncome = true,
            note = note,
            accountId = toAccount.id
        )

        transactionRepository.insertDirectTransaction(outflowTx)
        transactionRepository.insertDirectTransaction(inflowTx)

        return Pair(outflowTx, inflowTx)
    }
}
