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

class PassbookWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val accentColor = Color(settings.accentColor.colorValue)
        val currencySymbol = settings.currencySymbol
        val isPro = settings.isPro

        provideContent {
            val prefs = currentState<Preferences>()
            val rawTxJson = prefs[WidgetKeys.RECENT_TX_JSON] ?: "[]"
            val transactions = parseTransactionsJson(rawTxJson)

            GlanceTheme {
                Content(
                    context = context,
                    isPro = isPro,
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    brandColor = ColorProvider(accentColor)
                )
            }
        }
    }

    private fun parseTransactionsJson(rawJson: String): List<SimpleTx> {
        return try {
            val array = JSONArray(rawJson)
            val list = mutableListOf<SimpleTx>()
            for (i in 0 until minOf(array.length(), 50)) {
                val obj = array.getJSONObject(i)
                list.add(
                    SimpleTx(
                        merchant = obj.optString("merchant", "Expense"),
                        amount = obj.optDouble("amount", 0.0),
                        isIncome = obj.optBoolean("isIncome", false),
                        category = obj.optString("category", "General")
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    data class SimpleTx(
        val merchant: String,
        val amount: Double,
        val isIncome: Boolean,
        val category: String
    )

    @Composable
    private fun Content(
        context: Context,
        isPro: Boolean,
        transactions: List<SimpleTx>,
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
                            text = "passbook",
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
                            .clickable(actionRunCallback<PassbookRefreshAction>()),
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

                Spacer(GlanceModifier.height(8.dp))

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
                                text = context.getString(R.string.widget_passbook_description),
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = WidgetColors.TextMuted
                                ),
                                maxLines = 1
                            )
                        }
                    }
                } else if (transactions.isEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(WidgetColors.CardBg)
                            .cornerRadius(16.dp)
                            .clickable(actionStartActivity<MainActivity>())
                            .padding(12.dp),
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
                        items(transactions) { tx ->
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .background(WidgetColors.CardBg)
                                    .cornerRadius(12.dp)
                                    .padding(horizontal = 11.dp, vertical = 8.dp)
                                    .clickable(actionStartActivity<MainActivity>()),
                                verticalAlignment = Alignment.Vertical.CenterVertically
                            ) {
                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(
                                        text = tx.merchant,
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WidgetColors.TextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Spacer(GlanceModifier.height(1.dp))
                                    Text(
                                        text = tx.category.uppercase(),
                                        style = TextStyle(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = WidgetColors.TextMuted
                                        ),
                                        maxLines = 1
                                    )
                                }

                                val amountPrefix = if (tx.isIncome) "+" else "-"
                                val amountColor = if (tx.isIncome) WidgetColors.IncomeEmerald else WidgetColors.ExpenseRose
                                Text(
                                    text = "$amountPrefix$currencySymbol${formatAmount(tx.amount)}",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor
                                    )
                                )
                            }
                            Spacer(GlanceModifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format(Locale.getDefault(), "%.0f", amount)
    }
}

class PassbookRefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            .transactionRepository()
            .refreshWidgets()
    }
}
