package com.masum.cipher.ui.onboarding

import android.net.Uri
import com.masum.cipher.core.domain.usecase.ImportDataUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RestoreBackupViewModelTest {

    private lateinit var importDataUseCase: ImportDataUseCase
    private lateinit var viewModel: RestoreBackupViewModel
    private val uri = mock<Uri>()
    private val password = "secret".toCharArray()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        importDataUseCase = mock()
        viewModel = RestoreBackupViewModel(importDataUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun givenImportResult(result: Result<Unit>) = runBlocking {
        whenever(importDataUseCase(any(), any())).thenReturn(result)
    }

    @Test
    fun successfulRestoreEmitsRestoredAndStopsLoading() = runBlocking<Unit> {
        givenImportResult(Result.success(Unit))

        viewModel.handleIntent(RestoreBackupContract.Intent.Restore(uri, password))

        assertEquals(RestoreBackupContract.Effect.Restored, viewModel.effect.first())
        assertFalse(viewModel.state.value.isRestoring)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun failedRestoreExposesTheErrorAndStopsLoading() {
        givenImportResult(Result.failure(Exception("Incorrect password. Please try again.")))

        viewModel.handleIntent(RestoreBackupContract.Intent.Restore(uri, password))

        assertEquals("Incorrect password. Please try again.", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isRestoring)
    }

    @Test
    fun dismissingTheErrorClearsIt() {
        givenImportResult(Result.failure(Exception("Restore failed. Please try again.")))
        viewModel.handleIntent(RestoreBackupContract.Intent.Restore(uri, password))
        assertTrue(viewModel.state.value.errorMessage != null)

        viewModel.handleIntent(RestoreBackupContract.Intent.DismissError)

        assertNull(viewModel.state.value.errorMessage)
    }
}
