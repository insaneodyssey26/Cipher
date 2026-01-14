package com.example.cipherspend.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cipherspend.ui.dashboard.DashboardContract
import com.example.cipherspend.ui.theme.ExpenseRed
import com.example.cipherspend.ui.theme.IncomeGreen
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.*

@Composable
fun VelocityMetric(data: DashboardContract.VelocityData) {
    val isIncreased = data.trendPercentage > 0
    val trendColor = if (isIncreased) ExpenseRed else IncomeGreen
    val icon = if (isIncreased) Icons.Default.TrendingUp else Icons.Default.TrendingDown

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(trendColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = trendColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Spending Velocity",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${"%.0f".format(data.currentWeekAvg)}/day",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncreased) "+" else ""}${"%.1f".format(data.trendPercentage)}%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = trendColor
                )
                Text(
                    text = "vs last week",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun NetWorthChart(points: List<DashboardContract.Point>) {
    if (points.isEmpty()) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Net Worth Trend",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val maxNetWorth = points.maxOf { it.y }.coerceAtLeast(1f)
            val minNetWorth = points.minOf { it.y }.coerceAtMost(0f)
            val range = maxNetWorth - minNetWorth

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, point ->
                val x = (index.toFloat() / (points.size - 1)) * width
                val y = height - ((point.y - minNetWorth) / range) * height

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = ((index - 1).toFloat() / (points.size - 1)) * width
                    val prevY = height - ((points[index - 1].y - minNetWorth) / range) * height

                    // Bezier curve
                    val controlX1 = (prevX + x) / 2
                    path.cubicTo(controlX1, prevY, controlX1, y, x, y)
                    fillPath.cubicTo(controlX1, prevY, controlX1, y, x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            // Draw shadow/gradient fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                )
            )

            // Draw line with animation
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                alpha = animationProgress.value
            )
        }
    }
}

@Composable
fun CalendarHeatmap(data: Map<Long, Double>) {
    val days = 30
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.timeInMillis

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Spending Intensity",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 7
        ) {
            val maxSpend = data.values.maxOfOrNull { it } ?: 1.0

            for (i in (days - 1) downTo 0) {
                val time = today - TimeUnit.DAYS.toMillis(i.toLong())
                val spend = data[time] ?: 0.0
                val alpha = if (spend > 0) (0.2f + (spend / maxSpend).toFloat() * 0.8f).coerceAtMost(1f) else 0.05f

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(IncomeGreen.copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun CategoryDoughnutChart(categories: List<DashboardContract.CategoryData>) {
    var explodedIndex by remember { mutableIntStateOf(-1) }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Spending Vault",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp).pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Logic to detect which slice was tapped could be added here
                }
            }) {
                var startAngle = -90f
                categories.forEachIndexed { index, category ->
                    val sweepAngle = category.percentage * 360f
                    val isExploded = index == explodedIndex
                    val radiusOffset = if (isExploded) 10.dp.toPx() else 0f

                    drawArc(
                        color = Color(category.color),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Round),
                        size = Size(size.width - radiusOffset, size.height - radiusOffset),
                        topLeft = Offset(radiusOffset / 2, radiusOffset / 2)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${categories.size}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
