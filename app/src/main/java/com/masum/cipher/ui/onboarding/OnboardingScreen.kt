package com.masum.cipher.ui.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.masum.cipher.R
import com.masum.cipher.core.data.local.pref.AccentColor
import com.masum.cipher.core.data.local.pref.AppTheme
import com.masum.cipher.core.domain.model.AppCurrency
import com.masum.cipher.core.domain.model.AppLanguage
import com.masum.cipher.core.util.AppFormatters
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.components.VaultCard
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Manrope
import com.masum.cipher.ui.theme.RoseExpense
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowRight
import compose.icons.lucideicons.ChartBar
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.ChevronDown
import compose.icons.lucideicons.Globe
import compose.icons.lucideicons.Moon
import compose.icons.lucideicons.Plus
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Sun
import compose.icons.lucideicons.SunMoon
import compose.icons.lucideicons.Wallet
import compose.icons.lucideicons.WifiOff
import compose.icons.lucideicons.Zap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OnboardingScreen(
    currentAccentColor: AccentColor,
    onAccentColorSelected: (AccentColor) -> Unit,
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    currentCurrencyCode: String,
    currentCurrencySymbol: String,
    onCurrencySelected: (code: String, symbol: String) -> Unit,
    onComplete: () -> Unit,
    onSaveApps: (Set<String>) -> Unit
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val totalPages = 6
    var showQuickLangDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            OnboardingTopBar(
                currentPage = page,
                totalPages = totalPages,
                currentLanguageCode = currentLanguageCode,
                onOpenLanguagePicker = { showQuickLangDialog = true },
                onBack = { if (page > 0) page -= 1 }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val entering = targetState > initialState
                        if (entering) {
                            (fadeIn(tween(320)) + slideInVertically(tween(320, easing = FastOutSlowInEasing)) { it / 10 })
                                .togetherWith(fadeOut(tween(200)))
                        } else {
                            fadeIn(tween(280)) togetherWith fadeOut(tween(180))
                        }
                    },
                    label = "onboarding_page"
                ) { currentPage ->
                    when (currentPage) {
                        0 -> WelcomePage(
                            currentLanguageCode = currentLanguageCode,
                            onOpenLanguagePicker = { showQuickLangDialog = true },
                            onNext = { page = 1 }
                        )
                        1 -> ThemeSelectionPage(
                            currentAccentColor = currentAccentColor,
                            onAccentColorSelected = onAccentColorSelected,
                            currentTheme = currentTheme,
                            onThemeSelected = onThemeSelected,
                            currencySymbol = currentCurrencySymbol,
                            onNext = { page = 2 }
                        )
                        2 -> LanguageSelectionPage(
                            currentLanguageCode = currentLanguageCode,
                            onLanguageSelected = onLanguageSelected,
                            onNext = { page = 3 }
                        )
                        3 -> CurrencySelectionPage(
                            currentCurrencyCode = currentCurrencyCode,
                            currentCurrencySymbol = currentCurrencySymbol,
                            currentLanguageCode = currentLanguageCode,
                            onCurrencySelected = onCurrencySelected,
                            onNext = { page = 4 }
                        )
                        4 -> PermissionPage(onComplete = { page = 5 })
                        else -> AppSelectionScreen(
                            initialSelectedApps = emptySet(),
                            onComplete = { apps ->
                                onSaveApps(apps)
                                onComplete()
                            }
                        )
                    }
                }
            }
        }

        BackHandler(enabled = page > 0) { page -= 1 }
    }

    if (showQuickLangDialog) {
        QuickLanguagePickerModal(
            currentLanguageCode = currentLanguageCode,
            onLanguageSelected = { code ->
                onLanguageSelected(code)
                showQuickLangDialog = false
            },
            onDismiss = { showQuickLangDialog = false }
        )
    }
}

