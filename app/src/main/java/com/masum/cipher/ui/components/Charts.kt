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

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val path = Path()
                val fillPath = Path()
                
                val maxVal = points.maxOf { it.y }.coerceAtLeast(1f)
                val minVal = points.minOf { it.y }.coerceAtMost(0f)
                val range = (maxVal - minVal).coerceAtLeast(1f)

                onDrawBehind {
                    val width = size.width
                    val height = size.height

                    points.forEachIndexed { index, point ->
                        val x = (index.toFloat() / (points.size - 1)) * width
                        val y = height - ((point.y - minVal) / range) * height

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = ((index - 1).toFloat() / (points.size - 1)) * width
                            val prevY = height - ((points[index - 1].y - minVal) / range) * height
                            
                            // Cubic Bezier for smooth curves
                            val controlX = (prevX + x) / 2
                            path.cubicTo(controlX, prevY, controlX, y, x, y)
                            fillPath.cubicTo(controlX, prevY, controlX, y, x, y)
                        }
                    }
                    
                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(ElectricIndigo.copy(alpha = 0.35f), Transparent)
                        )
                    )

                    // Draw Main Stroke with Animated Progress
                    drawPath(
                        path = path,
                        color = ElectricIndigo,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        ),
                        alpha = animationProgress.value
                    )
                }
            }
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
fun SpendingHeatmap(hours: List<InsightsContract.PeakHourData>) {
    val maxSpend = hours.maxOfOrNull { it.amount } ?: 1.0
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        hours.chunked(6).forEach { rowHours ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowHours.forEach { hour ->
                    val intensity = if (maxSpend > 0) (hour.amount / maxSpend).toFloat() else 0f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(
                                color = ElectricIndigo.copy(alpha = 0.05f + intensity * 0.8f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hour.label.take(2),
                            style = Typography.labelSmall,
                            color = if (intensity > 0.5f) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
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
