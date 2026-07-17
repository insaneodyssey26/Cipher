package com.masum.cipher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import com.masum.cipher.ui.theme.*
import java.util.Calendar
import kotlinx.coroutines.launch
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes

/**
 * Premium Spending Trend Chart
 * 
 * - Animated line draw (Cubic Bezier)
 * - Gradient fill below the line
 * - Glow effect on the primary stroke
 */
@Composable
fun SpendingTrendChart(points: List<DashboardContract.Point>) {
    if (points.isEmpty()) return

    val chartEntryModel = entryModelOf(
        points.mapIndexed { index, point -> 
            FloatEntry(x = index.toFloat(), y = point.y)
        }
    )

    Chart(
        chart = lineChart(
            lines = listOf(
                com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                    lineColor = MaterialTheme.colorScheme.primary,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Transparent)
                        )
                    )
                )
            )
        ),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            valueFormatter = AxisValueFormatter { value, _ -> "₹${value.toInt()}" },
            label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            axis = null,
            tick = null,
            guideline = com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        ),
        bottomAxis = rememberBottomAxis(
            label = null,
            axis = null,
            tick = null,
            guideline = null
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 24.dp)
    )
}

/**
 * Thick Donut Chart for Category Allocation
 */
@Composable
fun CategoryAllocationDonut(categories: List<DashboardContract.CategoryData>) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animationProgress.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                categories.forEach { category ->
                    val sweepAngle = category.percentage * 360f * animationProgress.value
                    drawArc(
                        color = Color(category.color),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                        topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }
            Text(
                text = "${categories.size}\nCATEGORIES",
                style = Typography.labelSmall.copy(textAlign = TextAlign.Center),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(4).forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(category.color), CircleShape))
                    Text(text = category.category, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    Text(text = "${(category.percentage * 100).toInt()}%", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Spending Heatmap for Peak Hours
 */
@Composable
fun PeakHoursChart(hours: List<InsightsContract.PeakHourData>) {
    if (hours.isEmpty()) return

    val chartEntryModel = entryModelOf(
        hours.mapIndexed { index, hour ->
            FloatEntry(x = index.toFloat(), y = hour.amount.toFloat())
        }
    )

    Chart(
        chart = columnChart(
            columns = listOf(
                com.patrykandpatrick.vico.compose.component.lineComponent(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 16.dp,
                    shape = Shapes.roundedCornerShape(topLeftPercent = 50, topRightPercent = 50)
                )
            )
        ),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            valueFormatter = AxisValueFormatter { value, _ -> "₹${value.toInt()}" },
            label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            axis = null,
            tick = null,
            guideline = com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 1.dp
            )
        ),
        bottomAxis = rememberBottomAxis(
            valueFormatter = AxisValueFormatter { value, _ ->
                hours.getOrNull(value.toInt())?.label ?: ""
            },
            label = com.patrykandpatrick.vico.compose.axis.axisLabelComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            axis = null,
            tick = null,
            guideline = null
        ),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 24.dp)
    )
}

@Composable
fun CalendarHeatmap(
    data: Map<Long, Double>,
    selectedTimestamp: Long?,
    onDayClick: (Long) -> Unit
) {
    val todayCal = remember { Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }}
    val currentMonthIndex = 11
    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = currentMonthIndex,
        pageCount = { 12 }
    )
    val coroutineScope = rememberCoroutineScope()
    val maxSpend = remember(data) { data.values.maxOfOrNull { it } ?: 1.0 }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val visibleMonthCal = remember(pagerState.currentPage) {
                Calendar.getInstance().apply {
                    add(Calendar.MONTH, pagerState.currentPage - currentMonthIndex)
                }
            }
            Text(
                text = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(visibleMonthCal.time),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    imageVector = LucideIcons.ChevronLeft,
                    contentDescription = "Previous Month",
                    tint = if (pagerState.currentPage > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp).clickable(
                        enabled = pagerState.currentPage > 0,
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                )
                Icon(
                    imageVector = LucideIcons.ChevronRight,
                    contentDescription = "Next Month",
                    tint = if (pagerState.currentPage < 11) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp).clickable(
                        enabled = pagerState.currentPage < 11,
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val monthOffset = page - currentMonthIndex
            val pageCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            val daysInMonth = pageCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = pageCal.get(Calendar.DAY_OF_WEEK) - 1 
            val rows = 6 // Force exactly 6 rows to prevent height jumping
            
            val prevMonthCal = Calendar.getInstance().apply {
                timeInMillis = pageCal.timeInMillis
                add(Calendar.MONTH, -1)
            }
            val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val dayNum = row * 7 + col - firstDayOfWeek + 1
                            
                            val (displayDayNum, isCurrentMonth, cellCal) = when {
                                dayNum < 1 -> {
                                    val d = daysInPrevMonth + dayNum
                                    val c = Calendar.getInstance().apply {
                                        timeInMillis = prevMonthCal.timeInMillis
                                        set(Calendar.DAY_OF_MONTH, d)
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }
                                    Triple(d, false, c)
                                }
                                dayNum > daysInMonth -> {
                                    val d = dayNum - daysInMonth
                                    val c = Calendar.getInstance().apply {
                                        timeInMillis = pageCal.timeInMillis
                                        add(Calendar.MONTH, 1)
                                        set(Calendar.DAY_OF_MONTH, d)
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }
                                    Triple(d, false, c)
                                }
                                else -> {
                                    val c = Calendar.getInstance().apply {
                                        timeInMillis = pageCal.timeInMillis
                                        set(Calendar.DAY_OF_MONTH, dayNum)
                                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                    }
                                    Triple(dayNum, true, c)
                                }
                            }
                            
                            val time = cellCal.timeInMillis
                            val spend = data[time] ?: 0.0
                            val intensity = if (maxSpend > 0) (spend / maxSpend).toFloat() else 0f
                            
                            val isSelected = selectedTimestamp == time
                            val isToday = time == todayCal.timeInMillis

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        color = if (!isCurrentMonth) Color.Transparent 
                                                else if (spend > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f + intensity * 0.8f) 
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = if (isSelected || isToday) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else if (isToday) MaterialTheme.colorScheme.outline else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable(
                                        enabled = isCurrentMonth,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) { onDayClick(time) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = displayDayNum.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (!isCurrentMonth) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                            else if (spend > 0) Color(0xFF0D0D1A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected || isToday || spend > 0) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

