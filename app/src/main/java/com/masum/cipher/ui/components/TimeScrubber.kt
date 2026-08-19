package com.masum.cipher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.masum.cipher.core.domain.model.TimePeriod
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Calendar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimeSelectorDropdown(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier,
    isHapticsEnabled: Boolean = true,
    iconOnly: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)
    val view = androidx.compose.ui.platform.LocalView.current
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier) {
        // The Dropdown Trigger (Chip)
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable {
                    view.performVibrate(isHapticsEnabled)
                    expanded = true
                }
                .padding(horizontal = if (iconOnly) 12.dp else 16.dp, vertical = if (iconOnly) 12.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!iconOnly) {
                Text(
                    text = selectedPeriod.label.uppercase(),
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = if (iconOnly) LucideIcons.Calendar else LucideIcons.ChevronDown,
                contentDescription = "Select Time Range",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(if (iconOnly) 18.dp else 16.dp)
                    .rotate(if (!iconOnly) rotation else 0f)
            )
        }

        // The Dropdown Menu
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            TimePeriod.values().forEach { period ->
                val isSelected = selectedPeriod == period
                DropdownMenuItem(
                    text = {
                        Text(
                            text = period.label,
                            style = Typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(LucideIcons.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
                    } else null,
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        expanded = false
                        if (!isSelected) {
                            coroutineScope.launch {
                                delay(150)
                                onPeriodSelected(period)
                            }
                        }
                    }
                )
            }
        }
    }
}
