package com.masum.cipher.core.sms

fun resolveSmsTimestamp(smsTimestampMillis: Long, nowMillis: Long): Long {
    val isUsable = smsTimestampMillis > 0L && smsTimestampMillis <= nowMillis
    return if (isUsable) smsTimestampMillis else nowMillis
}
