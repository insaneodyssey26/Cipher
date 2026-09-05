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
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class StatsWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val accentColor = Color(settings.accentColor.colorValue)
        val currencySymbol = settings.currencySymbol
        provideContent {
            val prefs = currentState<Preferences>()
            val spent = prefs[WidgetKeys.STATS_SPENT] ?: 0.0
            val income = prefs[WidgetKeys.STATS_INCOME] ?: 0.0
            GlanceTheme {
                Content(spent = spent, income = income, currencySymbol = currencySymbol, brandColor = ColorProvider(accentColor))
            }
        }
    }

    @Composable
    private fun Content(spent: Double, income: Double, currencySymbol: String = "₹", brandColor: ColorProvider) {
        val net = income - spent
        val netPositive = net >= 0

        val emeraldIncome = ColorProvider(Color(0xFF10B981))
        val roseExpense = ColorProvider(Color(0xFFF43F5E))
        val surfaceBg = GlanceTheme.colors.surface
        val textMuted = GlanceTheme.colors.onSurfaceVariant

        val netColor = if (netPositive) emeraldIncome else roseExpense

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(surfaceBg)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity<MainActivity>())
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
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = "|",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = "flow",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = textMuted
                        ),
                        maxLines = 1
                    )
                    Spacer(GlanceModifier.defaultWeight())
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .size(24.dp)
                                .cornerRadius(12.dp)
                                .clickable(actionRunCallback<StatsRefreshAction>()),
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
                }

                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    val formattedNet = com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(kotlin.math.abs(net), currencySymbol)
                    val signedNet = if (netPositive) "+$formattedNet" else "-$formattedNet"
                    Text(
                        text = signedNet,
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = netColor
                        )
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "net this month",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = textMuted
                        )
                    )

                    Spacer(GlanceModifier.height(8.dp))

                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "↓ ${com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(income, currencySymbol)}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emeraldIncome
                                )
                            )
                        }
                        Spacer(GlanceModifier.width(4.dp))
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = "↑ ${com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(spent, currencySymbol)}",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = roseExpense
                                )
                            )
                        }
                    }
            }
        }
    }
}
}


class StatsRefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            .transactionRepository()
            .refreshWidgets()
    }
}
