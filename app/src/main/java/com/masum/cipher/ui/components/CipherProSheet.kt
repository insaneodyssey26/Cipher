package com.masum.cipher.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.ExternalLink
import compose.icons.lucideicons.FileSpreadsheet
import compose.icons.lucideicons.Key
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import compose.icons.lucideicons.Layers
import compose.icons.lucideicons.Palette
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.Wand
import compose.icons.lucideicons.X
import compose.icons.lucideicons.Zap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CipherProSheet(
    isPro: Boolean,
    proTier: String,
    isActivating: Boolean,
    activationError: String?,
    activationSuccess: Boolean,
    isHapticsEnabled: Boolean,
    onActivateKey: (String, String?) -> Unit,
    onDismiss: () -> Unit,
    dodoCheckoutUrl: String = "https://checkout.dodopayments.com/buy"
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var showKeyInput by remember { mutableStateOf(false) }
    var licenseKeyInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "shimmer_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_offset"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets.navigationBars.union(WindowInsets.ime) },
        modifier = Modifier
            .fillMaxHeight(0.92f)
            .imeNestedScroll()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
                )
                IconButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        coroutineScope.launch { sheetState.hide() }
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.X,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.25f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                MaterialTheme.colorScheme.primary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LucideIcons.Crown,
                    contentDescription = "Cipher Pro",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isPro) stringResource(R.string.pro_license_already_active) else stringResource(R.string.pro_btn_upgrade),
                style = Typography.headlineMedium.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.pro_subtitle),
                style = Typography.bodyMedium.copy(
                    fontFamily = DMSans,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProFeatureRow(
                    icon = LucideIcons.Palette,
                    title = stringResource(R.string.pro_perk_card_themes_title),
                    subtitle = stringResource(R.string.pro_perk_card_themes_desc)
                )
                ProFeatureRow(
                    icon = LucideIcons.Wand,
                    title = stringResource(R.string.pro_perk_smart_rules_title),
                    subtitle = stringResource(R.string.pro_perk_smart_rules_desc)
                )
                ProFeatureRow(
                    icon = LucideIcons.FileSpreadsheet,
                    title = stringResource(R.string.pro_perk_export_title),
                    subtitle = stringResource(R.string.pro_perk_export_desc)
                )
                ProFeatureRow(
                    icon = LucideIcons.Layers,
                    title = stringResource(R.string.pro_perk_unlimited_accounts_title),
                    subtitle = stringResource(R.string.pro_perk_unlimited_accounts_desc)
                )
                ProFeatureRow(
                    icon = LucideIcons.ShieldCheck,
                    title = stringResource(R.string.pro_perk_offline_crypto_title),
                    subtitle = stringResource(R.string.pro_perk_offline_crypto_desc)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isPro) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    Color(0xFFFFD700).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    Color(0xFFFFD700).copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.pro_badge_lifetime),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.pro_badge_popular),
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.pro_tier_lifetime_desc),
                                style = Typography.bodySmall.copy(
                                    fontFamily = DMSans,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        view.performVibrate(isHapticsEnabled, isLongPress = true)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(dodoCheckoutUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = LucideIcons.Sparkles,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.pro_btn_upgrade),
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = LucideIcons.ExternalLink,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        view.performVibrate(isHapticsEnabled)
                        showKeyInput = !showKeyInput
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LucideIcons.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
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
                    visible = showKeyInput,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp),
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
                            onValueChange = { licenseKeyInput = it.uppercase() },
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
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            textStyle = Typography.bodyMedium.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.SemiBold,
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
                                    keyboardController?.hide()
                                    if (licenseKeyInput.isNotBlank()) {
                                        onActivateKey(licenseKeyInput, emailInput.ifBlank { null })
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
                                style = Typography.bodySmall.copy(fontFamily = DMSans, fontSize = 11.sp),
                                color = RoseExpense
                            )
                        }

                        Button(
                            onClick = {
                                view.performVibrate(isHapticsEnabled, isLongPress = true)
                                keyboardController?.hide()
                                onActivateKey(licenseKeyInput, emailInput.ifBlank { null })
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
            } else {
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
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = LucideIcons.Check,
                            contentDescription = null,
                            tint = EmeraldIncome,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Licensed: $proTier Access Unlocked",
                            style = Typography.titleMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold
                            ),
                            color = EmeraldIncome
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(
                    fontFamily = DMSans,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
