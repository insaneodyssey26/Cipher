package com.masum.cipher.ui.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.data.local.entity.GoalEntity
import com.masum.cipher.core.domain.model.CategoryIconRegistry
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons

@Composable
fun GoalCard(
    goal: GoalEntity,
    currencySymbol: String,
    isHapticsEnabled: Boolean,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val goalColor = Color(goal.colorHex.toInt())
    val icon = CategoryIconRegistry.getIcon(goal.iconName)

    val progressFraction = if (goal.targetAmount > 0) {
        (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val progressPercentage = if (goal.targetAmount > 0) {
        ((goal.savedAmount / goal.targetAmount) * 100).toInt()
    } else 0

    val animatedSweep by animateFloatAsState(
        targetValue = progressFraction * 260f,
        animationSpec = tween(durationMillis = 900),
        label = "goal_sweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, White10, RoundedCornerShape(20.dp))
            .clickable {
                view.performVibrate(isHapticsEnabled, isLongPress = false)
                onCardClick()
            }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(116.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(112.dp)) {
                    val strokeWidth = 7.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    drawArc(
                        color = goalColor.copy(alpha = 0.15f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (animatedSweep > 0f) {
                        drawArc(
                            color = goalColor,
                            startAngle = 140f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$progressPercentage%",
                        style = Typography.labelMedium.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(goalColor.copy(alpha = 0.18f))
                            .border(1.dp, goalColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = goalColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = goal.name,
                style = Typography.titleMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = AppFormatters.formatCurrency(goal.savedAmount, currencySymbol, decimals = 0),
                style = Typography.titleMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                color = goalColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.goals_target_label, AppFormatters.formatCurrency(goal.targetAmount, currencySymbol, decimals = 0)),
                style = Typography.bodySmall.copy(
                    fontFamily = Lato,
                    fontSize = 11.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
