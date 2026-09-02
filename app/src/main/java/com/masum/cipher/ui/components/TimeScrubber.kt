package com.masum.cipher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.TimePeriod
import com.masum.cipher.core.domain.model.TimeRange
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.Clock
import compose.icons.lucideicons.Target
import compose.icons.lucideicons.TrendingUp
import compose.icons.lucideicons.X
import compose.icons.lucideicons.Zap
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalLocale
import com.masum.cipher.core.util.AppFormatters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSelectorDropdown(
    selectedPeriod: TimePeriod,
    selectedTimeRange: TimeRange? = null,
    onPeriodSelected: (TimePeriod, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier,
    isHapticsEnabled: Boolean = true,
    iconOnly: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val view = androidx.compose.ui.platform.LocalView.current
    val locale = LocalLocale.current.platformLocale
    val rotation by animateFloatAsState(targetValue = if (showDialog) 180f else 0f, label = "caret_rot")

    val triggerLabel = if (selectedPeriod == TimePeriod.CUSTOM && selectedTimeRange != null && selectedTimeRange.startTime > 0L) {
        val sdf = SimpleDateFormat("MMM d", locale)
        "${sdf.format(Date(selectedTimeRange.startTime))} – ${sdf.format(Date(selectedTimeRange.endTime))}"
    } else {
        stringResource(selectedPeriod.labelRes)
    }

    Box(modifier = modifier) {
        if (iconOnly) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        showDialog = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Calendar,
                    contentDescription = "Select Time Range",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        showDialog = true
                    }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LucideIcons.Calendar,
                    contentDescription = "Select Time Range",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = triggerLabel,
                    style = Typography.labelMedium.copy(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = LucideIcons.ChevronDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation)
                )
            }
        }

        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showDialog = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 420.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {},
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.timeframe_title),
                                        style = Typography.titleLarge.copy(
                                            fontFamily = Manrope,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.timeframe_subtitle),
                                        style = Typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        showDialog = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.X,
                                        contentDescription = stringResource(R.string.action_close),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            val context = androidx.compose.ui.platform.LocalContext.current
                            val periods = listOf(
                                Pair(TimePeriod.THIS_MONTH, Pair(LucideIcons.Calendar, getPeriodDateSubtitle(TimePeriod.THIS_MONTH, context, locale))),
                                Pair(TimePeriod.LAST_MONTH, Pair(LucideIcons.Clock, getPeriodDateSubtitle(TimePeriod.LAST_MONTH, context, locale))),
                                Pair(TimePeriod.THIS_WEEK, Pair(LucideIcons.Zap, getPeriodDateSubtitle(TimePeriod.THIS_WEEK, context, locale))),
                                Pair(TimePeriod.LAST_WEEK, Pair(LucideIcons.Clock, getPeriodDateSubtitle(TimePeriod.LAST_WEEK, context, locale))),
                                Pair(TimePeriod.THIS_YEAR, Pair(LucideIcons.TrendingUp, getPeriodDateSubtitle(TimePeriod.THIS_YEAR, context, locale))),
                                Pair(TimePeriod.ALL_TIME, Pair(LucideIcons.Target, getPeriodDateSubtitle(TimePeriod.ALL_TIME, context, locale)))
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (i in periods.indices step 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        for (j in 0..1) {
                                            if (i + j < periods.size) {
                                                val (period, iconAndSubtitle) = periods[i + j]
                                                val isSelected = selectedPeriod == period

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                        )
                                                        .border(
                                                            width = if (isSelected) 1.5.dp else 1.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                                            shape = RoundedCornerShape(20.dp)
                                                        )
                                                        .clickable {
                                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                            showDialog = false
                                                            onPeriodSelected(period, null, null)
                                                        }
                                                        .padding(14.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.Top
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(34.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = iconAndSubtitle.first,
                                                                contentDescription = null,
                                                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }

                                                        if (isSelected) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(18.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = LucideIcons.Check,
                                                                    contentDescription = null,
                                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Column(modifier = Modifier.padding(top = 44.dp)) {
                                                        Text(
                                                            text = stringResource(period.labelRes),
                                                            style = Typography.titleSmall.copy(
                                                                fontFamily = Manrope,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                fontSize = 14.sp
                                                            ),
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = iconAndSubtitle.second,
                                                            style = Typography.bodySmall.copy(
                                                                fontSize = 11.sp,
                                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                val isCustomSelected = selectedPeriod == TimePeriod.CUSTOM
                                val defaultCustomDesc = stringResource(R.string.period_custom_range_desc)
                                val customSubtitle = if (isCustomSelected && selectedTimeRange != null && selectedTimeRange.startTime > 0L) {
                                    val sdf = SimpleDateFormat("MMM d, yyyy", locale)
                                    "${sdf.format(Date(selectedTimeRange.startTime))} – ${sdf.format(Date(selectedTimeRange.endTime))}"
                                } else {
                                    defaultCustomDesc
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isCustomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        )
                                        .border(
                                            width = if (isCustomSelected) 1.5.dp else 1.dp,
                                            color = if (isCustomSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            showDateRangePicker = true
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCustomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.Calendar,
                                                    contentDescription = null,
                                                    tint = if (isCustomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = stringResource(R.string.period_custom_range),
                                                    style = Typography.titleSmall.copy(
                                                        fontFamily = Manrope,
                                                        fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                        fontSize = 14.sp
                                                    ),
                                                    color = if (isCustomSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = customSubtitle,
                                                    style = Typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = if (isCustomSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                )
                                            }
                                        }

                                        if (isCustomSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDateRangePicker) {
            val initialStart = if (selectedPeriod == TimePeriod.CUSTOM && selectedTimeRange != null && selectedTimeRange.startTime > 0L) {
                selectedTimeRange.startTime
            } else null
            val initialEnd = if (selectedPeriod == TimePeriod.CUSTOM && selectedTimeRange != null && selectedTimeRange.endTime > 0L) {
                selectedTimeRange.endTime
            } else null

            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val dateRangePickerState = rememberDateRangePickerState(
                initialSelectedStartDateMillis = initialStart,
                initialSelectedEndDateMillis = initialEnd,
                initialDisplayedMonthMillis = initialStart ?: System.currentTimeMillis(),
                yearRange = (currentYear - 5)..(currentYear + 2)
            )
            val datePickerColors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.primary,
                subheadContentColor = MaterialTheme.colorScheme.primary,
                navigationContentColor = MaterialTheme.colorScheme.primary,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
            )

            DatePickerDialog(
                onDismissRequest = { showDateRangePicker = false },
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 0.dp,
                colors = datePickerColors,
                confirmButton = {
                    TextButton(
                        onClick = {
                            val start = dateRangePickerState.selectedStartDateMillis
                            val end = dateRangePickerState.selectedEndDateMillis
                            if (start != null && end != null) {
                                val endCal = Calendar.getInstance().apply {
                                    timeInMillis = end
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                showDateRangePicker = false
                                showDialog = false
                                onPeriodSelected(TimePeriod.CUSTOM, start, endCal.timeInMillis)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.apply),
                            style = Typography.labelLarge.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDateRangePicker = false }) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = Typography.labelLarge.copy(fontFamily = Manrope),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            ) {
                DateRangePicker(
                    state = dateRangePickerState,
                    title = {
                        Text(
                            text = stringResource(R.string.select_date_range),
                            style = Typography.titleMedium.copy(
                                fontFamily = Manrope,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                        )
                    },
                    headline = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        val sdf = SimpleDateFormat("MMM d, yyyy", locale)
                        val startDatePlaceholder = stringResource(R.string.start_date)
                        val endDatePlaceholder = stringResource(R.string.end_date)
                        val rangeText = if (start != null && end != null) {
                            "${sdf.format(Date(start))} – ${sdf.format(Date(end))}"
                        } else if (start != null) {
                            "${sdf.format(Date(start))} – $endDatePlaceholder"
                        } else "$startDatePlaceholder – $endDatePlaceholder"

                        Text(
                            text = rangeText,
                            style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                        )
                    },
                    showModeToggle = false,
                    colors = datePickerColors
                )
            }
        }
    }
}

@Composable
fun TimeSelectorDropdown(
    selectedPeriod: TimePeriod,
    selectedTimeRange: TimeRange? = null,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
    isHapticsEnabled: Boolean = true,
    iconOnly: Boolean = false
) {
    TimeSelectorDropdown(
        selectedPeriod = selectedPeriod,
        selectedTimeRange = selectedTimeRange,
        onPeriodSelected = { period, _, _ -> onPeriodSelected(period) },
        modifier = modifier,
        isHapticsEnabled = isHapticsEnabled,
        iconOnly = iconOnly
    )
}

private fun getPeriodDateSubtitle(period: TimePeriod, context: android.content.Context? = null, locale: Locale = Locale.getDefault()): String {
    val range = TimeRange.from(period)
    val sdf = AppFormatters.getDay(locale)
    val sdfYear = SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "yyyy"), locale)
    return when (period) {
        TimePeriod.THIS_WEEK, TimePeriod.LAST_WEEK -> {
            "${sdf.format(Date(range.startTime))} – ${sdf.format(Date(range.endTime))}"
        }
        TimePeriod.THIS_MONTH, TimePeriod.LAST_MONTH -> {
            val sdfMonth = SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "MMMM"), locale)
            sdfMonth.format(Date(range.startTime))
        }
        TimePeriod.THIS_YEAR -> {
            sdfYear.format(Date(range.startTime))
        }
        TimePeriod.ALL_TIME -> context?.getString(R.string.all_records) ?: "All Records"
        TimePeriod.CUSTOM -> context?.getString(R.string.period_custom_range) ?: "Custom Range"
    }
}
