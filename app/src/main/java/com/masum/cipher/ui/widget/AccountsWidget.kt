@file:Suppress("RestrictedApi")

package com.masum.cipher.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masum.cipher.MainActivity
import com.masum.cipher.R
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.util.Locale

class AccountsWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val accentColor = Color(settings.accentColor.colorValue)
        val currencySymbol = settings.currencySymbol
        val isPro = settings.isPro

        provideContent {
            val prefs = currentState<Preferences>()
            val netWorth = prefs[WidgetKeys.NET_WORTH] ?: 0.0
            val accountsRaw = prefs[WidgetKeys.ACCOUNTS_JSON] ?: "[]"
            val accountItems = parseAccountsJson(accountsRaw)

            GlanceTheme {
                Content(
                    context = context,
                    isPro = isPro,
                    netWorth = netWorth,
                    accounts = accountItems,
                    currencySymbol = currencySymbol,
                    brandColor = ColorProvider(accentColor)
                )
            }
        }
    }

    private fun parseAccountsJson(rawJson: String): List<SimpleAccount> {
        return try {
            val array = JSONArray(rawJson)
            val list = mutableListOf<SimpleAccount>()
            for (i in 0 until minOf(array.length(), 20)) {
                val obj = array.getJSONObject(i)
                list.add(
                    SimpleAccount(
                        name = obj.optString("name", "Account"),
                        type = obj.optString("type", "BANK"),
                        balance = obj.optDouble("balance", 0.0),
                        colorHex = obj.optLong("colorHex", 0xFF6366F1L)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class SimpleAccount(
        val name: String,
        val type: String,
        val balance: Double,
        val colorHex: Long
    )

    @Composable
    private fun Content(
        context: Context,
        isPro: Boolean,
        netWorth: Double,
        accounts: List<SimpleAccount>,
        currencySymbol: String,
        brandColor: ColorProvider
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.SurfaceBg)
                .cornerRadius(24.dp)
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Row(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "cipher",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandColor
                            )
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "|",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = WidgetColors.TextMuted
                            )
                        )
                        Spacer(GlanceModifier.width(4.dp))
                        Text(
                            text = "accounts",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = WidgetColors.TextMuted
                            )
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .size(24.dp)
                            .cornerRadius(12.dp)
                            .clickable(actionRunCallback<AccountsRefreshAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↻",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandColor
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.height(4.dp))

                if (!isPro) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(WidgetColors.CardBg)
                            .cornerRadius(16.dp)
                            .clickable(actionStartActivity<MainActivity>())
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                        ) {
                            Text(
                                text = context.getString(R.string.widget_pro_locked_msg),
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WidgetColors.TextPrimary
                                ),
                                maxLines = 1
                            )
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = context.getString(R.string.widget_accounts_description),
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = WidgetColors.TextMuted
                                ),
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .clickable(actionStartActivity<MainActivity>()),
                        verticalAlignment = Alignment.Vertical.Bottom
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = context.getString(R.string.widget_net_worth_title).uppercase(),
                                style = TextStyle(
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = WidgetColors.TextMuted
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = formatMoney(netWorth, currencySymbol),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WidgetColors.TextPrimary
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(GlanceModifier.height(8.dp))

                    if (accounts.isEmpty()) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(WidgetColors.CardBg)
                                .cornerRadius(12.dp)
                                .clickable(actionStartActivity<MainActivity>())
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = context.getString(R.string.widget_no_transactions),
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = WidgetColors.TextMuted
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            items(accounts) { acc ->
                                Row(
                                    modifier = GlanceModifier
                                        .fillMaxWidth()
                                        .background(WidgetColors.CardBg)
                                        .cornerRadius(12.dp)
                                        .padding(horizontal = 11.dp, vertical = 7.dp)
                                        .clickable(actionStartActivity<MainActivity>()),
                                    verticalAlignment = Alignment.Vertical.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = GlanceModifier
                                            .size(8.dp)
                                            .cornerRadius(4.dp)
                                            .background(ColorProvider(Color(acc.colorHex)))
                                    )

                                    Spacer(GlanceModifier.width(8.dp))

                                    Column(modifier = GlanceModifier.defaultWeight()) {
                                        Text(
                                            text = acc.name,
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WidgetColors.TextPrimary
                                            ),
                                            maxLines = 1
                                        )
                                        Spacer(GlanceModifier.height(1.dp))
                                        Text(
                                            text = acc.type.uppercase(),
                                            style = TextStyle(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = WidgetColors.TextMuted
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    Text(
                                        text = formatMoney(acc.balance, currencySymbol),
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WidgetColors.TextPrimary
                                        )
                                    )
                                }
                                Spacer(GlanceModifier.height(5.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun formatMoney(amount: Double, symbol: String): String {
        val absVal = kotlin.math.abs(amount)
        val formatted = when {
            absVal >= 100_000 -> String.format(Locale.getDefault(), "%.1fL", absVal / 100_000)
            absVal >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", absVal / 1_000)
            else -> String.format(Locale.getDefault(), "%.0f", absVal)
        }
        return if (amount < 0) "-$symbol$formatted" else "$symbol$formatted"
    }
}

class AccountsRefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            .transactionRepository()
            .refreshWidgets()
    }
}
