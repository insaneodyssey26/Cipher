package com.masum.cipher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val onKeyClick: (String) -> Unit = { key ->
        view.performVibrate(true)
        if (input == "0" && key != "." && key !in "+-*/") {
            onInputChange(key)
        } else {
            onInputChange(input + key)
        }
    }

    val onBackspace: () -> Unit = {
        view.performVibrate(true)
        if (input.isNotEmpty()) {
            onInputChange(input.dropLast(1))
        }
    }

    val onClear: () -> Unit = {
        view.performVibrate(true, isLongPress = true)
        onInputChange("")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val rowModifier = Modifier.fillMaxWidth().height(48.dp)
        
        // Row 1
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumpadButton("1", modifier = Modifier.weight(1f), onClick = { onKeyClick("1") })
            NumpadButton("2", modifier = Modifier.weight(1f), onClick = { onKeyClick("2") })
            NumpadButton("3", modifier = Modifier.weight(1f), onClick = { onKeyClick("3") })
            NumpadButton("/", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("/") })
        }
        // Row 2
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumpadButton("4", modifier = Modifier.weight(1f), onClick = { onKeyClick("4") })
            NumpadButton("5", modifier = Modifier.weight(1f), onClick = { onKeyClick("5") })
            NumpadButton("6", modifier = Modifier.weight(1f), onClick = { onKeyClick("6") })
            NumpadButton("*", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("*") })
        }
        // Row 3
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumpadButton("7", modifier = Modifier.weight(1f), onClick = { onKeyClick("7") })
            NumpadButton("8", modifier = Modifier.weight(1f), onClick = { onKeyClick("8") })
            NumpadButton("9", modifier = Modifier.weight(1f), onClick = { onKeyClick("9") })
            NumpadButton("-", isOperator = true, modifier = Modifier.weight(1f), onClick = { onKeyClick("-") })
        }
        // Row 4
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NumpadButton(".", modifier = Modifier.weight(1f), onClick = { onKeyClick(".") })
            NumpadButton("0", modifier = Modifier.weight(1f), onClick = { onKeyClick("0") })
            
            // Backspace Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(color = MaterialTheme.colorScheme.onSurface),
                        onClick = onBackspace
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Delete,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface,
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
    val bgColor = if (isOperator) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val textColor = if (isOperator) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(color = textColor),
                onClick = onClick
            )
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = if (isOperator) 26.sp else 20.sp,
                fontWeight = if (isOperator) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
