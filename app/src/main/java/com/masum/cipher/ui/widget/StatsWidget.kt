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
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masum.cipher.MainActivity
import com.masum.cipher.core.data.local.pref.WidgetKeys
import com.masum.cipher.core.di.WidgetEntryPoint
import dagger.hilt.android.EntryPointAccessors

class StatsWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val spent = prefs[WidgetKeys.STATS_SPENT] ?: 0.0
            val income = prefs[WidgetKeys.STATS_INCOME] ?: 0.0
            GlanceTheme {
                Content(spent = spent, income = income)
            }
        }
    }

    @Composable
    private fun Content(spent: Double, income: Double) {
        val net = income - spent
        val netPositive = net >= 0
        val netColor = ColorProvider(if (netPositive) Color(0xFF4CAF50) else Color(0xFFE53935))
        val incomeColor = ColorProvider(Color(0xFF4CAF50))
        val expenseColor = ColorProvider(Color(0xFFE53935))
        val cipherBlue = ColorProvider(Color(0xFF4E6CF7))

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<MainActivity>())
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "cipher",
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = cipherBlue
                        )
                    )
                    Text(
                        text = "↻",
                        modifier = GlanceModifier.clickable(actionRunCallback<StatsRefreshAction>()),
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }

                Spacer(GlanceModifier.height(12.dp))

                Text(
                    text = "${if (netPositive) "+" else "−"}₹${fmt(net)}",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "net this month",
                    style = TextStyle(
                        fontSize = 11.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )

                Spacer(GlanceModifier.height(14.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "↑ ₹${fmt(income)}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = incomeColor
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "income",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                    Spacer(GlanceModifier.width(8.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "↓ ₹${fmt(spent)}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = expenseColor
                            )
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "spent",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }

    private fun fmt(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        return when {
            abs >= 1_00_000 -> "%.1fL".format(abs / 1_00_000)
            abs >= 1_000 -> "%.1fK".format(abs / 1_000)
            else -> "%.0f".format(abs)
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
