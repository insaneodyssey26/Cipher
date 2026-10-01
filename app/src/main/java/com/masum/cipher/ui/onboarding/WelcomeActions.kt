package com.masum.cipher.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.CloudDownload
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.Key

internal const val WELCOME_RESTORE_BACKUP_TAG = "welcome_restore_backup"
internal const val WELCOME_LICENSE_KEY_TAG = "welcome_license_key"

private const val COMPACT_WIDTH_DP = 360f
private const val LARGE_FONT_SCALE = 1.3f

internal fun shouldStackWelcomeActions(availableWidthDp: Float, fontScale: Float): Boolean =
    availableWidthDp < COMPACT_WIDTH_DP || fontScale >= LARGE_FONT_SCALE

private val SecondaryButtonShape = RoundedCornerShape(16.dp)
private val ProAccent = Color(0xFFE2FF38)
private val ProAccentSecondary = Color(0xFF38BDF8)

@Composable
internal fun WelcomeReturningUserSection(
    isPro: Boolean,
    proTier: String,
    onRestoreBackup: () -> Unit,
    onLicenseKey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val fontScale = LocalDensity.current.fontScale

    val restoreButton: @Composable (Modifier) -> Unit = { buttonModifier ->
        WelcomeSecondaryButton(
            icon = LucideIcons.CloudDownload,
            label = stringResource(R.string.onboarding_restore_backup_short),
            onClick = {
                view.performVibrate(true, isLongPress = false)
                onRestoreBackup()
            },
            modifier = buttonModifier.testTag(WELCOME_RESTORE_BACKUP_TAG)
        )
    }

    val licenseButton: @Composable (Modifier) -> Unit = { buttonModifier ->
        val openLicense = {
            view.performVibrate(true, isLongPress = false)
            onLicenseKey()
        }
        if (isPro) {
            WelcomeProActiveButton(
                tier = proTier,
                onClick = openLicense,
                modifier = buttonModifier.testTag(WELCOME_LICENSE_KEY_TAG)
            )
        } else {
            WelcomeSecondaryButton(
                icon = LucideIcons.Key,
                label = stringResource(R.string.onboarding_license_key_short),
                onClick = openLicense,
                modifier = buttonModifier.testTag(WELCOME_LICENSE_KEY_TAG)
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stacked = shouldStackWelcomeActions(maxWidth.value, fontScale)

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LabelledDivider(text = stringResource(R.string.onboarding_returning_title))

            if (stacked) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    restoreButton(Modifier.fillMaxWidth())
                    licenseButton(Modifier.fillMaxWidth())
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    restoreButton(Modifier.weight(1f).fillMaxHeight())
                    licenseButton(Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
    }
}

@Composable
internal fun WelcomeSecondaryButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(SecondaryButtonShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, SecondaryButtonShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = Typography.titleSmall.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            autoSize = TextAutoSize.StepBased(minFontSize = 11.sp, maxFontSize = 14.5.sp, stepSize = 0.5.sp)
        )
    }
}

@Composable
private fun WelcomeProActiveButton(
    tier: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(SecondaryButtonShape)
            .background(
                Brush.horizontalGradient(
                    listOf(ProAccent.copy(alpha = 0.16f), ProAccentSecondary.copy(alpha = 0.16f))
                )
            )
            .border(1.dp, ProAccent.copy(alpha = 0.35f), SecondaryButtonShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = LucideIcons.Crown,
            contentDescription = null,
            tint = ProAccent,
            modifier = Modifier.size(20.dp)
        )
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "CIPHER PRO",
                style = Typography.labelMedium.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.6.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = tier.uppercase(),
                style = Typography.labelSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LabelledDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = text,
            style = Typography.labelMedium.copy(
                fontFamily = Lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                letterSpacing = 0.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
internal fun StaggeredEntrance(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = 380, delayMillis = delayMillis)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 380, delayMillis = delayMillis, easing = FastOutSlowInEasing)
            ) { fullHeight -> fullHeight / 10 }
    ) {
        content()
    }
}
