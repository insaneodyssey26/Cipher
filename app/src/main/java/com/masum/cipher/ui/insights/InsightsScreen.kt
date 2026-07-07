package com.masum.cipher.ui.insights

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Clock
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit,
    onNavigateToDayDetail: (Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by userPreferences.settingsFlow.collectAsState(initial = null)
    val haptic = LocalHapticFeedback.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    TimeSelectorDropdown(
                        selectedPeriod = state.selectedTimePeriod,
                        onPeriodSelected = { period ->
                            if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.handleIntent(InsightsContract.Intent.SetTimePeriod(period))
                        }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Narrative Hero
            item {
                InsightHero(state = state)
            }

            // 2. Spending Trend Chart
            item {
                SectionLabel("SPENDING TREND")
                VaultCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(280.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 0.dp
                ) {
                    SpendingTrendChart(points = state.netWorthHistory)
                }
            }

            // 3. Category Allocation
            item {
                SectionLabel("CATEGORY ALLOCATION")
                VaultCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    CategoryAllocationDonut(categories = state.categoryBreakdown)
                }
            }

            // 4. Heatmap/Peak Hours
            item {
                SectionLabel("PEAK SPENDING HOURS")
                VaultCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(220.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 0.dp
                ) {
                    PeakHoursChart(hours = state.peakHours)
                }
            }

            // 5. Subscriptions
            if (state.detectedSubscriptions.isNotEmpty()) {
                item {
                    SectionLabel("SUBSCRIPTIONS")
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.detectedSubscriptions.forEach { sub ->
                            SubscriptionItem(sub = sub, isHapticsEnabled = isHapticsEnabled)
                        }
                    }
                }
            }

            // 6. Calendar History
            item {
                SectionLabel("ACTIVITY CALENDAR")
                VaultCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentPadding = 16.dp
                ) {
                    CalendarHeatmap(
                        data = state.calendarHeatmap,
                        selectedTimestamp = state.selectedDayTimestamp,
                        onDayClick = { timestamp ->
                            if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToDayDetail(timestamp)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightHero(state: InsightsContract.State) {
    val mostExpensiveCategory = state.categoryBreakdown.maxByOrNull { it.amount }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricIndigo.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(ElectricIndigoSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(LucideIcons.TrendingUp, null, tint = ElectricIndigo, modifier = Modifier.size(32.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (mostExpensiveCategory != null) {
                "You've spent the most on ${mostExpensiveCategory.category.toString().lowercase().replaceFirstChar { it.uppercase() }} this month."
            } else {
                "Your financial story is just beginning."
            },
            style = Typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Keep track of your spending to see patterns emerge.",
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InsightMetric(label = "NO-SPEND STREAK", value = "${state.noSpendStreak} Days", icon = LucideIcons.Calendar)
            InsightMetric(label = "AVG SPEND / TXN", value = "₹${state.avgTransactionSize.toInt()}", icon = LucideIcons.Clock)
        }
    }
}

@Composable
private fun InsightMetric(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = Typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, style = Typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = Typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 24.dp, top = 32.dp, bottom = 12.dp)
    )
}

@Composable
fun SubscriptionItem(
    sub: SubscriptionDetector.Subscription,
    isHapticsEnabled: Boolean
) {
    val haptic = LocalHapticFeedback.current

    VaultCard(
        onClick = { if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(sub.category.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = sub.category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = sub.category.color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.merchant,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Expected: ${AppFormatters.getDay().format(Date(sub.nextExpectedDate))}",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${sub.amount.toInt()}",
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Monthly",
                    style = Typography.labelSmall,
                    color = ElectricIndigo
                )
            }
        }
    }
}
