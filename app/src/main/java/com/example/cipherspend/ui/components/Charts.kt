package com.example.cipherspend.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cipherspend.ui.dashboard.DashboardContract
import com.example.cipherspend.ui.theme.ExpenseRed
import com.example.cipherspend.ui.theme.IncomeGreen
import java.util.Calendar
import java.util.concurrent.TimeUnit

@Composable
fun VelocityMetric(data: DashboardContract.VelocityData) {
    val isIncreased = data.trendPercentage > 0
    val trendColor = if (isIncreased) ExpenseRed else IncomeGreen
    val icon = if (isIncreased) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = trendColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = trendColor
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Average",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹${"%.0f".format(data.currentWeekAvg)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Badge(
                containerColor = trendColor.copy(alpha = 0.1f),
                contentColor = trendColor,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "${if (isIncreased) "+" else ""}${"%.1f".format(data.trendPercentage)}%",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Spending Trend",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
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
                    colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent)
                )
            )

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
fun CalendarHeatmap(
    data: Map<Long, Double>,
    selectedTimestamp: Long?,
    onDayClick: (Long) -> Unit
) {
    val days = 28 
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val today = calendar.timeInMillis

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Activity Density",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val availableWidth = maxWidth
            val itemSize = (availableWidth - (6 * 6).dp) / 7
            
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val maxSpend = data.values.maxOfOrNull { it } ?: 1.0
                
                for (row in 0 until 4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val time = today - TimeUnit.DAYS.toMillis((days - 1 - index).toLong())
                            val spend = data[time] ?: 0.0
                            val alpha = if (spend > 0) (0.2f + (spend / maxSpend).toFloat() * 0.8f).coerceAtMost(1f) else 0.05f
                            
                            val isSelected = selectedTimestamp?.let { sel ->
                                val selCal = Calendar.getInstance().apply { timeInMillis = sel }
                                val dayCal = Calendar.getInstance().apply { timeInMillis = time }
                                selCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                                selCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
                            } ?: false

                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                label = "border"
                            )

                            Box(
                                modifier = Modifier
                                    .size(itemSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                                    .border(
                                        width = 2.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onDayClick(time) }
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
            text = "Allocation",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier.size(140.dp),
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
                            style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round),
                            size = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx()),
                            topLeft = Offset(8.dp.toPx(), 8.dp.toPx())
                        )
                        startAngle += sweepAngle
                    }
                }
                
                Text(
                    text = "${categories.size}\nTypes",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(category.color))
        )
        Text(
            text = category.category,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = "${(category.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
