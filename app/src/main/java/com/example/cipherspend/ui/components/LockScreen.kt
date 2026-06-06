package com.masum.cipher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.ui.theme.*

@Composable
fun LockScreen(onUnlockClick: () -> Unit) {
    val breathe by rememberInfiniteTransition(label = "breathe").animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "btn"
    )

    val bg = MaterialTheme.colorScheme.background
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(breathe)
                        .background(CipherBlueDim.copy(alpha = 0.08f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, CipherBlue.copy(alpha = 0.25f), CircleShape)
                        .background(CipherBlueDim.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            fontSize = 32.sp
                        ),
                        color = CipherBlue
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Text(
                text = "CIPHER",
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = onSurfaceVar
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your financial vault",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .scale(btnScale)
                    .background(CipherBlueDim, RoundedCornerShape(14.dp))
                    .border(1.dp, CipherBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onUnlockClick
                    )
                    .padding(horizontal = 40.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Unlock",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = CipherBlue
                )
            }
        }
    }
}
