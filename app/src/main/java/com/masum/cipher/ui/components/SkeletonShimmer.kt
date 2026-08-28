package com.masum.cipher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun rememberAccentedShimmerBrush(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    val highlightColor = primary.copy(alpha = 0.18f)

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            baseColor,
            highlightColor,
            baseColor
        ),
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    brush: Brush = rememberAccentedShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun TransactionSkeletonItem(
    modifier: Modifier = Modifier,
    brush: Brush = rememberAccentedShimmerBrush()
) {
    VaultCard(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 6.dp),
        onClick = {},
        contentPadding = 12.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ShimmerBox(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                brush = brush
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(16.dp),
                    shape = RoundedCornerShape(6.dp),
                    brush = brush
                )
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp),
                    brush = brush
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .width(68.dp)
                        .height(16.dp),
                    shape = RoundedCornerShape(6.dp),
                    brush = brush
                )
                ShimmerBox(
                    modifier = Modifier
                        .width(44.dp)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp),
                    brush = brush
                )
            }
        }
    }
}

@Composable
fun TransactionListSkeleton(
    count: Int = 6,
    modifier: Modifier = Modifier
) {
    val brush = rememberAccentedShimmerBrush()
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(count) {
            TransactionSkeletonItem(brush = brush)
        }
    }
}
