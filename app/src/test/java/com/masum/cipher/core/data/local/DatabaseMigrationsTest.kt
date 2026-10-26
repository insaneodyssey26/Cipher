package com.masum.cipher.core.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DatabaseMigrationsTest {

    @Test
    fun `all migrations from 3 to 14 are defined continuously`() {
        val migrations = listOf(
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12,
            AppDatabase.MIGRATION_12_13,
            AppDatabase.MIGRATION_13_14
        )

        for (i in 0 until migrations.size) {
            val m = migrations[i]
            assertNotNull(m)
            val expectedStart = i + 3
            val expectedEnd = i + 4
            assertEquals(expectedStart, m.startVersion)
            assertEquals(expectedEnd, m.endVersion)
        }
    }
}
