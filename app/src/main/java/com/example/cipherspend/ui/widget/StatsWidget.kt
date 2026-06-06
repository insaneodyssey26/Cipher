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
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.masum.cipher.MainActivity
import com.masum.cipher.core.data.local.pref.WidgetDataStore
import kotlinx.coroutines.flow.first

class StatsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val spent = WidgetDataStore.spentFlow(context).first()
        val income = WidgetDataStore.incomeFlow(context).first()
        provideContent {
            GlanceTheme {
                Content(spent = spent, income = income)
            }
        }
    }

    @Composable
    private fun Content(spent: Double, income: Double) {
        val net = income - spent
        val netColor = if (net >= 0) ColorProvider(Color(0xFF4CAF50)) else ColorProvider(Color(0xFFE53935))

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
                    text = "This Month",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )

                Spacer(GlanceModifier.height(10.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "↑",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF4CAF50))
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Column {
                        Text(
                            text = "₹${fmt(income)}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFF4CAF50))
                            )
                        )
                        Text(
                            text = "earned",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.height(6.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "↓",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFFE53935))
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Column {
                        Text(
                            text = "₹${fmt(spent)}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFFE53935))
                            )
                        )
                        Text(
                            text = "spent",
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                Text(
                    text = "${if (net >= 0) "+" else ""}₹${fmt(net)} net",
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = netColor
                    )
                )
            }
        }
    }

    private fun fmt(amount: Double): String {
        val abs = Math.abs(amount)
        return when {
            abs >= 1_00_000 -> "%.1fL".format(abs / 1_00_000)
            abs >= 1_000 -> "%.1fK".format(abs / 1_000)
            else -> "%.0f".format(abs)
        }
    }
}
