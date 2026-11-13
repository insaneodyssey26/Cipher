package com.masum.cipher.ui.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ExternalLink
import compose.icons.lucideicons.Gift
import compose.icons.lucideicons.Mail
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.X

private const val DEVELOPER_SUPPORT_EMAIL = "skmasumali.dev@gmail.com"

fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
fun LegacyMigrationBanner(
    isLifetime: Boolean,
    licenseToken: String?,
    isDark: Boolean,
    isHapticsEnabled: Boolean,
    modifier: Modifier = Modifier,
    onRedeemClick: (() -> Unit)? = null,
    onDismissForever: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isDismissed by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(
        visible = !isDismissed,
        enter = fadeIn(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val accentColor = Color(0xFFF59E0B)
        val cardBg = if (isDark) Color(0xFF1E1A11) else Color(0xFFFFFBEB)
        val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
        val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Gift,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.22f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.pro_migration_badge).uppercase(),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.6.sp
                                    ),
                                    color = accentColor
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = stringResource(R.string.pro_migration_title),
                                style = Typography.titleMedium.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = textPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            isDismissed = true
                            onDismissForever?.invoke()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = "Dismiss",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = if (isLifetime) {
                        stringResource(R.string.pro_migration_desc_lifetime)
                    } else {
                        stringResource(R.string.pro_migration_desc_subscription)
                    },
                    style = Typography.bodySmall.copy(
                        fontFamily = Lato,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    ),
                    color = textSecondary
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onRedeemClick?.invoke()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.ExternalLink,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.pro_migration_action_redeem),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                ),
                                maxLines = 1
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            openDeveloperEmail(context, licenseToken)
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Mail,
                                contentDescription = null,
                                tint = textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.pro_migration_action_contact),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                color = textPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    if (onDismissForever != null) {
                        TextButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                isDismissed = true
                                onDismissForever.invoke()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_migration_action_dismiss_forever),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                ),
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegacyMigrationDialog(
    isLifetime: Boolean,
    licenseToken: String?,
    isDark: Boolean,
    isHapticsEnabled: Boolean,
    onRedeemClick: ((String) -> Unit)? = null,
    onExplorePro: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onDismissForever: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val accentColor = Color(0xFFF59E0B)
    val dialogBg = if (isDark) Color(0xFF1E1A11) else Color(0xFFFFFBEB)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    var currentStep by rememberSaveable { mutableStateOf(0) }
    var selectedPlan by rememberSaveable { mutableStateOf(if (isLifetime) "lifetime" else "annual") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == 2) LucideIcons.Sparkles else LucideIcons.Gift,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = when (currentStep) {
                        1 -> stringResource(R.string.pro_migration_title)
                        2 -> stringResource(R.string.pro_migration_humor_title)
                        else -> stringResource(R.string.pro_migration_question_title)
                    },
                    style = Typography.titleMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = textPrimary
                )
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (currentStep) {
                    0 -> {
                        Text(
                            text = stringResource(R.string.pro_migration_question_desc),
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                currentStep = 1
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_migration_choice_yes),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                currentStep = 2
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_migration_choice_no),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp
                                ),
                                color = textPrimary
                            )
                        }
                    }
                    1 -> {
                        Text(
                            text = stringResource(R.string.pro_migration_plan_prompt),
                            style = Typography.labelMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            color = textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val plans = listOf(
                                "lifetime" to stringResource(R.string.pro_migration_plan_lifetime),
                                "annual" to stringResource(R.string.pro_migration_plan_annual),
                                "monthly" to stringResource(R.string.pro_migration_plan_monthly)
                            )
                            plans.forEach { (planKey, planLabel) ->
                                val isSelected = selectedPlan == planKey
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) accentColor else accentColor.copy(alpha = 0.12f))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) accentColor else accentColor.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                                            selectedPlan = planKey
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = planLabel,
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        ),
                                        color = if (isSelected) Color.Black else textPrimary
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (selectedPlan == "lifetime") {
                                stringResource(R.string.pro_migration_desc_lifetime)
                            } else {
                                stringResource(R.string.pro_migration_desc_subscription)
                            },
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = textSecondary
                        )

                        Button(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onRedeemClick?.invoke(selectedPlan)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ExternalLink,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.pro_migration_action_redeem),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                openDeveloperEmail(context, licenseToken)
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = LucideIcons.Mail,
                                    contentDescription = null,
                                    tint = textPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.pro_migration_action_contact),
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    color = textPrimary
                                )
                            }
                        }
                    }
                    2 -> {
                        Text(
                            text = stringResource(R.string.pro_migration_humor_desc),
                            style = Typography.bodyMedium.copy(
                                fontFamily = Lato,
                                fontSize = 13.5.sp,
                                lineHeight = 19.sp
                            ),
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = false)
                                onDismissForever?.invoke()
                                onDismiss()
                                onExplorePro?.invoke()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_migration_humor_cta),
                                style = Typography.labelMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep == 1 && onDismissForever != null) {
                    TextButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onDismissForever()
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.pro_migration_action_dismiss_forever),
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Normal
                            ),
                            color = textSecondary
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                TextButton(
                    onClick = {
                        if (currentStep == 2) {
                            onDismissForever?.invoke()
                        }
                        onDismiss()
                    }
                ) {
                    Text(
                        text = if (currentStep == 2) stringResource(R.string.pro_migration_humor_dismiss) else "Got it",
                        style = Typography.labelLarge.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.Bold
                        ),
                        color = accentColor
                    )
                }
            }
        }
    )
}

private fun openDeveloperEmail(context: Context, licenseToken: String?) {
    try {
        val subject = "Cipher Pro Google Play Migration"
        val body = buildString {
            append("Hi Masum,\n\n")
            append("I purchased Cipher Pro earlier and would like to claim my Google Play promo code.\n\n")
            if (!licenseToken.isNullOrBlank()) {
                append("Previous License: ").append(licenseToken).append("\n")
            }
            append("Purchase Email: (Sent from my purchase email)\n")
        }
        val uri = "mailto:$DEVELOPER_SUPPORT_EMAIL?subject=${android.net.Uri.encode(subject)}&body=${android.net.Uri.encode(body)}".toUri()
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        context.startActivity(intent)
    } catch (_: Exception) {}
}
