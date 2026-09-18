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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
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
import kotlinx.coroutines.flow.first
import java.util.Locale

class DailyAllowanceWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val accentColor = Color(settings.accentColor.colorValue)
        val currencySymbol = settings.currencySymbol
        val isPro = settings.isPro

        provideContent {
            val prefs = currentState<Preferences>()
            val dailySpent = prefs[WidgetKeys.DAILY_SPENT] ?: 0.0
            val dailyAllowance = prefs[WidgetKeys.DAILY_ALLOWANCE] ?: 0.0

            GlanceTheme {
                Content(
                    context = context,
                    isPro = isPro,
                    dailySpent = dailySpent,
                    dailyAllowance = dailyAllowance,
                    currencySymbol = currencySymbol,
                    brandColor = ColorProvider(accentColor)
                )
            }
        }
    }

    @Composable
    private fun Content(
        context: Context,
        isPro: Boolean,
        dailySpent: Double,
        dailyAllowance: Double,
        currencySymbol: String,
        brandColor: ColorProvider
    ) {
        val progress = if (dailyAllowance > 0) (dailySpent / dailyAllowance).toFloat().coerceIn(0f, 1f) else 0f
        val isOver = dailySpent > dailyAllowance && dailyAllowance > 0

        val statusColor = when {
            isOver -> WidgetColors.ExpenseRose
            progress >= 0.8f -> WidgetColors.AmberWarning
            else -> WidgetColors.IncomeEmerald
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.SurfaceBg)
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
                            color = WidgetColors.TextMuted
                        )
                    )
                    Spacer(GlanceModifier.width(4.dp))
                    Text(
                        text = "daily",
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = WidgetColors.TextMuted
                        ),
                        maxLines = 1
                    )
                }

                Spacer(GlanceModifier.height(4.dp))

                if (!isPro) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(WidgetColors.CardBg)
                            .cornerRadius(14.dp)
                            .padding(8.dp),
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
                                text = context.getString(R.string.widget_daily_allowance_description),
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = WidgetColors.TextMuted
                                ),
                                maxLines = 1
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = context.getString(R.string.widget_safe_to_spend_title),
                            style = TextStyle(
                                fontSize = 9.5.sp,
                                color = WidgetColors.TextMuted
                            ),
                            maxLines = 1
                        )

                        val safeToday = (dailyAllowance - dailySpent).coerceAtLeast(0.0)
                        Text(
                            text = "$currencySymbol${formatAmount(safeToday)}",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            ),
                            maxLines = 1
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .cornerRadius(2.dp),
                            color = statusColor,
                            backgroundColor = WidgetColors.CardBg
                        )

                        Spacer(GlanceModifier.height(4.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "$currencySymbol${formatAmount(dailySpent)}",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = WidgetColors.TextMuted
                                )
                            )
                            Spacer(GlanceModifier.defaultWeight())
                            Text(
                                text = "$currencySymbol${formatAmount(dailyAllowance)}",
                                style = TextStyle(
                                    fontSize = 9.sp,
                                    color = WidgetColors.TextMuted
                                )
                            )
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
