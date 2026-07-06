package com.masum.cipher.ui.insights

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.domain.SubscriptionDetector
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.ui.components.*
import com.masum.cipher.ui.theme.*
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val haptic = LocalHapticFeedback.current

    val isHapticsEnabled = settings?.isHapticsEnabled ?: true

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Intelligence",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { MonthlySummaryCard(summary = state.monthlySummary) }

            item { VelocityMetric(data = state.spendingVelocity) }

            item { QuickStatsRow(streak = state.noSpendStreak, avgSize = state.avgTransactionSize) }

            if (state.topMerchants.isNotEmpty()) {
                item { TopMerchantsCard(merchants = state.topMerchants) }
            }

            if (state.weekdayBreakdown.isNotEmpty()) {
                item { WeekdayBreakdownCard(days = state.weekdayBreakdown) }
            }

            if (state.peakHours.any { it.amount > 0.0 }) {
                item { PeakHoursCard(hours = state.peakHours) }
            }

            item { NetWorthChart(points = state.netWorthHistory) }

            item { CategoryDoughnutChart(categories = state.categoryBreakdown) }

            if (state.detectedSubscriptions.isNotEmpty()) {
                item {
                    SubscriptionSection(
                        subscriptions = state.detectedSubscriptions,
                        isHapticsEnabled = isHapticsEnabled
                    )
                }
            }

            item {
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

@Composable
fun SubscriptionSection(
    subscriptions: List<SubscriptionDetector.Subscription>,
    isHapticsEnabled: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "DETECTED SUBSCRIPTIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            subscriptions.forEach { sub ->
                SubscriptionItem(sub = sub, isHapticsEnabled = isHapticsEnabled)
            }
        }
    }
}

@Composable
fun SubscriptionItem(
    sub: SubscriptionDetector.Subscription,
    isHapticsEnabled: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .clickable { if (isHapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = sub.category.color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = sub.category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = sub.category.color
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sub.merchant,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Next expected: ${AppFormatters.getDay().format(Date(sub.nextExpectedDate))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = AppFormatters.getCurrencyNoDecimals().format(sub.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Monthly",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
