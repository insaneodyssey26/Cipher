package com.masum.cipher.ui.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Lock

@Composable
fun DatabaseRecoveryScreen(
    onRetry: () -> Boolean,
    onErase: () -> Unit
) {
    var retryFailed by remember { mutableStateOf(false) }
    var showEraseConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.database_recovery_title),
                style = Typography.titleLarge.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.database_recovery_message),
                style = Typography.bodyMedium.copy(
                    fontFamily = Lato,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (retryFailed) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.database_recovery_still_unavailable),
                    style = Typography.bodySmall.copy(fontFamily = Lato, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { retryFailed = !onRetry() },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.database_recovery_retry),
                    style = Typography.labelLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showEraseConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.database_recovery_erase),
                    style = Typography.labelLarge.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showEraseConfirmation) {
        AlertDialog(
            onDismissRequest = { showEraseConfirmation = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = stringResource(R.string.database_recovery_erase_confirm_title),
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.database_recovery_erase_confirm_message),
                    style = Typography.bodyMedium.copy(fontFamily = Lato, lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEraseConfirmation = false
                        onErase()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.database_recovery_erase_confirm_action),
                        style = Typography.labelLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEraseConfirmation = false }) {
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
}
