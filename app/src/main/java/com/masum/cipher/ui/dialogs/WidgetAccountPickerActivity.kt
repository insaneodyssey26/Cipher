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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.AccountEntity
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.data.repository.AccountRepository
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.domain.usecase.WidgetSyncManager
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.accounts.getAccountIconVector
import com.masum.cipher.ui.theme.CipherTheme
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.widget.BudgetWidget
import com.masum.cipher.ui.widget.PassbookWidget
import com.masum.cipher.ui.widget.StatsWidget
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Layers
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@AndroidEntryPoint
class WidgetAccountPickerActivity : ComponentActivity() {

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var widgetSyncManager: WidgetSyncManager

    companion object {
        const val EXTRA_WIDGET_TYPE = "EXTRA_WIDGET_TYPE"
        const val WIDGET_TYPE_BUDGET = "BUDGET"
        const val WIDGET_TYPE_STATS = "STATS"
        const val WIDGET_TYPE_PASSBOOK = "PASSBOOK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE) ?: WIDGET_TYPE_BUDGET

        setContent {
            var accounts by remember { mutableStateOf<List<AccountEntity>>(emptyList()) }
            var currentAccountId by remember { mutableStateOf<Long?>(-1L) }
            var isHapticsEnabled by remember { mutableStateOf(true) }
            var themeMode by remember { mutableStateOf(AppTheme.SYSTEM) }
            var accentColorHex by remember { mutableStateOf(0xFF4F46E5) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val settings = userPreferences.settingsFlow.first()
                isHapticsEnabled = settings.isHapticsEnabled
                themeMode = settings.theme
                accentColorHex = settings.accentColor.colorValue
                val allAccounts = accountRepository.getAllAccountsFlow().first()
                accounts = allAccounts
                currentAccountId = if (widgetType == WIDGET_TYPE_BUDGET) {
                    settings.budgetAccountId ?: -1L
                } else {
                    -1L
                }
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
                WidgetAccountPickerDialogContent(
                    widgetType = widgetType,
                    accounts = accounts,
                    selectedAccountId = currentAccountId,
                    isHapticsEnabled = isHapticsEnabled,
                    onDismiss = { finish() },
                    onSelectAccount = { selectedId, selectedName ->
                        scope.launch {
                            handleAccountSelection(widgetType, selectedId, selectedName)
                            finish()
                        }
                    }
                )
            }
        }
    }

    private suspend fun handleAccountSelection(widgetType: String, accountId: Long, accountName: String) {
        val manager = GlanceAppWidgetManager(this)
        when (widgetType) {
            WIDGET_TYPE_BUDGET -> {
                userPreferences.setBudgetAccountId(if (accountId <= 0L) null else accountId)
                widgetSyncManager.syncWidget()
            }
            WIDGET_TYPE_STATS -> {
                manager.getGlanceIds(StatsWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(this, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        val jsonStr = prefs[WidgetKeys.STATS_ACCOUNTS_JSON] ?: "[]"
                        val array = try { JSONArray(jsonStr) } catch (_: Exception) { JSONArray() }
                        var targetSpent = 0.0
                        var targetIncome = 0.0
                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            if (obj.optLong("id") == accountId || obj.optString("name") == accountName) {
                                targetSpent = obj.optDouble("spent", 0.0)
                                targetIncome = obj.optDouble("income", 0.0)
                                break
                            }
                        }
                        prefs.toMutablePreferences().apply {
                            this[WidgetKeys.STATS_ACCOUNT_NAME] = accountName
                            this[WidgetKeys.STATS_SPENT] = targetSpent
                            this[WidgetKeys.STATS_INCOME] = targetIncome
                        }
                    }
                    StatsWidget().update(this, glanceId)
                }
            }
            WIDGET_TYPE_PASSBOOK -> {
                manager.getGlanceIds(PassbookWidget::class.java).forEach { glanceId ->
                    updateAppWidgetState(this, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[WidgetKeys.PASSBOOK_ACCOUNT_NAME] = accountName
                            this[WidgetKeys.PASSBOOK_ACCOUNT_ID] = accountId.toString()
                        }
                    }
                    PassbookWidget().update(this, glanceId)
                }
                widgetSyncManager.syncWidget()
            }
            else -> {
                widgetSyncManager.syncWidget()
            }
        }
    }
}

@Composable
private fun WidgetAccountPickerDialogContent(
    widgetType: String,
    accounts: List<AccountEntity>,
    selectedAccountId: Long?,
    isHapticsEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectAccount: (Long, String) -> Unit
) {
    val view = LocalView.current

    val title = when (widgetType) {
        WidgetAccountPickerActivity.WIDGET_TYPE_BUDGET -> "Select Budget Account"
        WidgetAccountPickerActivity.WIDGET_TYPE_STATS -> "Select Cashflow Account"
        WidgetAccountPickerActivity.WIDGET_TYPE_PASSBOOK -> "Filter Passbook by Account"
        else -> "Select Account"
    }

    val subtitle = when (widgetType) {
        WidgetAccountPickerActivity.WIDGET_TYPE_BUDGET -> "Show monthly budget & limits for this account"
        WidgetAccountPickerActivity.WIDGET_TYPE_STATS -> "Show monthly income & expenses for this account"
        WidgetAccountPickerActivity.WIDGET_TYPE_PASSBOOK -> "Show recent transactions for this account"
        else -> "Choose an account for this widget"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .padding(20.dp),
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
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        style = Typography.titleMedium.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = Typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isAllSelected = selectedAccountId == null || selectedAccountId <= 0L
                    item(key = -1L) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isAllSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onSelectAccount(-1L, "All Accounts")
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = LucideIcons.Layers,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "All Accounts",
                                    style = Typography.titleSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Combined summary across all accounts",
                                    style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isAllSelected) {
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

                    items(accounts, key = { it.id }) { account ->
                        val isSelected = selectedAccountId == account.id
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
                                    onSelectAccount(account.id, account.name)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
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
                                val accountType = AccountType.fromKey(account.type)
                                val typeLabel = if (!account.accountNumberLast4.isNullOrBlank()) {
                                    "${accountType.displayName} •••• ${account.accountNumberLast4}"
                                } else {
                                    accountType.displayName
                                }
                                Text(
                                    text = typeLabel,
                                    style = Typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
