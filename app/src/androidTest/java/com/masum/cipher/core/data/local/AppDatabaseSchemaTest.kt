package com.masum.cipher.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseSchemaTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun theExportedVersionTwelveSchemaMatchesTheCurrentEntities() {
        helper.createDatabase(DATABASE_NAME, 12).close()
        helper.runMigrationsAndValidate(DATABASE_NAME, 12, true)
    }

    private companion object {
        const val DATABASE_NAME = "schema-test"
    }
}
