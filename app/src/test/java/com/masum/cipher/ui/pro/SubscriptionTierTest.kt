package com.masum.cipher.ui.pro

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionTierTest {

    @Test
    fun recurringTiersCanBeManaged() {
        listOf("MONTHLY", "HALF_YEARLY", "6MONTH", "ANNUAL", "annual", " Monthly ").forEach {
            assertTrue(it, isSubscriptionTier(it))
        }
    }

    @Test
    fun oneTimeAndInternalTiersCannot() {
        listOf("LIFETIME", "PROMO", "DEV", "FREE", "").forEach {
            assertFalse(it, isSubscriptionTier(it))
        }
    }
}
