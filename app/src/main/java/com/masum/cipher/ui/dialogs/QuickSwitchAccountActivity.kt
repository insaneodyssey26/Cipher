package com.masum.cipher.ui.dialogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.entity.TransactionEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.data.repository.TransactionRepository
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.notifications.LocalNotificationManager
import com.masum.cipher.core.notifications.NotificationActionReceiver
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.theme.CipherTheme
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuickSwitchAccountActivity : ComponentActivity() {

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionId = intent.getLongExtra(NotificationActionReceiver.EXTRA_TRANSACTION_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, -1)

        if (transactionId == -1L) {
            finish()
            return
        }

        setContent {
            var transaction by remember { mutableStateOf<TransactionEntity?>(null) }
            var accounts by remember { mutableStateOf<List<AccountEntity>>(emptyList()) }
            var isHapticsEnabled by remember { mutableStateOf(true) }
            var themeMode by remember { mutableStateOf(AppTheme.SYSTEM) }
            var accentColorHex by remember { mutableStateOf(0xFF4F46E5) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val settings = userPreferences.settingsFlow.first()
                isHapticsEnabled = settings.isHapticsEnabled
                themeMode = settings.theme
                accentColorHex = settings.accentColor.colorValue
                transaction = transactionRepository.getTransactionById(transactionId)
                accounts = accountRepository.getAllAccountsFlow().first()
            }

            val isSystemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemDark
            }

            CipherTheme(
                darkTheme = darkTheme,
                accentColor = Color(accentColorHex)
            ) {
                QuickSwitchDialogContent(
                    transaction = transaction,
                    accounts = accounts,
                    isHapticsEnabled = isHapticsEnabled,
                    onDismiss = { finish() },
                    onSelectAccount = { selectedAcc ->
                        scope.launch {
                            val currentTx = transaction
                            if (currentTx != null) {
                                transactionRepository.updateTransaction(currentTx.copy(accountId = selectedAcc.id))
                            }
                            if (notificationId != -1) {
                                NotificationManagerCompat.from(this@QuickSwitchAccountActivity).cancel(notificationId)
                            }
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickSwitchDialogContent(
    transaction: TransactionEntity?,
    accounts: List<AccountEntity>,
    isHapticsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectAccount: (AccountEntity) -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.35f))
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                .clickable(enabled = false) { }
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.notify_action_switch_account),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (transaction != null) {
                            Text(
                                text = transaction.merchant.ifBlank { "Transaction" },
                                style = Typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        val isSelected = transaction?.accountId == account.id
                        val accountType = AccountType.fromKey(account.type)
                        val iconVector = getAccountIconVector(account.iconName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onSelectAccount(account)
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(account.colorHex).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = Color(account.colorHex),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!account.accountNumberLast4.isNullOrBlank()) {
                                    Text(
                                        text = "•••• ${account.accountNumberLast4}",
                                        style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            style = Typography.labelLarge.copy(fontFamily = Lato),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
