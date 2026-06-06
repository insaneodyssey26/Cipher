package com.masum.cipher.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masum.cipher.MainActivity
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.data.local.pref.WidgetDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class BudgetWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val budget = UserPreferences(context).settingsFlow.map { it.monthlyBudget }.first()
        val spent = WidgetDataStore.spentFlow(context).first()
        provideContent {
            GlanceTheme {
                Content(spent = spent, budget = budget)
            }
        }
    }

    @Composable
    private fun Content(spent: Double, budget: Double) {
        val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        val overBudget = spent > budget && budget > 0

        val indicatorColor = when {
            overBudget -> ColorProvider(Color(0xFFE53935))
            progress >= 0.8f -> ColorProvider(Color(0xFFFFB300))
            else -> ColorProvider(Color(0xFF4CAF50))
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    text = "Budget",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                Spacer(GlanceModifier.height(8.dp))

                if (budget <= 0.0) {
                    Text(
                        text = "No budget set.\nTap to configure.",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                } else {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = GlanceModifier.fillMaxWidth().height(8.dp),
                        color = indicatorColor,
                        backgroundColor = ColorProvider(Color(0x224CAF50))
                    )

                    Spacer(GlanceModifier.height(8.dp))

                    Text(
                        text = "₹${fmt(spent)} spent",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )

                    Text(
                        text = "of ₹${fmt(budget)}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    Text(
                        text = if (overBudget) "Over budget!" else "₹${fmt(budget - spent)} left",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = indicatorColor
                        )
                    )
                }
            }
        }
    }

    private fun fmt(amount: Double): String = when {
        amount >= 1_00_000 -> "%.1fL".format(amount / 1_00_000)
        amount >= 1_000 -> "%.1fK".format(amount / 1_000)
        else -> "%.0f".format(amount)
    }
}
