package com.masum.cipher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
                    lineColor = ElectricIndigo,
                    lineBackgroundShader = DynamicShaders.fromBrush(
                        brush = Brush.verticalGradient(
                            colors = listOf(ElectricIndigo.copy(alpha = 0.4f), Transparent)
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
                    color = ElectricIndigo,
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
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val today = calendar.timeInMillis
    val maxSpend = data.values.maxOfOrNull { it } ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 4) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val time = today - (27 - index) * 24 * 60 * 60 * 1000L
                    val spend = data[time] ?: 0.0
                    val intensity = if (maxSpend > 0) (spend / maxSpend).toFloat() else 0f
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = if (spend > 0) ElectricIndigo.copy(alpha = 0.1f + intensity * 0.9f) else White10,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onDayClick(time) }
                    )
                }
            }
        }
    }
}
