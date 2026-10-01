package com.masum.cipher.core.security

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SecurityManagerTest {

    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var keystoreManager: KeystoreManager
    private lateinit var securityManager: SecurityManager

    private val storedKey = "encrypted-passphrase"

    @Before
    fun setup() {
        context = mock()
        preferences = mock()
        editor = mock()
        keystoreManager = mock()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(preferences)
        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.remove(any())).thenReturn(editor)
        securityManager = SecurityManager(context, keystoreManager)
    }

    private fun givenStoredKey(decryptsTo: String?) {
        whenever(preferences.getString("db_passphrase_keystore_v2", null)).thenReturn(storedKey)
        whenever(keystoreManager.decrypt(storedKey)).thenReturn(decryptsTo)
    }

    @Test
    fun unreadableStoredKeyThrowsInsteadOfGeneratingAReplacement() {
        givenStoredKey(decryptsTo = null)

        assertThrows(DatabaseKeyUnavailableException::class.java) {
            securityManager.getDatabasePassphrase()
        }
    }

    @Test
    fun storedKeyThatCannotBeDecryptedIsReportedUnavailable() {
        givenStoredKey(decryptsTo = null)

        assertTrue(securityManager.isDatabaseKeyUnavailable())
    }

    @Test
    fun storedKeyThatDecryptsIsReportedAvailable() {
        givenStoredKey(decryptsTo = "decrypted")

        assertFalse(securityManager.isDatabaseKeyUnavailable())
    }

    @Test
    fun missingStoredKeyIsNotReportedUnavailableAndSkipsTheKeystore() {
        whenever(preferences.getString("db_passphrase_keystore_v2", null)).thenReturn(null)

        assertFalse(securityManager.isDatabaseKeyUnavailable())
        verify(keystoreManager, never()).decrypt(any())
    }

    @Test
    fun discardingTheKeyRemovesOnlyTheStoredPassphrase() {
        securityManager.discardDatabaseKey()

        verify(editor).remove("db_passphrase_keystore_v2")
        verify(editor).apply()
        verify(editor, never()).clear()
    }
}
