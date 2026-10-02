package com.masum.cipher.core.security

import com.masum.cipher.core.data.local.pref.UserPreferences
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class PendingLicenseActivatorTest {

    private lateinit var userPreferences: UserPreferences
    private lateinit var licenseEngine: LicenseEngine
    private lateinit var deviceIdProvider: DeviceIdProvider
    private lateinit var activator: PendingLicenseActivator

    private val token = "RESTORED-KEY-1"

    @Before
    fun setup() {
        userPreferences = mock()
        licenseEngine = mock()
        deviceIdProvider = mock()
        whenever(deviceIdProvider.get()).thenReturn("device-1")
        activator = PendingLicenseActivator(userPreferences, licenseEngine, deviceIdProvider)
    }

    private fun givenPendingToken(value: String?) {
        whenever(userPreferences.getPendingLicenseToken()).thenReturn(value)
    }

    private fun givenServerAnswer(result: LicenseValidationResult) = runBlocking {
        whenever(licenseEngine.activateLicenseRemote(any(), anyOrNull(), any(), any())).thenReturn(result)
    }

    @Test
    fun nothingHappensWhenNoKeyIsPending() = runBlocking<Unit> {
        givenPendingToken(null)

        activator.activate()

        verifyNoInteractions(licenseEngine)
        verify(userPreferences, never()).setProStatus(any(), any(), anyOrNull(), anyOrNull(), any())
    }

    @Test
    fun theKeyIsKeptWhenThisDeviceHasNoId() = runBlocking<Unit> {
        givenPendingToken(token)
        whenever(deviceIdProvider.get()).thenReturn(null)

        activator.activate()

        verifyNoInteractions(licenseEngine)
        verify(userPreferences, never()).setPendingLicenseToken(anyOrNull())
    }

    @Test
    fun proTurnsOnOnlyAfterTheServerAcceptsTheKey() = runBlocking<Unit> {
        givenPendingToken(token)
        givenServerAnswer(
            LicenseValidationResult(isValid = true, tier = ProTier.LIFETIME, orderId = "DODO-1", expiresAtEpochMs = 0L)
        )

        activator.activate()

        verify(userPreferences).setProStatus(eq(true), eq("LIFETIME"), eq(token), eq("DODO-1"), eq(0L))
        verify(userPreferences).setPendingLicenseToken(null)
    }

    @Test
    fun aKeyTheServerRejectsNeverTurnsProOnAndIsDropped() = runBlocking<Unit> {
        givenPendingToken(token)
        givenServerAnswer(LicenseValidationResult(isValid = false, errorMessage = "License key not found."))

        activator.activate()

        verify(userPreferences, never()).setProStatus(any(), any(), anyOrNull(), anyOrNull(), any())
        verify(userPreferences).setPendingLicenseToken(null)
    }

    @Test
    fun aRejectedRestoreTellsAUserWithoutProThatTheirLicenseWasNotAccepted() = runBlocking<Unit> {
        givenPendingToken(token)
        whenever(userPreferences.isCachedPro()).thenReturn(false)
        givenServerAnswer(LicenseValidationResult(isValid = false, errorMessage = "Device limit reached"))

        activator.activate()

        verify(userPreferences).markLicenseRevoked(any())
    }

    @Test
    fun aRejectedRestoreNeverRemovesProThatAlreadyWorksOnThisPhone() = runBlocking<Unit> {
        givenPendingToken(token)
        whenever(userPreferences.isCachedPro()).thenReturn(true)
        givenServerAnswer(LicenseValidationResult(isValid = false, errorMessage = "License key not found."))

        activator.activate()

        verify(userPreferences, never()).markLicenseRevoked(any())
        verify(userPreferences).setPendingLicenseToken(null)
    }

    @Test
    fun withoutInternetProStaysOffButTheKeyIsKeptForTheNextAttempt() = runBlocking<Unit> {
        givenPendingToken(token)
        givenServerAnswer(LicenseValidationResult(isValid = false, isNetworkFailure = true))

        activator.activate()

        verify(userPreferences, never()).setProStatus(any(), any(), anyOrNull(), anyOrNull(), any())
        verify(userPreferences, never()).setPendingLicenseToken(anyOrNull())
    }

    @Test
    fun theDeviceIdSentToTheServerIsThisDevicesId() = runBlocking<Unit> {
        givenPendingToken(token)
        givenServerAnswer(LicenseValidationResult(isValid = false, errorMessage = "nope"))

        activator.activate()

        verify(licenseEngine).activateLicenseRemote(eq(token), anyOrNull(), eq("device-1"), any())
    }
}
