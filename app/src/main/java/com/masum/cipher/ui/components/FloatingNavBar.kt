package com.masum.cipher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.masum.cipher.core.util.performVibrate
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChartBar
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Wallet

import androidx.annotation.StringRes
import com.masum.cipher.R

enum class BottomNavItem(val route: String, val icon: ImageVector, @StringRes val labelRes: Int) {
    Dashboard("dashboard", LucideIcons.Wallet, R.string.nav_spend),
    Insights("insights", LucideIcons.ChartBar, R.string.nav_insights),
    Settings("settings", LucideIcons.Settings, R.string.nav_settings)
}

@Composable
fun FloatingNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHapticsEnabled: Boolean = true
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        backgroundColor.copy(alpha = 0.5f),
                        backgroundColor.copy(alpha = 0.85f),
                        backgroundColor,
                        backgroundColor
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            )
            .navigationBarsPadding()
            .padding(top = 48.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.15f))
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                item = BottomNavItem.Dashboard,
                isSelected = currentRoute == BottomNavItem.Dashboard.route,
                onClick = { 
                    view.performVibrate(isHapticsEnabled)
                    onNavigate(BottomNavItem.Dashboard.route) 
                }
            )

            NavItem(
                item = BottomNavItem.Insights,
                isSelected = currentRoute == BottomNavItem.Insights.route,
                onClick = { 
                    view.performVibrate(isHapticsEnabled)
                    onNavigate(BottomNavItem.Insights.route) 
                }
            )

            // The FAB inside the NavBar
            Box(
                modifier = Modifier
                    .size(48.dp) // Make FAB slightly larger than tabs
                    .shadow(elevation = 12.dp, shape = CircleShape, spotColor = MaterialTheme.colorScheme.primary)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        onAddClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Plus,
                    contentDescription = "Add Transaction",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            NavItem(
                item = BottomNavItem.Settings,
                isSelected = currentRoute == BottomNavItem.Settings.route,
                onClick = { 
                    view.performVibrate(isHapticsEnabled)
                    onNavigate(BottomNavItem.Settings.route) 
                }
            )
        }
    }
}

@Composable
private fun NavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = tween(300),
        label = "scale"
    )
    val color by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(300),
        label = "color"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(300),
        label = "bgColor"
    )
    val width by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSelected) 64.dp else 40.dp,
        animationSpec = tween(300),
        label = "width"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .height(40.dp)
            .width(width)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = androidx.compose.ui.res.stringResource(item.labelRes),
            tint = color,
            modifier = Modifier
                .size(22.dp)
                .scale(scale)
        )
    }
}
