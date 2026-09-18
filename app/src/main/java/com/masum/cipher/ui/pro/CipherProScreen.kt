package com.masum.cipher.ui.pro

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.masum.cipher.R
import com.masum.cipher.core.data.local.pref.UserPreferences
import com.masum.cipher.core.security.LicenseEngine
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.BellRing
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.ExternalLink
import compose.icons.lucideicons.FileText
import compose.icons.lucideicons.Key
import compose.icons.lucideicons.Layers
import compose.icons.lucideicons.LayoutGrid
import compose.icons.lucideicons.Palette
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.X
import kotlinx.coroutines.launch

enum class ProTierSelection {
    MONTHLY,
    LIFETIME
}

private data class ProPerkItem(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
)

fun Modifier.dotGridPattern(
    dotColor: Color,
    dotRadius: Float = 2.4f,
    spacing: Float = 26f,
    fadeHeightFraction: Float = 0.60f
): Modifier = this.drawBehind {
    val cols = (size.width / spacing).toInt() + 1
    val rows = ((size.height * fadeHeightFraction) / spacing).toInt() + 1
    for (i in 0..cols) {
        for (j in 0..rows) {
            val x = i * spacing
            val y = j * spacing
            val alphaProgress = 1f - (y / (size.height * fadeHeightFraction)).coerceIn(0f, 1f)
            val currentAlpha = (alphaProgress * 0.55f).coerceAtLeast(0f)
            if (currentAlpha > 0.01f) {
                drawCircle(
                    color = dotColor.copy(alpha = currentAlpha),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherProScreen(
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit,
    dodoCheckoutUrlMonthly: String = "https://test.checkout.dodopayments.com/buy/pdt_0NnrurXNQQ9SdcuXSgqzT?quantity=1",
    dodoCheckoutUrlLifetime: String = "https://test.checkout.dodopayments.com/buy/pdt_0Nnrtp1txcVAsuHEgyyDa?quantity=1"
) {
    val settings by userPreferences.settingsFlow.collectAsStateWithLifecycle(initialValue = userPreferences.getCachedSettings())
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val licenseEngine = remember { LicenseEngine() }

    val isHapticsEnabled = settings.isHapticsEnabled
    val isPro = settings.isPro
    val proTier = settings.proTier

    var selectedTier by remember { mutableStateOf(ProTierSelection.LIFETIME) }
    var showKeyActivation by remember { mutableStateOf(false) }
    var licenseKeyInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var isActivating by remember { mutableStateOf(false) }
    var activationError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "glow_rotation")
    val glowAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_angle"
    )

    val perks = remember {
        listOf(
            ProPerkItem(
                icon = LucideIcons.Layers,
                titleRes = R.string.pro_perk_unlimited_accounts_title,
                descriptionRes = R.string.pro_perk_unlimited_accounts_desc
            ),
            ProPerkItem(
                icon = LucideIcons.FileText,
                titleRes = R.string.pro_perk_export_title,
                descriptionRes = R.string.pro_perk_export_desc
            ),
            ProPerkItem(
                icon = LucideIcons.Sparkles,
                titleRes = R.string.pro_perk_smart_rules_title,
                descriptionRes = R.string.pro_perk_smart_rules_desc
            ),
            ProPerkItem(
                icon = LucideIcons.LayoutGrid,
                titleRes = R.string.pro_perk_card_themes_title,
                descriptionRes = R.string.pro_perk_card_themes_desc
            ),
            ProPerkItem(
                icon = LucideIcons.Palette,
                titleRes = R.string.pro_perk_accent_colors_title,
                descriptionRes = R.string.pro_perk_accent_colors_desc
            ),
            ProPerkItem(
                icon = LucideIcons.ShieldCheck,
                titleRes = R.string.pro_perk_offline_crypto_title,
                descriptionRes = R.string.pro_perk_offline_crypto_desc
            )
        )
    }

    val heroAccentColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                },
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "cipher.",
                                style = Typography.displaySmall.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = (-1.6).sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isPro) stringResource(R.string.pro_license_already_active) else stringResource(R.string.pro_subtitle),
                            style = Typography.bodyMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled, isLongPress = false)
                            onNavigateBack()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.X,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    heroAccentColor,
                                    heroAccentColor.copy(alpha = 0.90f),
                                    heroAccentColor.copy(alpha = 0.80f)
                                )
                            )
                        )
                        .dotGridPattern(
                            dotColor = Color.White,
                            dotRadius = 1.6f,
                            spacing = 20f,
                            fadeHeightFraction = 0.45f
                        )
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (selectedTier == ProTierSelection.LIFETIME) "₹499" else "₹59",
                                    style = Typography.displaySmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        letterSpacing = (-0.6).sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = if (selectedTier == ProTierSelection.LIFETIME) " /lifetime" else " /month",
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (selectedTier == ProTierSelection.LIFETIME) stringResource(R.string.pro_tier_lifetime_desc) else "₹708 / year",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = stringResource(R.string.pro_perk_unlimited_accounts_desc),
                            style = Typography.bodySmall.copy(
                                fontFamily = DMSans,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp
                            ),
                            color = Color.White.copy(alpha = 0.90f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.pro_title),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                perks.forEach { perk ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = perk.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stringResource(perk.titleRes),
                                                style = Typography.titleSmall.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = stringResource(perk.descriptionRes),
                                                style = Typography.bodySmall.copy(
                                                    fontFamily = DMSans,
                                                    fontSize = 11.5.sp,
                                                    lineHeight = 15.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            if (!isPro) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val monthlySelected = selectedTier == ProTierSelection.MONTHLY
                        val lifetimeSelected = selectedTier == ProTierSelection.LIFETIME

                        val monthlyBorderColor by animateColorAsState(
                            targetValue = if (monthlySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            label = "monthly_border"
                        )
                        val monthlyBgColor by animateColorAsState(
                            targetValue = if (monthlySelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            label = "monthly_bg"
                        )

                        val lifetimeBorderColor by animateColorAsState(
                            targetValue = if (lifetimeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            label = "lifetime_border"
                        )
                        val lifetimeBgColor by animateColorAsState(
                            targetValue = if (lifetimeSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            label = "lifetime_bg"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(monthlyBgColor)
                                .border(
                                    width = if (monthlySelected) 1.5.dp else 1.dp,
                                    color = monthlyBorderColor,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    selectedTier = ProTierSelection.MONTHLY
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (monthlySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .background(if (monthlySelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (monthlySelected) {
                                            Icon(
                                                imageVector = LucideIcons.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${stringResource(R.string.pro_tier_monthly)} ₹59",
                                        style = Typography.titleMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "₹708 / year",
                                    style = Typography.bodySmall.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(lifetimeBgColor)
                                .border(
                                    width = if (lifetimeSelected) 1.5.dp else 1.dp,
                                    color = lifetimeBorderColor,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    view.performVibrate(isHapticsEnabled)
                                    selectedTier = ProTierSelection.LIFETIME
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 1.5.dp,
                                                color = if (lifetimeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                shape = CircleShape
                                            )
                                            .background(if (lifetimeSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (lifetimeSelected) {
                                            Icon(
                                                imageVector = LucideIcons.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${stringResource(R.string.pro_tier_lifetime)} ₹499",
                                        style = Typography.titleMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(EmeraldIncome.copy(alpha = 0.16f))
                                        .border(0.8.dp, EmeraldIncome.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                    Text(
                                        text = "Save ₹708/yr forever",
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            letterSpacing = 0.3.sp
                                        ),
                                        color = EmeraldIncome
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .drawWithContent {
                                rotate(glowAngle) {
                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF6366F1),
                                                Color(0xFFFFD700),
                                                Color(0xFF10B981),
                                                Color(0xFF06B6D4),
                                                Color(0xFF6366F1)
                                            )
                                        ),
                                        radius = size.maxDimension
                                    )
                                }
                                drawContent()
                            }
                            .padding(2.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                view.performVibrate(isHapticsEnabled, isLongPress = true)
                                try {
                                    val targetUrl = if (selectedTier == ProTierSelection.LIFETIME) dodoCheckoutUrlLifetime else dodoCheckoutUrlMonthly
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Sparkles,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedTier == ProTierSelection.LIFETIME) stringResource(R.string.pro_btn_upgrade) else stringResource(R.string.pro_tier_monthly),
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = LucideIcons.ExternalLink,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    TextButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            showKeyActivation = !showKeyActivation
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = LucideIcons.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.pro_license_title),
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showKeyActivation,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                                .imePadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.pro_license_title),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            BasicTextField(
                                value = licenseKeyInput,
                                onValueChange = { 
                                    licenseKeyInput = it.uppercase()
                                    activationError = null
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 1.dp,
                                        color = if (activationError != null) RoseExpense else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                textStyle = Typography.bodyMedium.copy(
                                    fontFamily = DMSans,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = 1.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Characters,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        if (licenseKeyInput.isNotBlank()) {
                                            isActivating = true
                                            coroutineScope.launch {
                                                val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "DEVICE_${System.currentTimeMillis()}"
                                                val res = licenseEngine.activateLicenseRemote(licenseKeyInput, emailInput.ifBlank { null }, deviceId)
                                                if (res.isValid) {
                                                    userPreferences.setProStatus(
                                                        isPro = true,
                                                        tier = res.tier.identifier,
                                                        token = licenseKeyInput.trim(),
                                                        orderId = res.orderId
                                                    )
                                                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                                                    snackbarHostState.showSnackbar(context.getString(R.string.pro_license_success))
                                                } else {
                                                    activationError = res.errorMessage ?: context.getString(R.string.pro_license_invalid)
                                                }
                                                isActivating = false
                                            }
                                        }
                                    }
                                ),
                                decorationBox = { innerTextField ->
                                    if (licenseKeyInput.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.pro_license_hint),
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = DMSans,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            activationError?.let { err ->
                                Text(
                                    text = err,
                                    style = Typography.bodySmall.copy(fontFamily = DMSans, fontSize = 11.5.sp),
                                    color = RoseExpense
                                )
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    isActivating = true
                                    coroutineScope.launch {
                                        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "DEVICE_${System.currentTimeMillis()}"
                                        val res = licenseEngine.activateLicenseRemote(licenseKeyInput, emailInput.ifBlank { null }, deviceId)
                                        if (res.isValid) {
                                            userPreferences.setProStatus(
                                                isPro = true,
                                                tier = res.tier.identifier,
                                                token = licenseKeyInput.trim(),
                                                orderId = res.orderId
                                            )
                                            view.performVibrate(isHapticsEnabled, isLongPress = true)
                                            snackbarHostState.showSnackbar(context.getString(R.string.pro_license_success))
                                        } else {
                                            activationError = res.errorMessage ?: context.getString(R.string.pro_license_invalid)
                                        }
                                        isActivating = false
                                    }
                                },
                                enabled = licenseKeyInput.isNotBlank() && !isActivating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                if (isActivating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.pro_btn_activate),
                                        style = Typography.titleSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(EmeraldIncome.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = EmeraldIncome.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldIncome.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.ShieldCheck,
                                        contentDescription = null,
                                        tint = EmeraldIncome,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Active License: $proTier",
                                        style = Typography.titleMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = EmeraldIncome
                                    )
                                    Text(
                                        text = stringResource(R.string.pro_perk_offline_crypto_desc),
                                        style = Typography.bodySmall.copy(
                                            fontFamily = DMSans,
                                            fontSize = 11.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    view.performVibrate(isHapticsEnabled)
                                    coroutineScope.launch {
                                        userPreferences.setProStatus(
                                            isPro = false,
                                            tier = "free",
                                            token = null,
                                            orderId = null
                                        )
                                        snackbarHostState.showSnackbar("Switched back to Free tier")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    text = "Deactivate & Return to Free",
                                    style = Typography.labelMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
