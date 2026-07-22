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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
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
import kotlinx.coroutines.flow.map

class BudgetWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val budget = settings.monthlyBudget
        val accentColor = Color(settings.accentColor.colorValue)
        provideContent {
            val spent = currentState<Preferences>()[WidgetKeys.BUDGET_SPENT] ?: 0.0
            GlanceTheme {
                Content(spent = spent, budget = budget, brandColor = ColorProvider(accentColor))
            }
        }
    }

    @Composable
    private fun Content(spent: Double, budget: Double, brandColor: ColorProvider) {
        val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        val overBudget = spent > budget && budget > 0
        val remaining = budget - spent

        val accentLime = ColorProvider(Color(0xFF84CC16))
        val roseExpense = ColorProvider(Color(0xFFF43F5E))
        val amberWarning = ColorProvider(Color(0xFFF59E0B))
        val surfaceBg = GlanceTheme.colors.surface
        val textPrimary = GlanceTheme.colors.onSurface
        val textMuted = GlanceTheme.colors.onSurfaceVariant
        val trackBg = GlanceTheme.colors.surfaceVariant

        val statusColor = when {
            overBudget -> roseExpense
            progress >= 0.8f -> amberWarning
            else -> accentLime
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(surfaceBg)
                .cornerRadius(24.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp),
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
                            color = brandColor
                        )
                    )
                    Text(
                        text = "BUDGET",
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )
                    )
                    Box(
                        modifier = GlanceModifier
                            .padding(start = 6.dp)
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

                Spacer(GlanceModifier.height(6.dp))

                if (budget <= 0.0) {
                    Text(
                        text = "No budget set",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "Tap to set in settings",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    )
                } else {
                    Text(
                        text = "₹${fmt(spent)}",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    )
                    Spacer(GlanceModifier.height(1.dp))
                    Text(
                        text = "spent of ₹${fmt(budget)}",
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textMuted
                        )
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = GlanceModifier.fillMaxWidth().height(5.dp).cornerRadius(3.dp),
                        color = statusColor,
                        backgroundColor = trackBg
                    )
                    Spacer(GlanceModifier.height(6.dp))
                    Text(
                        text = if (overBudget) "Over by ₹${fmt(-remaining)}" else "₹${fmt(remaining)} remaining",
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

    private fun fmt(amount: Double): String {
        val abs = kotlin.math.abs(amount)
        return when {
            abs >= 1_00_000 -> "%.1fL".format(abs / 1_00_000)
            abs >= 1_000 -> "%.1fK".format(abs / 1_000)
            else -> "%.0f".format(abs)
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
