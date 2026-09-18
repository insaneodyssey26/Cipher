@file:Suppress("RestrictedApi")

package com.masum.cipher.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
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
import kotlinx.coroutines.flow.first

class QuickLogWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition
    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = UserPreferences(context).settingsFlow.first()
        val accentColor = Color(settings.accentColor.colorValue)
        val isPro = settings.isPro

        provideContent {
            val prefs = currentState<Preferences>()
            val page = prefs[WidgetKeys.QUICK_LOG_PAGE] ?: 0

            GlanceTheme {
                Content(
                    context = context,
                    isPro = isPro,
                    page = page,
                    brandColor = ColorProvider(accentColor)
                )
            }
        }
    }

    private data class QuickLogItem(
        val title: String,
        val categoryKey: String,
        val iconRes: Int
    )

    @Composable
    private fun Content(
        context: Context,
        isPro: Boolean,
        page: Int,
        brandColor: ColorProvider
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(WidgetColors.SurfaceBg)
                .cornerRadius(20.dp)
                .clickable(actionStartActivity<MainActivity>())
                .padding(horizontal = 7.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isPro) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    Text(
                        text = context.getString(R.string.widget_pro_locked_msg),
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WidgetColors.TextPrimary
                        )
                    )
                }
            } else {
                val page0Items = listOf(
                    QuickLogItem("FOOD", "FOOD", R.drawable.ic_widget_food),
                    QuickLogItem("SHOP", "SHOPPING", R.drawable.ic_widget_shopping),
                    QuickLogItem("RIDE", "TRANSPORT", R.drawable.ic_widget_ride),
                    QuickLogItem("BILLS", "BILLS", R.drawable.ic_widget_bills)
                )

                val page1Items = listOf(
                    QuickLogItem("FUN", "ENTERTAINMENT", R.drawable.ic_widget_entertainment),
                    QuickLogItem("HEALTH", "HEALTH", R.drawable.ic_widget_health),
                    QuickLogItem("INVEST", "INVESTMENT", R.drawable.ic_widget_invest),
                    QuickLogItem("MORE", "OTHERS", R.drawable.ic_widget_more)
                )

                val activeShortcuts = if (page % 2 == 1) page1Items else page0Items
                val categoryParamKey = ActionParameters.Key<String>("quick_log_category")

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                ) {
                    activeShortcuts.forEachIndexed { index, item ->
                        if (index > 0) Spacer(GlanceModifier.width(4.dp))
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .background(WidgetColors.CardBg)
                                .cornerRadius(11.dp)
                                .clickable(
                                    actionStartActivity<MainActivity>(
                                        actionParametersOf(categoryParamKey to item.categoryKey)
                                    )
                                )
                                .padding(vertical = 7.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.Vertical.CenterVertically,
                                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                            ) {
                                Image(
                                    provider = ImageProvider(item.iconRes),
                                    contentDescription = item.title,
                                    modifier = GlanceModifier.size(12.dp),
                                    colorFilter = ColorFilter.tint(WidgetColors.TextPrimary)
                                )
                                Spacer(GlanceModifier.width(3.dp))
                                Text(
                                    text = item.title,
                                    style = TextStyle(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WidgetColors.TextPrimary
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(GlanceModifier.width(4.dp))

                    Box(
                        modifier = GlanceModifier
                            .size(28.dp)
                            .background(WidgetColors.CardBgElevated)
                            .cornerRadius(10.dp)
                            .clickable(actionRunCallback<QuickLogPageAction>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_widget_next),
                            contentDescription = "Next Categories",
                            modifier = GlanceModifier.size(12.dp),
                            colorFilter = ColorFilter.tint(brandColor)
                        )
                    }
                }
            }
        }
    }
}

class QuickLogPageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val currentPage = prefs[WidgetKeys.QUICK_LOG_PAGE] ?: 0
            val nextPage = (currentPage + 1) % 2
            prefs.toMutablePreferences().apply {
                this[WidgetKeys.QUICK_LOG_PAGE] = nextPage
            }
        }
        QuickLogWidget().update(context, glanceId)
    }
}
