package com.masum.cipher.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography

@Composable
fun RestoreBackupPasswordDialog(
    isRestoring: Boolean,
    errorMessage: String?,
    onConfirm: (CharArray) -> Unit,
    onErrorCleared: () -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isRestoring) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.enter_backup_password_title),
                style = Typography.titleMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.backup_password_import_desc),
                    style = Typography.bodyMedium.copy(fontFamily = Lato, lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (errorMessage != null) onErrorCleared()
                    },
                    label = { Text(stringResource(R.string.password_label)) },
                    singleLine = true,
                    enabled = !isRestoring,
                    isError = errorMessage != null,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        style = Typography.bodySmall.copy(fontFamily = Lato),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (isRestoring) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                TextButton(
                    enabled = password.isNotBlank(),
                    onClick = { onConfirm(password.toCharArray()) }
                ) {
                    Text(
                        text = stringResource(R.string.action_import),
                        style = Typography.labelLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isRestoring, onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel),
                    style = Typography.labelLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
