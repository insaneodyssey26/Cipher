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
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
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
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.di.WidgetEntryPoint
import com.masum.cipher.ui.dialogs.WidgetAccountPickerActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class BudgetWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val baseBudget = settings.monthlyBudget
        val isDynamic = settings.isDynamicBudgetEnabled
        val accentColor = Color(settings.accentColor.colorValue)
        val currencySymbol = settings.currencySymbol
        provideContent {
            val prefs = currentState<Preferences>()
            val spent = prefs[WidgetKeys.BUDGET_SPENT] ?: 0.0
            val income = prefs[WidgetKeys.BUDGET_INCOME] ?: 0.0
            val accountName = prefs[WidgetKeys.BUDGET_ACCOUNT_NAME] ?: "All Accounts"
            val budget = if (isDynamic && baseBudget > 0) baseBudget + income else baseBudget
            GlanceTheme {
                Content(
                    spent = spent,
                    budget = budget,
                    accountName = accountName,
                    currencySymbol = currencySymbol,
                    brandColor = ColorProvider(accentColor)
                )
            }
        }
    }

    @Composable
    private fun Content(
        spent: Double,
        budget: Double,
        accountName: String,
        currencySymbol: String = "₹",
        brandColor: ColorProvider
    ) {
        val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        val overBudget = spent > budget && budget > 0
        val remaining = budget - spent

        val statusColor = when {
            overBudget -> WidgetColors.ExpenseRose
            progress >= 0.8f -> WidgetColors.AmberWarning
            else -> WidgetColors.LimeAccent
        }

        val displayName = if (accountName.equals("All Accounts", ignoreCase = true)) "All" else accountName
        val widgetTypeParam = ActionParameters.Key<String>(WidgetAccountPickerActivity.EXTRA_WIDGET_TYPE)

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.SurfaceBg)
                .cornerRadius(24.dp)
                .padding(10.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "cipher",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = brandColor
                        ),
                        modifier = GlanceModifier.clickable(actionStartActivity<MainActivity>())
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
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(WidgetColors.CardBg)
                            .clickable(
                                actionStartActivity<WidgetAccountPickerActivity>(
                                    actionParametersOf(widgetTypeParam to WidgetAccountPickerActivity.WIDGET_TYPE_BUDGET)
                                )
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                            Text(
                                text = displayName,
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor
                                ),
                                maxLines = 1
                            )
                            Spacer(GlanceModifier.width(3.dp))
                            Text(
                                text = "▾",
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandColor
                                )
                            )
                        }
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Box(
                        modifier = GlanceModifier
                            .size(22.dp)
                            .cornerRadius(11.dp)
                            .clickable(actionRunCallback<BudgetRefreshAction>()),
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

                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth()
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    if (budget <= 0.0) {
                        Text(
                            text = "No budget set",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WidgetColors.TextPrimary
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "Tap to set in settings",
                            style = TextStyle(
                                fontSize = 9.5.sp,
                                color = WidgetColors.TextMuted
                            )
                        )
                    } else {
                        Text(
                            text = com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(spent, currencySymbol),
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WidgetColors.TextPrimary
                            )
                        )
                        Spacer(GlanceModifier.height(1.dp))
                        Text(
                            text = "spent of ${com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(budget, currencySymbol)}",
                            style = TextStyle(
                                fontSize = 9.5.sp,
                                color = WidgetColors.TextMuted
                            )
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = GlanceModifier.fillMaxWidth().height(5.dp).cornerRadius(3.dp),
                            color = statusColor,
                            backgroundColor = WidgetColors.CardBg
                        )
                        Spacer(GlanceModifier.height(6.dp))
                        Text(
                            text = if (overBudget) "Over by ${com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(kotlin.math.abs(remaining), currencySymbol)}" else "${com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(remaining, currencySymbol)} remaining",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        )
                    }
                }
            }
        }
    }
}

class BudgetRefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            .transactionRepository()
            .refreshWidgets()
    }
}
