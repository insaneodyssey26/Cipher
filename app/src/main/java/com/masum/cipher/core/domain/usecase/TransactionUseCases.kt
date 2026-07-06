package com.masum.cipher.core.domain.usecase

import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) = repository.insertTransaction(transaction)
}

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) = repository.deleteTransaction(transaction)
}

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: TransactionEntity) = repository.updateTransaction(transaction)
}
