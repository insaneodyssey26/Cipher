package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.ambientBackdrop(tint: Color = MaterialTheme.colorScheme.primary): Modifier =
    this
        .background(MaterialTheme.colorScheme.background)
        .background(Brush.verticalGradient(listOf(tint.copy(alpha = 0.12f), Color.Transparent), endY = 1100f))