@Composable
private fun OnboardingTopBar(
    currentPage: Int,
    totalPages: Int,
    currentLanguageCode: String,
    onOpenLanguagePicker: () -> Unit,
    onBack: () -> Unit
) {
    val activeLang = remember(currentLanguageCode) { AppLanguage.fromCode(currentLanguageCode) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (currentPage > 0) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LucideIcons.ArrowLeft,
                        contentDescription = stringResource(R.string.action_back),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage > 0) {
                repeat(totalPages) { idx ->
                    val isFilled = idx < currentPage
                    val isActive = idx == currentPage
                    val colorAlpha = when {
                        isActive -> 1f
                        isFilled -> 0.38f
                        else -> 0.12f
                    }
                    val dotWidth by animateFloatAsState(
                        targetValue = if (isActive) 20f else 6f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "dot_w_$idx"
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Box(
                        modifier = Modifier
                            .size(width = dotWidth.dp, height = 6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = colorAlpha))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
            }
        }

        Box(
            modifier = Modifier.width(72.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOpenLanguagePicker
                    )
                    .padding(horizontal = 9.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = LucideIcons.Globe,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(11.dp)
                    )
                    AnimatedContent(
                        targetState = activeLang.countryCode,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                        label = "lang_code"
                    ) { code ->
                        Text(
                            text = code,
                            style = Typography.labelSmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                    Icon(
                        imageVector = LucideIcons.ChevronDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(
    currentLanguageCode: String,
    onOpenLanguagePicker: () -> Unit,
    onNext: () -> Unit
) {
    val view = LocalView.current

    val greetings = listOf("Hello", "নমস্কার", "नमस्ते", "こんにちは", "Hola", "Bonjour", "Hallo")
    var greetingIndex by remember { mutableIntStateOf(0) }

    val contentAlpha = remember { Animatable(0f) }
    val contentOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { contentOffset.animateTo(0f, spring(stiffness = 200f, dampingRatio = Spring.DampingRatioMediumBouncy)) }
        contentAlpha.animateTo(1f, tween(500))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2800.milliseconds)
            greetingIndex = (greetingIndex + 1) % greetings.size
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffset.value
                },
            horizontalAlignment = Alignment.Start
        ) {
            AnimatedContent(
                targetState = greetings[greetingIndex],
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(280)) },
                label = "greeting"
            ) { greeting ->
                Text(
                    text = greeting,
                    style = Typography.bodyLarge.copy(
                        fontFamily = Manrope,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "cipher.",
                style = Typography.displayLarge.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 52.sp,
                    letterSpacing = (-2.5).sp,
                    lineHeight = 56.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.onboarding_welcome_tagline),
                style = Typography.titleMedium.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    letterSpacing = (-0.1).sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = contentAlpha.value },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WelcomeFeatureItem(
                icon = LucideIcons.WifiOff,
                title = stringResource(R.string.onboarding_feat_offline_title),
                description = stringResource(R.string.onboarding_feat_offline_desc)
            )
            WelcomeFeatureItem(
                icon = LucideIcons.Zap,
                title = stringResource(R.string.onboarding_feat_auto_title),
                description = stringResource(R.string.onboarding_feat_auto_desc)
            )
            WelcomeFeatureItem(
                icon = LucideIcons.ShieldCheck,
                title = stringResource(R.string.onboarding_feat_encryption_title),
                description = stringResource(R.string.onboarding_feat_encryption_desc)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .graphicsLayer { alpha = contentAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryActionButton(
                label = stringResource(R.string.onboarding_welcome_begin),
                onClick = {
                    view.performVibrate(true, isLongPress = false)
                    onNext()
                }
            )
        }
    }
}

@Composable
private fun WelcomeFeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleSmall.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = Typography.bodySmall.copy(
                    fontFamily = Manrope,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ThemeSelectionPage(
    currentAccentColor: AccentColor,
    onAccentColorSelected: (AccentColor) -> Unit,
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
    currencySymbol: String,
    onNext: () -> Unit
) {
    val view = LocalView.current
    var selectedColor by remember(currentAccentColor) { mutableStateOf(currentAccentColor) }
    var selectedTheme by remember(currentTheme) { mutableStateOf(currentTheme) }
    val dynamicColor = Color(selectedColor.colorValue)

    val heroAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(340))
        delay(110)
        contentAlpha.animateTo(1f, tween(380))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = heroAlpha.value }) {
                Column {
                    StepLabel(text = stringResource(R.string.onboarding_step_appearance))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_theme_title),
                        style = Typography.displaySmall.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_theme_subtitle),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = contentAlpha.value }) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        listOf(
                            Triple(AppTheme.SYSTEM, stringResource(R.string.onboarding_theme_mode_system), LucideIcons.SunMoon),
                            Triple(AppTheme.LIGHT, stringResource(R.string.onboarding_theme_mode_light), LucideIcons.Sun),
                            Triple(AppTheme.DARK, stringResource(R.string.onboarding_theme_mode_dark), LucideIcons.Moon)
                        ).forEach { (theme, label, icon) ->
                            val isSelected = selectedTheme == theme
                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed) 0.96f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "theme_scale_$theme"
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(scale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) dynamicColor.copy(alpha = 0.10f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) dynamicColor.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(interactionSource = interactionSource, indication = null) {
                                        view.performVibrate(true, isLongPress = false)
                                        selectedTheme = theme
                                        onThemeSelected(theme)
                                    }
                                    .padding(vertical = 14.dp, horizontal = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) dynamicColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = label,
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) dynamicColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    VaultCard(
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentPadding = 20.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.onboarding_theme_preview_label),
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Manrope,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = dynamicColor
                                )
                                AnimatedContent(
                                    targetState = selectedColor.colorName,
                                    transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
                                    label = "color_name"
                                ) { name ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(dynamicColor.copy(alpha = 0.11f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = name,
                                            style = Typography.labelSmall.copy(
                                                fontFamily = Manrope,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            color = dynamicColor
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "$currencySymbol 18,450.00",
                                style = Typography.displaySmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    letterSpacing = (-1.5).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(0.64f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(dynamicColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(0.36f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(RoseExpense.copy(alpha = 0.6f))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Income",
                                    style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 11.sp),
                                    color = dynamicColor.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "Expenses",
                                    style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 11.sp),
                                    color = RoseExpense.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        AccentColor.entries.chunked(2).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                rowColors.forEach { color ->
                                    val isSelected = selectedColor == color
                                    val swatchColor = Color(color.colorValue)
                                    ColorTile(
                                        modifier = Modifier.weight(1f),
                                        color = swatchColor,
                                        name = color.colorName,
                                        isSelected = isSelected,
                                        onClick = {
                                            view.performVibrate(true, isLongPress = true)
                                            selectedColor = color
                                            onAccentColorSelected(color)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        PrimaryActionButton(
            label = stringResource(R.string.action_continue),
            onClick = {
                view.performVibrate(true, isLongPress = false)
                onNext()
            }
        )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun ColorTile(
    modifier: Modifier = Modifier,
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "color_tile_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.11f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .border(
                1.dp,
                if (isSelected) color.copy(alpha = 0.42f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = LucideIcons.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Text(
                text = name,
                style = Typography.titleSmall.copy(
                    fontFamily = Manrope,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LanguageSelectionPage(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    val view = LocalView.current
    var selectedLang by rememberSaveable { mutableStateOf(currentLanguageCode) }

    val heroAlpha = remember { Animatable(0f) }
    val gridAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(340))
        delay(120)
        gridAlpha.animateTo(1f, tween(360))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = heroAlpha.value }) {
                Column {
                    StepLabel(text = stringResource(R.string.onboarding_step_region))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_section_language)
                            .lowercase().replaceFirstChar { it.uppercase() },
                        style = Typography.displaySmall.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_lang_subtitle),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = gridAlpha.value }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    AppLanguage.SUPPORTED_LANGUAGES.chunked(2).forEach { rowLangs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            rowLangs.forEach { lang ->
                                val isSelected = selectedLang.equals(lang.code, ignoreCase = true)
                                SelectionTile(
                                    modifier = Modifier.weight(1f),
                                    isSelected = isSelected,
                                    onClick = {
                                        view.performVibrate(true, isLongPress = false)
                                        selectedLang = lang.code
                                        onLanguageSelected(lang.code)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = lang.nativeName,
                                                style = Typography.titleSmall.copy(
                                                    fontFamily = Manrope,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 14.sp
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = lang.name,
                                                style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 10.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = lang.countryCode,
                                                style = Typography.labelSmall.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowLangs.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        PrimaryActionButton(
            label = stringResource(R.string.action_continue),
            onClick = {
                view.performVibrate(true, isLongPress = false)
                onNext()
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun CurrencySelectionPage(
    currentCurrencyCode: String,
    currentCurrencySymbol: String,
    currentLanguageCode: String,
    onCurrencySelected: (code: String, symbol: String) -> Unit,
    onNext: () -> Unit
) {
    val view = LocalView.current
    var selectedCode by rememberSaveable { mutableStateOf(currentCurrencyCode) }
    var selectedSym by rememberSaveable { mutableStateOf(currentCurrencySymbol) }

    val activeLocale = remember(currentLanguageCode) {
        if (currentLanguageCode == "system") Locale.getDefault() else Locale.forLanguageTag(currentLanguageCode)
    }

    val heroAlpha = remember { Animatable(0f) }
    val previewAlpha = remember { Animatable(0f) }
    val gridAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(320))
        delay(80)
        previewAlpha.animateTo(1f, tween(340))
        delay(90)
        gridAlpha.animateTo(1f, tween(340))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = heroAlpha.value }) {
                Column {
                    StepLabel(text = stringResource(R.string.onboarding_step_region))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_section_currency)
                            .lowercase().replaceFirstChar { it.uppercase() },
                        style = Typography.displaySmall.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_lang_subtitle),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = previewAlpha.value }) {
                VaultCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.onboarding_live_preview_label),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.onboarding_preview_merchant),
                                style = Typography.titleSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.onboarding_preview_category),
                                style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        AnimatedContent(
                            targetState = selectedSym,
                            transitionSpec = { fadeIn(tween(260)) togetherWith fadeOut(tween(180)) },
                            label = "preview_amount"
                        ) { sym ->
                            val formatted = AppFormatters.formatCurrency(14.50, sym, activeLocale, 2)
                            Text(
                                text = "-$formatted",
                                style = Typography.titleLarge.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = RoseExpense
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = gridAlpha.value }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    AppCurrency.SUPPORTED_CURRENCIES.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            row.forEach { cur ->
                                val isSelected = selectedCode.equals(cur.code, ignoreCase = true)
                                SelectionTile(
                                    modifier = Modifier.weight(1f),
                                    isSelected = isSelected,
                                    onClick = {
                                        view.performVibrate(true, isLongPress = false)
                                        selectedCode = cur.code
                                        selectedSym = cur.symbol
                                        onCurrencySelected(cur.code, cur.symbol)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cur.code,
                                                style = Typography.titleSmall.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = cur.name,
                                                style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cur.symbol,
                                            style = Typography.titleMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                }
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        PrimaryActionButton(
            label = stringResource(R.string.action_continue),
            onClick = {
                view.performVibrate(true, isLongPress = false)
                onNext()
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun PermissionPage(onComplete: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var hasPostNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    var hasNotificationAccess by remember {
        mutableStateOf(NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName))
    }
    var showSkipDialog by remember { mutableStateOf(false) }
    var showPostNotifDialog by remember { mutableStateOf(false) }
    var showTour by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    hasPostNotificationPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                }
                hasNotificationAccess = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasSmsPermission = result[Manifest.permission.RECEIVE_SMS] == true || hasSmsPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPostNotificationPermission = result[Manifest.permission.POST_NOTIFICATIONS] == true || hasPostNotificationPermission
        }
    }

    val heroAlpha = remember { Animatable(0f) }
    val cardsAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        heroAlpha.animateTo(1f, tween(340))
        delay(130)
        cardsAlpha.animateTo(1f, tween(380))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = heroAlpha.value }) {
                Column {
                    StepLabel(text = stringResource(R.string.onboarding_step_automation))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_auto_title),
                        style = Typography.displaySmall.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            letterSpacing = (-0.8).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.onboarding_perm_intro),
                        style = Typography.bodyMedium.copy(
                            fontFamily = Manrope,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Box(modifier = Modifier.graphicsLayer { alpha = cardsAlpha.value }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CapabilityCard(
                        icon = LucideIcons.Wallet,
                        title = stringResource(R.string.onboarding_perm_sms_title),
                        description = stringResource(R.string.onboarding_perm_sms_desc),
                        isEnabled = hasSmsPermission,
                        onToggle = {
                            if (!hasSmsPermission) {
                                permissionLauncher.launch(arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS))
                            }
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        CapabilityCard(
                            icon = LucideIcons.Zap,
                            title = stringResource(R.string.onboarding_perm_alerts_title),
                            description = stringResource(R.string.onboarding_perm_alerts_desc),
                            isEnabled = hasPostNotificationPermission,
                            onToggle = {
                                if (!hasPostNotificationPermission) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                                }
                            }
                        )
                    }

                    CapabilityCard(
                        icon = LucideIcons.ChartBar,
                        title = stringResource(R.string.onboarding_perm_notif_title),
                        description = stringResource(R.string.onboarding_perm_notif_desc),
                        isEnabled = hasNotificationAccess,
                        onToggle = {
                            if (!hasNotificationAccess) {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { showTour = true }) {
                    Text(
                        text = stringResource(R.string.onboarding_take_tour),
                        style = Typography.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        PrimaryActionButton(
            label = stringResource(R.string.action_continue),
            onClick = {
                view.performVibrate(true, isLongPress = false)
                if (!hasSmsPermission || !hasNotificationAccess) {
                    showSkipDialog = true
                } else if (!hasPostNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showPostNotifDialog = true
                } else {
                    onComplete()
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Missing Automation Permissions", style = Typography.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold)) },
            text = { Text("Cipher relies on local SMS and notification access to log transactions automatically without cloud sync. Without these, transactions must be logged manually.", style = Typography.bodyMedium.copy(fontFamily = Manrope)) },
            confirmButton = {
                Button(onClick = { showSkipDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Grant Permissions", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false; onComplete() }) {
                    Text("Proceed Without Automation", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    if (showPostNotifDialog) {
        AlertDialog(
            onDismissRequest = { showPostNotifDialog = false },
            title = { Text("Missing Alert Permissions", style = Typography.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold)) },
            text = { Text("Without notification permissions, you won't receive daily summaries, subscription alerts, or threshold warnings.", style = Typography.bodyMedium.copy(fontFamily = Manrope)) },
            confirmButton = {
                Button(onClick = { showPostNotifDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Grant Permission", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostNotifDialog = false; onComplete() }) {
                    Text("Proceed Without Alerts", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }

    if (showTour) {
        AppTourDialog(onDismiss = { showTour = false })
    }
}

@Composable
private fun CapabilityCard(
    icon: ImageVector,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cap_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.055f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
            )
            .border(
                1.dp,
                if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                RoundedCornerShape(16.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onToggle(!isEnabled) }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(21.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = title,
                        style = Typography.titleSmall.copy(
                            fontFamily = Manrope,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isEnabled) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldIncome.copy(alpha = 0.13f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.onboarding_status_active),
                                style = Typography.labelSmall.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.5.sp
                                ),
                                color = EmeraldIncome
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = Typography.bodySmall.copy(
                        fontFamily = Manrope,
                        fontSize = 11.5.sp,
                        lineHeight = 15.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(checked = isEnabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun QuickLanguagePickerModal(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        VaultCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 20.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dialog_language_title),
                        style = Typography.titleLarge.copy(
                            fontFamily = DMSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            style = Typography.titleMedium.copy(fontFamily = DMSans, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.SUPPORTED_LANGUAGES.chunked(2).forEach { rowLangs ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowLangs.forEach { lang ->
                                val isSelected = currentLanguageCode.equals(lang.code, ignoreCase = true)
                                SelectionTile(
                                    modifier = Modifier.weight(1f),
                                    isSelected = isSelected,
                                    onClick = {
                                        view.performVibrate(true, isLongPress = false)
                                        onLanguageSelected(lang.code)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = lang.nativeName,
                                                style = Typography.titleSmall.copy(
                                                    fontFamily = Manrope,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    fontSize = 13.sp
                                                ),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = lang.name,
                                                style = Typography.bodySmall.copy(fontFamily = Manrope, fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = LucideIcons.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowLangs.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = Typography.labelSmall.copy(
                fontFamily = Manrope,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SelectionTile(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tile_scale"
    )
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) { content() }
}

@Composable
private fun PrimaryActionButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btn_scale"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = Typography.titleMedium.copy(
                    fontFamily = Manrope,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Icon(
                imageVector = LucideIcons.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.80f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AppTourDialog(onDismiss: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 3 })

        VaultCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentPadding = 24.dp
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) { tourPage ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (tourPage) {
                            0 -> TourSlide(
                                icon = LucideIcons.Wallet,
                                title = "Command Dashboard",
                                body = "Your financial balance and flows appear here automatically in real-time as alerts are captured."
                            )
                            1 -> TourSlide(
                                icon = LucideIcons.Plus,
                                title = "Manual Expenses",
                                body = "Paid with cash? Tap the centered + button on the navigation dock anytime to log offline entries."
                            )
                            else -> TourSlide(
                                icon = LucideIcons.ChartBar,
                                title = "Deep Analytics",
                                body = "Explore the Insights tab to examine category velocity, recurring commitments, and heatmaps."
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(3) { idx ->
                        val isActive = pagerState.currentPage == idx
                        val dotW by animateFloatAsState(
                            targetValue = if (isActive) 20f else 6f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "tour_dot_$idx"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(width = dotW.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    label = if (pagerState.currentPage == 2) "Got It" else "Next",
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TourSlide(icon: ImageVector, title: String, body: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = Typography.titleLarge.copy(
                fontFamily = DMSans,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = (-0.4).sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            style = Typography.bodyMedium.copy(
                fontFamily = Manrope,
                fontSize = 12.5.sp,
                lineHeight = 17.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
