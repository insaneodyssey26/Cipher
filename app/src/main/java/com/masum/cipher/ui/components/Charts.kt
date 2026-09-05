package com.masum.cipher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.dashboard.DashboardContract
import com.masum.cipher.ui.insights.InsightsContract
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun CategoryAllocationDonut(
    categories: List<DashboardContract.CategoryData>,
    categoryBudgets: Map<String, Double> = emptyMap(),
    currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
    onCategoryClick: (DashboardContract.CategoryData) -> Unit = {}
) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.not_enough_category_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    
    val animProgress = remember { Animatable(0f) }
    val hasAnimated = rememberSaveable(categories) { mutableStateOf(false) }
    val locale = LocalLocale.current.platformLocale
    LaunchedEffect(categories) {
        if (!hasAnimated.value) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
            hasAnimated.value = true
        } else {
            animProgress.snapTo(1f)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            categories.forEach { category ->
                val categoryEnum = com.masum.cipher.core.domain.model.TransactionCategory.fromString(category.category)
                val safeWeight = (category.percentage * animProgress.value).coerceAtLeast(0.001f)
                Box(
                    modifier = Modifier
                        .weight(safeWeight)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(categoryEnum.color)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(4).forEach { category ->
                val categoryEnum = com.masum.cipher.core.domain.model.TransactionCategory.fromString(category.category)
                val budget = categoryBudgets[category.category] ?: categoryBudgets[categoryEnum.name] ?: 0.0
                val isOver = budget > 0 && category.amount > budget

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onCategoryClick(category) }
                        .padding(vertical = 4.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(categoryEnum.color.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = categoryEnum.icon,
                                contentDescription = null,
                                tint = categoryEnum.color,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = categoryEnum.displayName,
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(category.amount, currencySymbol = currencySymbol, locale = locale),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = if (isOver) RoseExpense else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoryEnum.color.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${(category.percentage * 100).toInt()}%",
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp
                                ),
                                color = categoryEnum.color
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildSmoothLinePath(pts: List<Offset>): Path {
    val path = Path()
    if (pts.size < 2) return path
    path.moveTo(pts[0].x, pts[0].y)
    if (pts.size == 2) {
        path.lineTo(pts[1].x, pts[1].y)
        return path
    }
    for (i in 0 until pts.size - 1) {
        val p0 = pts.getOrNull(i - 1) ?: pts[0]
        val p1 = pts[i]
        val p2 = pts[i + 1]
        val p3 = pts.getOrNull(i + 2) ?: pts.last()
        val cx1 = p1.x + (p2.x - p0.x) / 6f
        val cy1 = p1.y + (p2.y - p0.y) / 6f
        val cx2 = p2.x - (p3.x - p1.x) / 6f
        val cy2 = p2.y - (p3.y - p1.y) / 6f
        path.cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
    }
    return path
}

enum class FinancialFlowMode {
    EXPENSE,
    INCOME,
    NET_FLOW
}

@Composable
fun SpendingTrendChart(
    expensePoints: List<DashboardContract.Point>,
    incomePoints: List<DashboardContract.Point> = emptyList(),
    netFlowPoints: List<DashboardContract.Point> = emptyList(),
    currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol,
    isHapticsEnabled: Boolean = true
) {
    var selectedMode by rememberSaveable { mutableStateOf(FinancialFlowMode.EXPENSE) }
    val points = when (selectedMode) {
        FinancialFlowMode.EXPENSE -> expensePoints
        FinancialFlowMode.INCOME -> incomePoints.ifEmpty { expensePoints }
        FinancialFlowMode.NET_FLOW -> netFlowPoints.ifEmpty { expensePoints }
    }
    val view = androidx.compose.ui.platform.LocalView.current
    val locale = LocalLocale.current.platformLocale

    if (points.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.not_enough_data_trend),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val currentTotal = points.lastOrNull()?.y ?: 0f
    val dynamicThemeColor = when (selectedMode) {
        FinancialFlowMode.EXPENSE -> MaterialTheme.colorScheme.primary
        FinancialFlowMode.INCOME -> EmeraldIncome
        FinancialFlowMode.NET_FLOW -> if (currentTotal >= 0f) EmeraldIncome else RoseExpense
    }

    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val drawProgress = remember { Animatable(0f) }

    LaunchedEffect(selectedMode, points) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    var scrubX by remember { mutableStateOf<Float?>(null) }
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        val safeTotal = if (currentTotal.isInfinite() || currentTotal.isNaN()) 0.0 else currentTotal.toDouble()
                        val strOutflow = stringResource(R.string.total_outflow)
                        val strInflow = stringResource(R.string.total_inflow)
                        val strSurplus = stringResource(R.string.net_surplus)
                        val strDeficit = stringResource(R.string.net_deficit)
                        val (statTitle, statSubtitle) = when (selectedMode) {
                            FinancialFlowMode.EXPENSE -> Pair(com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(kotlin.math.abs(safeTotal), currencySymbol = currencySymbol), strOutflow)
                            FinancialFlowMode.INCOME -> Pair(com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(kotlin.math.abs(safeTotal), currencySymbol = currencySymbol), strInflow)
                            FinancialFlowMode.NET_FLOW -> {
                                val label = if (safeTotal >= 0.0) strSurplus else strDeficit
                                Pair(com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(safeTotal, currencySymbol = currencySymbol), label)
                            }
                        }

                        Text(
                            text = statTitle,
                            style = Typography.titleMedium.copy(fontFamily = Lato, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = statSubtitle,
                            style = Typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    if (incomePoints.isNotEmpty() || netFlowPoints.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val strExpense = stringResource(R.string.expense)
                            val strIncome = stringResource(R.string.income)
                            val strNet = stringResource(R.string.net)
                            FinancialFlowMode.entries.forEach { mode ->
                                val isSelected = selectedMode == mode
                                val bgAlpha by animateFloatAsState(if (isSelected) 1f else 0f, label = "flow_toggle_bg")
                                val label = when (mode) {
                                    FinancialFlowMode.EXPENSE -> strExpense
                                    FinancialFlowMode.INCOME -> strIncome
                                    FinancialFlowMode.NET_FLOW -> strNet
                                }
                                val modeActiveColor = when (mode) {
                                    FinancialFlowMode.EXPENSE -> MaterialTheme.colorScheme.primary
                                    FinancialFlowMode.INCOME -> EmeraldIncome
                                    FinancialFlowMode.NET_FLOW -> if (currentTotal >= 0f) EmeraldIncome else RoseExpense
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(modeActiveColor.copy(alpha = bgAlpha))
                                        .clickable {
                                            if (selectedMode != mode) {
                                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                                selectedMode = mode
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = Typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .pointerInput(points) {
                            detectDragGestures(
                                onDragStart = { offset -> scrubX = offset.x },
                                onDrag = { change, _ -> scrubX = change.position.x },
                                onDragEnd = { scrubX = null },
                                onDragCancel = { scrubX = null }
                            )
                        }
                        .pointerInput(points) {
                            detectTapGestures(
                                onPress = { offset ->
                                    scrubX = offset.x
                                    tryAwaitRelease()
                                    scrubX = null
                                }
                            )
                        }
                ) {
                    val chartPadLeft = with(density) { 52.dp.toPx() }
                    val chartPadRight = with(density) { 16.dp.toPx() }
                    val chartPadTop = with(density) { 24.dp.toPx() }
                    val chartPadBottom = with(density) { 28.dp.toPx() }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val plotW = w - chartPadLeft - chartPadRight
                        val plotH = h - chartPadTop - chartPadBottom

                        val yValues = points.map { it.y }
                        val rawMin = yValues.minOrNull() ?: 0f
                        val rawMax = yValues.maxOrNull() ?: 0f

                        val yMin = if (rawMin < 0f) rawMin * 1.15f else (rawMin * 0.85f).coerceAtLeast(0f)
                        val yMax = if (rawMax > 0f) rawMax * 1.20f else 10f
                        val yRange = (yMax - yMin).coerceAtLeast(1f)

                        fun xForIndex(i: Int): Float =
                            chartPadLeft + i.toFloat() / (points.size - 1).coerceAtLeast(1) * plotW

                        fun yForValue(v: Float): Float =
                            chartPadTop + plotH - ((v - yMin) / yRange * plotH)

                        val offsets = points.mapIndexed { i, pt -> Offset(xForIndex(i), yForValue(pt.y)) }

                        val gridCount = 4
                        repeat(gridCount + 1) { step ->
                            val frac = step.toFloat() / gridCount
                            val yVal = yMin + frac * yRange
                            val yPx = yForValue(yVal)

                            drawLine(
                                color = onSurfaceVariant.copy(alpha = 0.10f),
                                start = Offset(chartPadLeft, yPx),
                                end = Offset(w - chartPadRight, yPx),
                                strokeWidth = 1f
                            )

                            val labelText = com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(
                                yVal.toDouble(),
                                currencySymbol = currencySymbol,
                                locale = locale
                            )

                            val measured = textMeasurer.measure(
                                text = labelText,
                                style = TextStyle(
                                    color = onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(
                                    x = chartPadLeft - measured.size.width - with(density) { 6.dp.toPx() },
                                    y = yPx - measured.size.height / 2f
                                )
                            )
                        }

                        val clipRight = chartPadLeft + plotW * drawProgress.value

                        val areaPath = buildSmoothLinePath(offsets).apply {
                            lineTo(offsets.last().x, chartPadTop + plotH)
                            lineTo(offsets.first().x, chartPadTop + plotH)
                            close()
                        }
                        clipRect(left = 0f, top = 0f, right = clipRight, bottom = h) {
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        dynamicThemeColor.copy(alpha = 0.25f),
                                        dynamicThemeColor.copy(alpha = 0.0f)
                                    ),
                                    startY = chartPadTop,
                                    endY = chartPadTop + plotH
                                )
                            )

                            drawPath(
                                path = buildSmoothLinePath(offsets),
                                color = dynamicThemeColor,
                                style = Stroke(
                                    width = with(density) { 2.5.dp.toPx() },
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            drawPath(
                                path = buildSmoothLinePath(offsets),
                                color = dynamicThemeColor.copy(alpha = 0.18f),
                                style = Stroke(
                                    width = with(density) { 8.dp.toPx() },
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        val scrub = scrubX
                        if (scrub != null && drawProgress.value >= 1f) {
                            val clampedX = scrub.coerceIn(chartPadLeft, chartPadLeft + plotW)

                            val nearestIdx = offsets.indices.minByOrNull { i ->
                                kotlin.math.abs(offsets[i].x - clampedX)
                            } ?: 0
                            val nearestOffset = offsets[nearestIdx]
                            val nearestPoint = points[nearestIdx]

                            drawLine(
                                color = onSurface.copy(alpha = 0.25f),
                                start = Offset(nearestOffset.x, chartPadTop),
                                end = Offset(nearestOffset.x, chartPadTop + plotH),
                                strokeWidth = with(density) { 1.dp.toPx() },
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                            )

                            drawCircle(
                                color = dynamicThemeColor.copy(alpha = 0.2f),
                                radius = with(density) { 6.dp.toPx() } * pulseScale,
                                center = nearestOffset
                            )
                            drawCircle(
                                color = dynamicThemeColor,
                                radius = with(density) { 5.dp.toPx() },
                                center = nearestOffset
                            )
                            drawCircle(
                                color = surface,
                                radius = with(density) { 2.5.dp.toPx() },
                                center = nearestOffset
                            )

                            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", locale)
                            val formattedNearest = com.masum.cipher.core.util.AppFormatters.formatCurrency(nearestPoint.y.toDouble(), currencySymbol, locale)
                            val amountText = when (selectedMode) {
                                FinancialFlowMode.EXPENSE -> "$formattedNearest spent"
                                FinancialFlowMode.INCOME -> "$formattedNearest earned"
                                FinancialFlowMode.NET_FLOW -> {
                                    val label = if (nearestPoint.y >= 0f) "surplus" else "deficit"
                                    "$formattedNearest $label"
                                }
                            }
                            val dateText = sdf.format(java.util.Date(nearestPoint.timestamp))

                            val amountMeasured = textMeasurer.measure(
                                amountText,
                                TextStyle(color = onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            )
                            val dateMeasured = textMeasurer.measure(
                                dateText,
                                TextStyle(color = onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Normal)
                            )

                            val tooltipPadH = with(density) { 10.dp.toPx() }
                            val tooltipPadV = with(density) { 8.dp.toPx() }
                            val tooltipW = maxOf(amountMeasured.size.width, dateMeasured.size.width) + tooltipPadH * 2
                            val tooltipH = amountMeasured.size.height + dateMeasured.size.height + tooltipPadV * 2 + with(density) { 4.dp.toPx() }
                            val tooltipRadius = with(density) { 8.dp.toPx() }

                            var tooltipLeft = nearestOffset.x - tooltipW / 2
                            tooltipLeft = tooltipLeft.coerceIn(chartPadLeft, w - chartPadRight - tooltipW)
                            val tooltipTop = (nearestOffset.y - tooltipH - with(density) { 14.dp.toPx() }).coerceAtLeast(chartPadTop)

                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                    color = android.graphics.Color.TRANSPARENT
                                    setShadowLayer(with(density) { 8.dp.toPx() }, 0f, with(density) { 2.dp.toPx() }, android.graphics.Color.argb(60, 0, 0, 0))
                                }
                                canvas.nativeCanvas.drawRoundRect(
                                    tooltipLeft, tooltipTop,
                                    tooltipLeft + tooltipW, tooltipTop + tooltipH,
                                    tooltipRadius, tooltipRadius, paint
                                )
                            }

                            drawRoundRect(
                                color = surfaceVariant,
                                topLeft = Offset(tooltipLeft, tooltipTop),
                                size = Size(tooltipW, tooltipH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(tooltipRadius)
                            )
                            drawRoundRect(
                                color = dynamicThemeColor.copy(alpha = 0.6f),
                                topLeft = Offset(tooltipLeft, tooltipTop),
                                size = Size(tooltipW, tooltipH),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(tooltipRadius),
                                style = Stroke(width = with(density) { 1.dp.toPx() })
                            )

                            drawText(
                                textLayoutResult = amountMeasured,
                                topLeft = Offset(
                                    tooltipLeft + (tooltipW - amountMeasured.size.width) / 2f,
                                    tooltipTop + tooltipPadV
                                )
                            )
                            drawText(
                                textLayoutResult = dateMeasured,
                                topLeft = Offset(
                                    tooltipLeft + (tooltipW - dateMeasured.size.width) / 2f,
                                    tooltipTop + tooltipPadV + amountMeasured.size.height + with(density) { 4.dp.toPx() }
                                )
                            )
                        }

                        val sdfX = java.text.SimpleDateFormat("MMM d", locale)
                        val maxLabels = minOf(points.size, 5)
                        val step = (points.size - 1).toFloat() / (maxLabels - 1).coerceAtLeast(1)
                        for (labelIdx in 0 until maxLabels) {
                            val ptIdx = (labelIdx * step).toInt().coerceIn(0, points.lastIndex)
                            val xPx = xForIndex(ptIdx)
                            val label = sdfX.format(java.util.Date(points[ptIdx].timestamp))
                            val measured = textMeasurer.measure(
                                label,
                                TextStyle(
                                    color = onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(
                                    x = (xPx - measured.size.width / 2f).coerceIn(
                                        chartPadLeft,
                                        w - chartPadRight - measured.size.width
                                    ),
                                    y = chartPadTop + plotH + with(density) { 8.dp.toPx() }
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun PeakHoursChart(
    hours: List<InsightsContract.PeakHourData>,
    currencySymbol: String = com.masum.cipher.core.domain.model.AppCurrency.detectDefault().symbol
) {
    if (hours.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.not_enough_hourly_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val animProgress = remember { Animatable(0f) }
    val hasAnimated = rememberSaveable(hours) { mutableStateOf(false) }
    LaunchedEffect(hours) {
        if (!hasAnimated.value) {
            animProgress.snapTo(0f)
            animProgress.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
            hasAnimated.value = true
        } else {
            animProgress.snapTo(1f)
        }
    }

    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0] ?: java.util.Locale.getDefault()
    var tappedIdx by remember { mutableStateOf<Int?>(null) }
    val barCount = hours.size
    val maxAmount = hours.maxOfOrNull { it.amount } ?: 1.0
    val anchorIndices = if (barCount <= 5) hours.indices.toList()
        else listOf(0, barCount / 4, barCount / 2, 3 * barCount / 4, barCount - 1)

    val morningStr = stringResource(R.string.morning)
    val afternoonStr = stringResource(R.string.afternoon)
    val eveningStr = stringResource(R.string.evening)
    val nightStr = stringResource(R.string.night)
    fun localizeHourLabel(l: String): String = when (l.lowercase()) {
        "morning" -> morningStr
        "afternoon" -> afternoonStr
        "evening" -> eveningStr
        "night" -> nightStr
        else -> l
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp)
                .padding(top = 16.dp)
                .pointerInput(hours) {
                    detectTapGestures { offset ->
                        val padH = with(density) { 16.dp.toPx() }
                        val availW = size.width - padH * 2
                        val idx = ((offset.x - padH) / (availW / barCount))
                            .toInt().coerceIn(0, barCount - 1)
                        tappedIdx = if (tappedIdx == idx) null else idx
                    }
                }
        ) {
            val chartPadL = with(density) { 48.dp.toPx() }
            val chartPadR = with(density) { 16.dp.toPx() }
            val chartPadT = with(density) { 16.dp.toPx() }
            val chartPadB = with(density) { 26.dp.toPx() }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val plotW = w - chartPadL - chartPadR
                val plotH = h - chartPadT - chartPadB
                val slotW = plotW / barCount
                val barW = (slotW * 0.55f).coerceAtMost(with(density) { 16.dp.toPx() })
                val cornerR = barW / 2f

                val gridCount = 4
                for (step in 0..gridCount) {
                    val frac = step.toFloat() / gridCount
                    val yVal = frac * maxAmount
                    val yPx = chartPadT + plotH - (frac * plotH)

                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.15f),
                        start = Offset(chartPadL, yPx),
                        end = Offset(w - chartPadR, yPx),
                        strokeWidth = with(density) { 1.dp.toPx() },
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )

                    val labelText = com.masum.cipher.core.util.AppFormatters.formatCompactCurrency(yVal, currencySymbol = currencySymbol, locale = locale)
                    val measured = textMeasurer.measure(
                        text = labelText,
                        style = TextStyle(
                            color = onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            x = chartPadL - measured.size.width - with(density) { 6.dp.toPx() },
                            y = yPx - measured.size.height / 2f
                        )
                    )
                }

                hours.forEachIndexed { i, data ->
                    val intensity = (data.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                    val animH = plotH * intensity * animProgress.value
                    val barCx = chartPadL + i * slotW + slotW / 2f
                    val barLeft = barCx - barW / 2f
                    val barTop = chartPadT + plotH - animH
                    val barBottom = chartPadT + plotH
                    val isTapped = tappedIdx == i
                    val baseAlpha = if (intensity > 0.1f) 0.3f else 0.15f
                    val barAlpha = baseAlpha + intensity * 0.7f
                    val topColor = primaryColor.copy(alpha = if (isTapped) 1f else barAlpha)
                    val bottomColor = primaryColor.copy(alpha = if (isTapped) 0.6f else barAlpha * 0.3f)

                    val barBrush = Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor),
                        startY = barTop,
                        endY = barBottom
                    )

                    if (animH > cornerR * 2) {
                        val path = Path().apply {
                            moveTo(barLeft, barBottom)
                            lineTo(barLeft, barTop + cornerR)
                            quadraticTo(barLeft, barTop, barLeft + cornerR, barTop)
                            lineTo(barLeft + barW - cornerR, barTop)
                            quadraticTo(barLeft + barW, barTop, barLeft + barW, barTop + cornerR)
                            lineTo(barLeft + barW, barBottom)
                            close()
                        }
                        drawPath(path, brush = barBrush)
                    } else if (animH > 0f) {
                        drawCircle(topColor, radius = animH / 2f, center = Offset(barCx, barTop + animH / 2f))
                    }

                    if (isTapped) {
                        drawRect(
                            color = primaryColor.copy(alpha = 0.07f),
                            topLeft = Offset(barLeft - with(density) { 4.dp.toPx() }, chartPadT),
                            size = Size(barW + with(density) { 8.dp.toPx() }, plotH)
                        )
                    }
                }

                anchorIndices.forEach { idx ->
                    val data = hours.getOrNull(idx) ?: return@forEach
                    val xPx = chartPadL + idx * slotW + slotW / 2f
                    val measured = textMeasurer.measure(
                        localizeHourLabel(data.label),
                        TextStyle(color = onSurfaceVariant.copy(alpha = 0.65f), fontSize = 9.sp, fontWeight = FontWeight.Normal)
                    )
                    drawText(
                        measured,
                        topLeft = Offset(
                            (xPx - measured.size.width / 2f).coerceIn(chartPadL, w - chartPadR - measured.size.width),
                            chartPadT + plotH + with(density) { 7.dp.toPx() }
                        )
                    )
                }

                val tapped = tappedIdx
                if (tapped != null) {
                    val data = hours.getOrNull(tapped) ?: return@Canvas
                    val intensity = (data.amount / maxAmount).toFloat()
                    val barCx = chartPadL + tapped * slotW + slotW / 2f
                    val barTopY = chartPadT + plotH - plotH * intensity * animProgress.value

                    val amtMeasured = textMeasurer.measure(
                        com.masum.cipher.core.util.AppFormatters.formatCurrency(data.amount, currencySymbol, locale),
                        TextStyle(color = onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )
                    val lblMeasured = textMeasurer.measure(
                        localizeHourLabel(data.label),
                        TextStyle(color = onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Normal)
                    )
                    val pH = with(density) { 10.dp.toPx() }
                    val pV = with(density) { 7.dp.toPx() }
                    val tipW = maxOf(amtMeasured.size.width, lblMeasured.size.width) + pH * 2
                    val tipH = amtMeasured.size.height + lblMeasured.size.height + pV * 2 + with(density) { 3.dp.toPx() }
                    val tipR = with(density) { 8.dp.toPx() }
                    var tipL = barCx - tipW / 2f
                    tipL = tipL.coerceIn(chartPadL, w - chartPadR - tipW)
                    val tipT = (barTopY - tipH - with(density) { 10.dp.toPx() }).coerceAtLeast(chartPadT)

                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            isAntiAlias = true; color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(with(density) { 8.dp.toPx() }, 0f, with(density) { 2.dp.toPx() }, android.graphics.Color.argb(50, 0, 0, 0))
                        }
                        canvas.nativeCanvas.drawRoundRect(tipL, tipT, tipL + tipW, tipT + tipH, tipR, tipR, paint)
                    }
                    drawRoundRect(color = surface, topLeft = Offset(tipL, tipT), size = Size(tipW, tipH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(tipR))
                    drawRoundRect(color = primaryColor.copy(alpha = 0.7f), topLeft = Offset(tipL, tipT), size = Size(tipW, tipH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(tipR), style = Stroke(with(density) { 1.dp.toPx() }))
                    drawText(amtMeasured, topLeft = Offset(tipL + (tipW - amtMeasured.size.width) / 2f, tipT + pV))
                    drawText(lblMeasured, topLeft = Offset(tipL + (tipW - lblMeasured.size.width) / 2f, tipT + pV + amtMeasured.size.height + with(density) { 3.dp.toPx() }))
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
    val locale = LocalLocale.current.platformLocale

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
                text = com.masum.cipher.core.util.AppFormatters.getMonthYearFormat(locale).format(visibleMonthCal.time),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val view = androidx.compose.ui.platform.LocalView.current
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
                        view.performVibrate(true)
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
                        view.performVibrate(true)
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                )
            }
        }

        val weekdays = remember(locale) {
            val symbols = java.text.DateFormatSymbols(locale).shortWeekdays
            (Calendar.SUNDAY..Calendar.SATURDAY).map { dayIndex ->
                val name = symbols[dayIndex]
                if (name.length > 2) name.substring(0, 2) else name
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekdays.forEach { day ->
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
            val rows = 6
            
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
                            val isBrightCell = isCurrentMonth && spend > 0 && (0.35f + intensity * 0.65f) >= 0.55f

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .background(
                                        color = if (!isCurrentMonth) Color.Transparent 
                                                else if (spend > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f + intensity * 0.65f) 
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        width = if (isSelected || isToday) 2.dp else if (isCurrentMonth && spend == 0.0) 1.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary 
                                                else if (isToday) MaterialTheme.colorScheme.outline 
                                                else if (isCurrentMonth && spend == 0.0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f) 
                                                else Color.Transparent,
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
                                            else if (isBrightCell) Color(0xFF0D0D1A) 
                                            else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected || isToday || spend > 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

