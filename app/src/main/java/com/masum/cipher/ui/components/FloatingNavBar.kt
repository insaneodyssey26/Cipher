package com.masum.cipher.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.annotation.StringRes
import com.masum.cipher.R
import com.masum.cipher.ui.theme.White10
import compose.icons.LucideIcons
import compose.icons.lucideicons.ChartBar
import compose.icons.lucideicons.ChevronLeft
import compose.icons.lucideicons.ChevronRight
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.Settings
import compose.icons.lucideicons.Users
import compose.icons.lucideicons.Wallet

enum class BottomNavItem(val route: String, val icon: ImageVector, @StringRes val labelRes: Int) {
    Dashboard("dashboard", LucideIcons.Wallet, R.string.nav_spend),
    Insights("insights", LucideIcons.ChartBar, R.string.nav_insights),
    Splits("split_expenses", LucideIcons.Users, R.string.nav_splits),
    Settings("settings", LucideIcons.Settings, R.string.nav_settings)
}

@Composable
fun FloatingNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHapticsEnabled: Boolean = true,
    isCompressed: Boolean = false,
    onToggleCompress: (Boolean) -> Unit = {}
) {
    val view = androidx.compose.ui.platform.LocalView.current
    val backgroundColor = MaterialTheme.colorScheme.background

    val activeItem = remember(currentRoute) {
        BottomNavItem.values().find { it.route == currentRoute } ?: BottomNavItem.Dashboard
    }

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
            .padding(top = 48.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = isCompressed,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 60))) togetherWith
                (fadeOut(animationSpec = tween(180)))
            },
            label = "navbar_compress_transition"
        ) { compressed ->
            if (compressed) {
                Row(
                    modifier = Modifier
                        .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, White10, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(elevation = 8.dp, shape = CircleShape, spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
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
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onToggleCompress(false)
                            }
                            .padding(horizontal = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = activeItem.icon,
                                contentDescription = androidx.compose.ui.res.stringResource(activeItem.labelRes),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Icon(
                                imageVector = LucideIcons.ChevronRight,
                                contentDescription = "Expand bar",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .border(1.dp, White10, CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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

                    Box(
                        modifier = Modifier
                            .size(46.dp)
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
                        item = BottomNavItem.Splits,
                        isSelected = currentRoute == BottomNavItem.Splits.route,
                        onClick = { 
                            view.performVibrate(isHapticsEnabled)
                            onNavigate(BottomNavItem.Splits.route) 
                        }
                    )

                    NavItem(
                        item = BottomNavItem.Settings,
                        isSelected = currentRoute == BottomNavItem.Settings.route,
                        onClick = { 
                            view.performVibrate(isHapticsEnabled)
                            onNavigate(BottomNavItem.Settings.route) 
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onToggleCompress(true)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.ChevronLeft,
                            contentDescription = "Collapse bar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
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
        targetValue = if (isSelected) 56.dp else 38.dp,
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
                .size(20.dp)
                .scale(scale)
        )
    }
}

