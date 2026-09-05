package com.masum.cipher.core.util

import com.masum.cipher.core.domain.model.SplitParticipant
import kotlin.math.roundToLong

object SplitCalculator {

    fun calculateEqualSplits(totalAmount: Double, participants: List<SplitParticipant>): List<SplitParticipant> {
        if (participants.isEmpty()) return emptyList()
        val count = participants.size
        val totalCents = (totalAmount * 100.0).roundToLong()
        val baseCents = totalCents / count
        val remainder = totalCents % count

        return participants.mapIndexed { index, participant ->
            val extra = if (index < remainder) 1L else 0L
            val share = (baseCents + extra) / 100.0
            val pct = if (totalAmount > 0) (share / totalAmount) * 100.0 else 100.0 / count
            participant.copy(amount = share, percentage = pct)
        }
    }

    fun calculatePercentageSplits(totalAmount: Double, participants: List<SplitParticipant>): List<SplitParticipant> {
        return participants.map { participant ->
            val share = ((totalAmount * participant.percentage) / 100.0 * 100.0).roundToLong() / 100.0
            participant.copy(amount = share)
        }
    }

    fun formatShareBreakdownMessage(
        expenseName: String,
        totalAmount: Double,
        currencySymbol: String,
        participants: List<SplitParticipant>
    ): String {
        val sb = StringBuilder()
        sb.append("Expense: ").append(expenseName.ifBlank { "Expense" }).append("\n")
        sb.append("Total: ").append(currencySymbol).append(String.format(java.util.Locale.US, "%.2f", totalAmount)).append("\n\n")
        sb.append("Split Breakdown:\n")
        participants.forEach { p ->
            val status = if (p.isCurrentUser) "(Host)" else if (p.isPaid) "(Settled)" else "(Pending)"
            sb.append("• ").append(p.name).append(": ").append(currencySymbol)
                .append(String.format(java.util.Locale.US, "%.2f", p.amount))
                .append(" ").append(status).append("\n")
        }
        return sb.toString().trimEnd()
    }
}
