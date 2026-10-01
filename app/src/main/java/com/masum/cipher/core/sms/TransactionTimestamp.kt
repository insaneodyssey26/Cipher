package com.masum.cipher.core.sms

fun resolveTransactionTimestamp(sourceTimestampMillis: Long, nowMillis: Long): Long {
    val isUsable = sourceTimestampMillis > 0L && sourceTimestampMillis <= nowMillis
    return if (isUsable) sourceTimestampMillis else nowMillis
}
