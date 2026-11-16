package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetGesturesEnabled: Boolean = true,
    containerColor: Color = Color.Unspecified,
    tint: Color = Color.Unspecified,
    tintAlpha: Float = 0.22f,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    content: @Composable ColumnScope.() -> Unit
) {
    val base = if (containerColor == Color.Unspecified) MaterialTheme.colorScheme.surface else containerColor
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetGesturesEnabled = sheetGesturesEnabled,
        shape = SheetShape,
        containerColor = Color.Transparent,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.55f),
        contentWindowInsets = contentWindowInsets,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SheetShape)
                .background(base)
                .then(if (tint != Color.Unspecified) Modifier.tintWash(tint, tintAlpha) else Modifier)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            )
            content()
        }
    }
}

@Composable
private fun Modifier.tintWash(tint: Color, alpha: Float): Modifier {
    val animated by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(durationMillis = 700, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)),
        label = "sheet_tint_wash"
    )
    return this.drawBehind {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(animated.copy(alpha = alpha), animated.copy(alpha = alpha * 0.3f), Color.Transparent),
                endY = size.height * 0.85f
            )
        )
    }
}
