package com.masum.cipher.ui.accounts

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.domain.model.AccountType
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.Lock
import compose.icons.lucideicons.Pencil
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Trash2
import java.util.Locale

@Composable
fun LuxuryAccountCard(
    name: String,
    type: AccountType,
    balance: Double,
    colorHex: Long,
    iconName: String,
    isDefault: Boolean,
    last4: String?,
    currencySymbol: String,
    locale: Locale,
    modifier: Modifier = Modifier,
    transactionCount: Int? = null,
    isFrozen: Boolean = false,
    isHapticsEnabled: Boolean = true,
    onCardClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onSetDefaultClick: (() -> Unit)? = null,
    onUpgradeProClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val baseColor = Color(colorHex)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val formattedLast4 = if (!last4.isNullOrBlank()) last4.takeLast(4) else "••••"

    val isDebtAccount = type == AccountType.CREDIT_CARD
    val formattedBalance = AppFormatters.formatCurrency(balance, currencySymbol, locale, decimals = 2)

    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 10.dp else 4.dp,
                shape = cardShape,
                spotColor = if (isDark) baseColor.copy(alpha = 0.35f) else baseColor.copy(alpha = 0.18f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.06f)
            )
            .clip(cardShape)
            .background(
                brush = if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E212A),
                            Color(0xFF14161C),
                            Color(0xFF0F1116)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                            baseColor.copy(alpha = 0.06f)
                        )
                    )
                },
                shape = cardShape
            )
            .border(
                width = 1.dp,
                brush = if (isDark) {
                    Brush.linearGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.65f),
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.40f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.06f)
                        )
                    )
                },
                shape = cardShape
            )
            .then(
                if (onCardClick != null) {
                    Modifier.clickable {
                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                        onCardClick()
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(baseColor.copy(alpha = 0.20f), RoundedCornerShape(12.dp))
                            .border(1.dp, baseColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getAccountIconVector(iconName),
                            contentDescription = null,
                            tint = baseColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = name.ifBlank { "Account" },
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isFrozen) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF64748B).copy(alpha = 0.22f), RoundedCornerShape(8.dp))
                                .border(0.8.dp, Color(0xFF94A3B8).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_frozen_badge),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                            )
                        }
                    } else if (isDefault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) baseColor.copy(alpha = 0.25f) else baseColor.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                                .border(0.8.dp, if (isDark) baseColor.copy(alpha = 0.6f) else baseColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.account_primary_toggle_label),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (isDark) Color.White else baseColor
                            )
                        }
                    } else if (onSetDefaultClick != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .border(0.6.dp, if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onSetDefaultClick()
                                }
                                .padding(horizontal = 8.dp, vertical = 3.5.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.accounts_set_primary),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp
                                ),
                                color = if (isDark) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onEditClick != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onEditClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Pencil,
                                contentDescription = "Edit Card",
                                tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    if (onDeleteClick != null && !isDefault) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .clickable {
                                    view.performVibrate(isHapticsEnabled, isLongPress = false)
                                    onDeleteClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Trash2,
                                contentDescription = "Delete Card",
                                tint = RoseExpense.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (isDebtAccount) stringResource(R.string.account_outstanding_balance_label) else stringResource(R.string.balance_label).uppercase(),
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formattedBalance,
                    style = Typography.headlineMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = if (balance < 0) RoseExpense else if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .border(0.6.dp, if (isDark) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.CreditCard,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "•••• $formattedLast4",
                        style = Typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.5.sp,
                            letterSpacing = 1.sp
                        ),
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(type.labelRes).uppercase(),
                    style = Typography.labelSmall.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = if (isDark) Color.White.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (isFrozen) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(
                        if (isDark) {
                            Color(0xF0080B10)
                        } else {
                            Color(0xF20F172A)
                        }
                    )
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), cardShape)
                    .clickable(enabled = false) {}
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = stringResource(R.string.account_locked_title),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.account_locked_desc),
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        ),
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (onSetDefaultClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.14f))
                                    .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        onSetDefaultClick()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.account_make_primary_btn),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        if (onUpgradeProClick != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFF59E0B),
                                                Color(0xFFD97706)
                                            )
                                        )
                                    )
                                    .clickable {
                                        view.performVibrate(isHapticsEnabled, isLongPress = false)
                                        onUpgradeProClick()
                                    }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Sparkles,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.pro_btn_upgrade),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp
                                        ),
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
