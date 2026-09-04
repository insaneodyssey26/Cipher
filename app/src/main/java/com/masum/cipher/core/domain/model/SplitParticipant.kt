package com.masum.cipher.core.domain.model

import java.util.UUID

data class SplitParticipant(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double = 0.0,
    val percentage: Double = 0.0,
    val isPaid: Boolean = false,
    val isCurrentUser: Boolean = false
)

enum class SplitMode {
    EQUAL,
    EXACT,
    PERCENTAGE
}
