package com.example.cipherspend.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(trendColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = trendColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DAILY VELOCITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "₹${"%.0f".format(data.currentWeekAvg)}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = trendColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${if (isIncreased) "+" else ""}${"%.1f".format(data.trendPercentage)}%",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                    color = trendColor
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
        animationProgress.animateTo(1f, tween(2000, easing = FastOutSlowInEasing))
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "EQUITY GROWTH",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val width = size.width
            val height = size.height
            val maxNetWorth = points.maxOf { it.y }.coerceAtLeast(1f)
            val minNetWorth = points.minOf { it.y }.coerceAtMost(0f)
            val range = (maxNetWorth - minNetWorth).coerceAtLeast(1f)

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { index, point ->
                val x = (index.toFloat() / (points.size - 1).coerceAtLeast(1)) * width
                val y = height - ((point.y - minNetWorth) / range) * height

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = ((index - 1).toFloat() / (points.size - 1)) * width
                    val prevY = height - ((points[index - 1].y - minNetWorth) / range) * height
                    val controlX = (prevX + x) / 2
                    path.cubicTo(controlX, prevY, controlX, y, x, y)
                    fillPath.cubicTo(controlX, prevY, controlX, y, x, y)
                }
            }

            fillPath.lineTo(width, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent)
                )
            )

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
                alpha = animationProgress.value
            )
        }
    }
}

@Composable
fun CalendarHeatmap(data: Map<Long, Double>) {
    val days = 28 // 4 weeks exactly for a cleaner grid
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.timeInMillis

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "ACTIVITY DENSITY",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val itemSize = (maxWidth - (6 * 6).dp) / 7
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val maxSpend = data.values.maxOfOrNull { it } ?: 1.0
                
                for (row in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val time = today - TimeUnit.DAYS.toMillis((days - 1 - index).toLong())
                            val spend = data[time] ?: 0.0
                            val alpha = if (spend > 0) (0.15f + (spend / maxSpend).toFloat() * 0.85f).coerceAtMost(1f) else 0.05f
                            
                            Box(
                                modifier = Modifier
                                    .size(itemSize)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IncomeGreen.copy(alpha = alpha))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDoughnutChart(categories: List<DashboardContract.CategoryData>) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "ALLOCATION",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var startAngle = -90f
                    categories.forEach { category ->
                        val sweepAngle = category.percentage * 360f
                        drawArc(
                            color = Color(category.color),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                            topLeft = Offset(12.dp.toPx(), 12.dp.toPx())
                        )
                        startAngle += sweepAngle
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${categories.size}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.take(4).forEach { category ->
                    CategoryLegendItem(category)
                }
            }
        }
    }
}

@Composable
fun CategoryLegendItem(category: DashboardContract.CategoryData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(category.color))
        )
        Text(
            text = category.category,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${(category.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
