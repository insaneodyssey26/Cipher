package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.core.util.performVibrate
import compose.icons.LucideIcons
import compose.icons.lucideicons.Delete

@Composable
fun CalculatorNumpad(
    input: String,
    cursorPosition: Int = input.length,
    onInputChange: (newInput: String, newCursorPosition: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val safeCursor = cursorPosition.coerceIn(0, input.length)

    val onKeyClick: (String) -> Unit = { key ->
        view.performVibrate(true)
        val builder = StringBuilder(input)
        if (input == "0" && key != "." && key !in "+-*/") {
            onInputChange(key, 1)
        } else {
            builder.insert(safeCursor, key)
            val nextCursor = safeCursor + key.length
            onInputChange(builder.toString(), nextCursor)
        }
    }

    val onBackspace: () -> Unit = {
        view.performVibrate(true)
        if (input.isNotEmpty() && safeCursor > 0) {
            val builder = StringBuilder(input)
            builder.deleteCharAt(safeCursor - 1)
            onInputChange(builder.toString(), safeCursor - 1)
        }
    }

    val onClear: () -> Unit = {
        view.performVibrate(true, isLongPress = true)
        onInputChange("", 0)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)), // Ultra-faint grid lines
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        val rowModifier = Modifier.fillMaxWidth().height(60.dp) // Slightly taller for better touch area
        
        // Row 1
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            NumpadButton("1", modifier = Modifier.weight(1f), onClick = { onKeyClick("1") })
            NumpadButton("2", modifier = Modifier.weight(1f), onClick = { onKeyClick("2") })
            NumpadButton("3", modifier = Modifier.weight(1f), onClick = { onKeyClick("3") })
            NumpadButton("÷", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("/") })
        }
        // Row 2
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            NumpadButton("4", modifier = Modifier.weight(1f), onClick = { onKeyClick("4") })
            NumpadButton("5", modifier = Modifier.weight(1f), onClick = { onKeyClick("5") })
            NumpadButton("6", modifier = Modifier.weight(1f), onClick = { onKeyClick("6") })
            NumpadButton("×", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("*") })
        }
        // Row 3
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            NumpadButton("7", modifier = Modifier.weight(1f), onClick = { onKeyClick("7") })
            NumpadButton("8", modifier = Modifier.weight(1f), onClick = { onKeyClick("8") })
            NumpadButton("9", modifier = Modifier.weight(1f), onClick = { onKeyClick("9") })
            NumpadButton("-", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("-") })
        }
        // Row 4
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            NumpadButton(".", modifier = Modifier.weight(1f), onClick = { onKeyClick(".") })
            NumpadButton("0", modifier = Modifier.weight(1f), onClick = { onKeyClick("0") })
            
            // Backspace Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface) // Same as other buttons
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(color = MaterialTheme.colorScheme.primary), // Operator colored ripple
                        onClick = onBackspace
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Delete,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.primary, // Primary color for operator action
                    modifier = Modifier.size(24.dp)
                )
            }
            
            NumpadButton("+", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("+") })
        }
    }
}

@Composable
private fun NumpadButton(
    text: String,
    isOperator: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.surface // Solid, uniform background for all buttons
    
    val textColor = if (isOperator) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = textColor),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (isOperator) 28.sp else 24.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}
