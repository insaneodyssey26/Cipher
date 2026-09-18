package com.masum.cipher.ui.pro

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
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
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.Check
import compose.icons.lucideicons.Crown
import compose.icons.lucideicons.ExternalLink
import compose.icons.lucideicons.Key
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Sparkles
import compose.icons.lucideicons.X
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.navigationBarsPadding
import com.masum.cipher.core.data.local.pref.AppTheme
import kotlinx.coroutines.launch

private data class PlanFeatureItem(
    val title: String,
    val subtitle: String,
    val isIncluded: Boolean = true
)

private data class PricingCardData(
    val planId: String,
    val title: String,
    val badge: String,
    val strikePrice: String?,
    val mainPrice: String,
    val periodLabel: String,
    val billedSubtext: String,
    val description: String,
    val accentColorDark: Color,
    val accentColorLight: Color,
    val onAccentColorDark: Color,
    val onAccentColorLight: Color,
    val checkoutUrl: String,
    val features: List<PlanFeatureItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CipherProScreen(
    userPreferences: UserPreferences,
    onNavigateBack: () -> Unit,
    dodoCheckoutUrlMonthly: String = "https://test.checkout.dodopayments.com/buy/pdt_0NnrurXNQQ9SdcuXSgqzT?quantity=1",
    dodoCheckoutUrlHalfYearly: String = "https://test.checkout.dodopayments.com/buy/pdt_0NnrurXNQQ9SdcuXSgqzT?quantity=1",
    dodoCheckoutUrlYearly: String = "https://test.checkout.dodopayments.com/buy/pdt_0NnrurXNQQ9SdcuXSgqzT?quantity=1",
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
    val systemInDark = isSystemInDarkTheme()
    val isDark = remember(settings.theme, systemInDark) {
        when (settings.theme) {
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> systemInDark
        }
    }

    val screenBg = if (isDark) Color(0xFF11151B) else Color(0xFFF6F8FA)
    val cardBg = if (isDark) Color(0xFF181D26) else Color(0xFFFFFFFF)
    val cardBorderDefault = if (isDark) Color(0xFF262E39) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val textDescription = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
    val topPillTrackBg = if (isDark) Color(0xFF262E39) else Color(0xFFE2E8F0)
    val topPillActiveBg = if (isDark) Color(0xFF11151B) else Color(0xFFFFFFFF)
    val ctaButtonBg = if (isDark) Color.White else Color(0xFF0F172A)
    val ctaButtonText = if (isDark) Color.Black else Color.White

    var showKeyActivation by remember { mutableStateOf(false) }
    var showCongratulations by remember { mutableStateOf(false) }
    var activatedTierName by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var licenseKeyInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var isActivating by remember { mutableStateOf(false) }
    var activationError by remember { mutableStateOf<String?>(null) }

    val plans = remember(dodoCheckoutUrlMonthly, dodoCheckoutUrlHalfYearly, dodoCheckoutUrlYearly, dodoCheckoutUrlLifetime) {
        listOf(
            PricingCardData(
                planId = "monthly",
                title = "Monthly Pass",
                badge = "Standard",
                strikePrice = null,
                mainPrice = "₹59",
                periodLabel = "/ month",
                billedSubtext = "₹59 billed every month",
                description = "Full Cipher Pro access on a flexible month-to-month basis. Perfect if you want to try all advanced features.",
                accentColorDark = Color(0xFF94A3B8),
                accentColorLight = Color(0xFF64748B),
                onAccentColorDark = Color(0xFF0F172A),
                onAccentColorLight = Color.White,
                checkoutUrl = dodoCheckoutUrlMonthly,
                features = listOf(
                    PlanFeatureItem("Unlimited Accounts & Wallets", "Track savings, credit cards, cash & vault balances"),
                    PlanFeatureItem("Smart Rules & Automation", "Auto-categorize transactions by merchant & note tags"),
                    PlanFeatureItem("Home Screen Widgets Suite", "Live balance trackers & instant quick-add chips"),
                    PlanFeatureItem("Custom PDF Statement Exports", "Generate audit-ready breakdowns & financial reports"),
                    PlanFeatureItem("Split Expenses & Group Debts", "Calculate shared payments with automatic settlements"),
                    PlanFeatureItem("Theme & Styling Customization", "Exclusive themes and personalized palette options")
                )
            ),
            PricingCardData(
                planId = "6month",
                title = "6-Month Pro",
                badge = "Save 16% •",
                strikePrice = "₹354",
                mainPrice = "₹299",
                periodLabel = "/ 6 months",
                billedSubtext = "Effective ₹49.80 / month",
                description = "Half a year of seamless offline wealth management. Save 16% compared to the standard monthly pass.",
                accentColorDark = Color(0xFFFBBF24),
                accentColorLight = Color(0xFFD97706),
                onAccentColorDark = Color(0xFF1C1917),
                onAccentColorLight = Color.White,
                checkoutUrl = dodoCheckoutUrlHalfYearly,
                features = listOf(
                    PlanFeatureItem("Unlimited Accounts & Wallets", "Track savings, credit cards, cash & vault balances"),
                    PlanFeatureItem("Smart Rules & Automation", "Auto-categorize transactions by merchant & note tags"),
                    PlanFeatureItem("Home Screen Widgets Suite", "Live balance trackers & instant quick-add chips"),
                    PlanFeatureItem("Custom PDF Statement Exports", "Generate audit-ready breakdowns & financial reports"),
                    PlanFeatureItem("Split Expenses & Group Debts", "Calculate shared payments with automatic settlements"),
                    PlanFeatureItem("Theme & Styling Customization", "Exclusive themes and personalized palette options")
                )
            ),
            PricingCardData(
                planId = "annual",
                title = "1-Year Annual",
                badge = "Save 30% •",
                strikePrice = "₹708",
                mainPrice = "₹499",
                periodLabel = "/ year",
                billedSubtext = "Effective ₹41.58 / month",
                description = "A full year of uninterrupted financial clarity. Best overall savings for mastering budgets, rules, and group splits.",
                accentColorDark = Color(0xFFE2FF38),
                accentColorLight = Color(0xFF059669),
                onAccentColorDark = Color.Black,
                onAccentColorLight = Color.White,
                checkoutUrl = dodoCheckoutUrlYearly,
                features = listOf(
                    PlanFeatureItem("Unlimited Accounts & Wallets", "Track savings, credit cards, cash & vault balances"),
                    PlanFeatureItem("Smart Rules & Automation", "Auto-categorize transactions by merchant & note tags"),
                    PlanFeatureItem("Home Screen Widgets Suite", "Live balance trackers & instant quick-add chips"),
                    PlanFeatureItem("Custom PDF Statement Exports", "Generate audit-ready breakdowns & financial reports"),
                    PlanFeatureItem("Split Expenses & Group Debts", "Calculate shared payments with automatic settlements"),
                    PlanFeatureItem("Theme & Styling Customization", "Exclusive themes and personalized palette options")
                )
            ),
            PricingCardData(
                planId = "lifetime",
                title = "Lifetime VIP",
                badge = "Best Deal •",
                strikePrice = null,
                mainPrice = "₹899",
                periodLabel = "one-time",
                billedSubtext = "Pay once, keep forever",
                description = "Zero recurring subscriptions ever. Complete lifetime ownership of Cipher Pro with all future pro updates included.",
                accentColorDark = Color(0xFFA78BFA),
                accentColorLight = Color(0xFF7C3AED),
                onAccentColorDark = Color(0xFF1E1B4B),
                onAccentColorLight = Color.White,
                checkoutUrl = dodoCheckoutUrlLifetime,
                features = listOf(
                    PlanFeatureItem("Unlimited Accounts & Wallets", "Track savings, credit cards, cash & vault balances"),
                    PlanFeatureItem("Smart Rules & Automation", "Auto-categorize transactions by merchant & note tags"),
                    PlanFeatureItem("Home Screen Widgets Suite", "Live balance trackers & instant quick-add chips"),
                    PlanFeatureItem("Custom PDF Statement Exports", "Generate audit-ready breakdowns & financial reports"),
                    PlanFeatureItem("Split Expenses & Group Debts", "Calculate shared payments with automatic settlements"),
                    PlanFeatureItem("Theme & Styling Customization", "Exclusive themes and personalized palette options")
                )
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 2, pageCount = { plans.size })

    Scaffold(
        containerColor = screenBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            view.performVibrate(isHapticsEnabled)
                            onNavigateBack()
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = LucideIcons.ArrowLeft,
                            contentDescription = "Back",
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(topPillTrackBg)
                            .padding(3.dp)
                    ) {
                        val tabWidth = maxWidth / plans.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * pagerState.currentPage,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                            label = "pro_tab_indicator_offset"
                        )

                        Box(
                            modifier = Modifier
                                .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                                .width(tabWidth)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(topPillActiveBg)
                        )

                        Row(modifier = Modifier.fillMaxSize()) {
                            plans.forEachIndexed { index, plan ->
                                val isSelected = pagerState.currentPage == index
                                val tabTitle = plan.title.replace("Pass", "").replace("Pro", "").replace("Annual", "").replace("VIP", "").trim()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            view.performVibrate(isHapticsEnabled)
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tabTitle,
                                        style = Typography.labelMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.5.sp
                                        ),
                                        color = if (isSelected) textPrimary else textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Cipher Pro",
                            style = Typography.displaySmall.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            ),
                            color = textPrimary
                        )
                        Text(
                            text = "Elevate your offline wealth vault",
                            style = Typography.bodySmall.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            color = textSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        plans.indices.forEach { index ->
                            val isActive = pagerState.currentPage == index
                            val plan = plans[index]
                            val planAccent = if (isDark) plan.accentColorDark else plan.accentColorLight
                            val barWidth = if (isActive) 18.dp else 6.dp
                            val barColor = if (isActive) planAccent else if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

                            Box(
                                modifier = Modifier
                                    .height(4.dp)
                                    .width(barWidth)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 14.dp
            ) { pageIndex ->
                val plan = plans[pageIndex]
                val planAccent = if (isDark) plan.accentColorDark else plan.accentColorLight
                val planOnAccent = if (isDark) plan.onAccentColorDark else plan.onAccentColorLight

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(cardBg)
                        .border(
                            width = 1.5.dp,
                            color = planAccent,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = plan.title,
                                    style = Typography.titleLarge.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp
                                    ),
                                    color = textPrimary
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(planAccent)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = plan.badge,
                                        style = Typography.labelSmall.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = planOnAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (plan.strikePrice != null) {
                                    Text(
                                        text = plan.strikePrice,
                                        style = Typography.titleLarge.copy(
                                            fontFamily = DMSans,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 20.sp,
                                            textDecoration = TextDecoration.LineThrough
                                        ),
                                        color = textSecondary,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }

                                Text(
                                    text = plan.mainPrice,
                                    style = Typography.displaySmall.copy(
                                        fontFamily = DMSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp
                                    ),
                                    color = planAccent
                                )

                                Text(
                                    text = plan.periodLabel,
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = textSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            Text(
                                text = plan.billedSubtext,
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = planAccent
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = plan.description,
                                style = Typography.bodySmall.copy(
                                    fontFamily = Lato,
                                    fontSize = 12.sp,
                                    lineHeight = 16.5.sp
                                ),
                                color = textDescription
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            HorizontalDivider(
                                color = cardBorderDefault,
                                thickness = 1.dp
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            plan.features.forEach { feat ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(planAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = LucideIcons.Check,
                                            contentDescription = null,
                                            tint = planOnAccent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = feat.title,
                                            style = Typography.bodyMedium.copy(
                                                fontFamily = Lato,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp
                                            ),
                                            color = textPrimary
                                        )
                                        if (feat.subtitle.isNotBlank()) {
                                            Text(
                                                text = feat.subtitle,
                                                style = Typography.bodySmall.copy(
                                                    fontFamily = Lato,
                                                    fontWeight = FontWeight.Normal,
                                                    fontSize = 10.5.sp,
                                                    lineHeight = 13.5.sp
                                                ),
                                                color = textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = LucideIcons.ShieldCheck,
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "No cloud servers • 100% offline on device",
                                    style = Typography.labelSmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.5.sp
                                    ),
                                    color = textSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    view.performVibrate(isHapticsEnabled, isLongPress = true)
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(plan.checkoutUrl))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ctaButtonBg,
                                    contentColor = ctaButtonText
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Sparkles,
                                        contentDescription = null,
                                        tint = ctaButtonText,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Text(
                                        text = "Get ${plan.title} — ${plan.mainPrice}",
                                        style = Typography.titleMedium.copy(
                                            fontFamily = Lato,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = ctaButtonText
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = LucideIcons.ExternalLink,
                                        contentDescription = null,
                                        tint = ctaButtonText.copy(alpha = 0.75f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            TextButton(
                onClick = {
                    view.performVibrate(isHapticsEnabled)
                    showKeyActivation = true
                },
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = LucideIcons.Key,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.pro_license_title),
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = textSecondary
                    )
                }
            }
        }
    }

    if (showKeyActivation) {
        val activePlan = plans[pagerState.currentPage.coerceIn(0, plans.lastIndex)]
        val activePlanAccent = if (isDark) activePlan.accentColorDark else activePlan.accentColorLight
        val activePlanOnAccent = if (isDark) activePlan.onAccentColorDark else activePlan.onAccentColorLight

        ModalBottomSheet(
            onDismissRequest = { showKeyActivation = false },
            sheetState = sheetState,
            containerColor = cardBg,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.pro_license_desc),
                    style = Typography.labelMedium.copy(
                        fontFamily = Lato,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = textSecondary
                )

                BasicTextField(
                    value = licenseKeyInput,
                    onValueChange = { 
                        licenseKeyInput = it.uppercase()
                        activationError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(screenBg)
                        .border(
                            width = 1.dp,
                            color = if (activationError != null) RoseExpense else activePlanAccent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp),
                    textStyle = Typography.bodyMedium.copy(
                        fontFamily = Lato,
                        color = textPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(textPrimary),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (licenseKeyInput.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.pro_license_hint),
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        color = textSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                BasicTextField(
                    value = emailInput,
                    onValueChange = { 
                        emailInput = it
                        activationError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(screenBg)
                        .border(
                            width = 1.dp,
                            color = cardBorderDefault,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp),
                    textStyle = Typography.bodyMedium.copy(
                        fontFamily = Lato,
                        color = textPrimary
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(textPrimary),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (emailInput.isEmpty()) {
                                Text(
                                    text = "Email address (optional)",
                                    style = Typography.bodyMedium.copy(
                                        fontFamily = Lato,
                                        color = textSecondary
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (activationError != null) {
                    Text(
                        text = activationError ?: "",
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            color = RoseExpense,
                            fontSize = 12.sp
                        )
                    )
                }

                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        val rawKey = licenseKeyInput.trim()
                        if (rawKey.isBlank()) {
                            activationError = "Please enter a valid license key."
                            return@Button
                        }
                        isActivating = true
                        activationError = null
                        coroutineScope.launch {
                            val result = licenseEngine.validateLicense(rawKey, emailInput.ifBlank { null })
                            if (result.isValid) {
                                userPreferences.setProStatus(
                                    isPro = true,
                                    tier = result.tier.identifier,
                                    token = rawKey,
                                    orderId = result.orderId
                                )
                                isActivating = false
                                showKeyActivation = false
                                activatedTierName = result.tier.displayName
                                showCongratulations = true
                            } else {
                                isActivating = false
                                activationError = result.errorMessage ?: "Invalid or expired license key."
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activePlanAccent,
                        contentColor = activePlanOnAccent
                    ),
                    enabled = !isActivating
                ) {
                    if (isActivating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = activePlanOnAccent,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.pro_license_verify),
                            style = Typography.labelLarge.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold
                            ),
                            color = activePlanOnAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    if (showCongratulations) {
        ProCongratulationsDialog(
            isDark = isDark,
            planTitle = activatedTierName,
            onDismiss = {
                showCongratulations = false
                onNavigateBack()
            }
        )
    }
}

@Composable
private fun ProCongratulationsDialog(
    isDark: Boolean,
    planTitle: String,
    onDismiss: () -> Unit
) {
    val dialogBg = if (isDark) Color(0xFF181D26) else Color.White
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val chipBg = if (isDark) Color(0xFF262E39) else Color(0xFFF1F5F9)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = dialogBg,
        shape = RoundedCornerShape(28.dp),
        text = {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProConfettiBurst(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .align(Alignment.TopCenter)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFE2FF38),
                                        Color(0xFFA78BFA),
                                        Color(0xFF38BDF8)
                                    )
                                )
                            )
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(dialogBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Crown,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFE2FF38) else Color(0xFF059669),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Welcome to Cipher Pro!",
                            style = Typography.titleLarge.copy(
                                fontFamily = DMSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (planTitle.isNotBlank()) "Your $planTitle is active & ready." else "All Pro features unlocked on this device.",
                            style = Typography.labelMedium.copy(
                                fontFamily = Lato,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = if (isDark) Color(0xFFE2FF38) else Color(0xFF059669),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Thank you so much for supporting Cipher! Your support directly powers private, offline software with zero trackers and zero ads.",
                        style = Typography.bodySmall.copy(
                            fontFamily = Lato,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        ),
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        listOf(
                            "Unlimited accounts & multi-currency vaults",
                            "Smart merchant auto-categorisation rules",
                            "Interactive home screen widget suite",
                            "Custom encrypted PDF statement exports",
                            "Shared bills & debt settlement calculator"
                        ).forEach { perk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(chipBg)
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFE2FF38) else Color(0xFF059669)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = LucideIcons.Check,
                                        contentDescription = null,
                                        tint = if (isDark) Color.Black else Color.White,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = perk,
                                    style = Typography.bodySmall.copy(
                                        fontFamily = Lato,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.5.sp
                                    ),
                                    color = textPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color.White else Color(0xFF0F172A),
                            contentColor = if (isDark) Color.Black else Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.Sparkles,
                                contentDescription = null,
                                tint = if (isDark) Color.Black else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Explore Pro Vault",
                                style = Typography.titleMedium.copy(
                                    fontFamily = Lato,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isDark) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    )
}

private data class ProConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val angle: Double,
    val speed: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val shapeType: Int
)

@Composable
private fun ProConfettiBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val animProgress = remember { Animatable(0f) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        )
    }

    val palette = remember {
        listOf(
            Color(0xFFE2FF38),
            Color(0xFFA78BFA),
            Color(0xFF38BDF8),
            Color(0xFFFBBF24),
            Color(0xFFF43F5E),
            Color(0xFF34D399)
        )
    }

    val particles = remember {
        List(particleCount) {
            val angle = Math.toRadians((0..360).random().toDouble())
            val speed = (150..520).random().toFloat()
            val rotationSpeed = (-540..540).random().toFloat()
            val size = (5..12).random().toFloat()
            val color = palette.random()
            val shapeType = (0..2).random()
            ProConfettiParticle(0.5f, 0.16f, angle, speed, rotationSpeed, size, color, shapeType)
        }
    }

    Canvas(modifier = modifier) {
        val progress = animProgress.value
        if (progress >= 1f) return@Canvas

        val alpha = (1f - progress).coerceIn(0f, 1f)
        val gravity = 420f * progress * progress

        particles.forEach { p ->
            val distance = p.speed * progress
            val x = size.width * p.initialX + (kotlin.math.cos(p.angle) * distance).toFloat()
            val y = size.height * p.initialY + (kotlin.math.sin(p.angle) * distance).toFloat() + gravity
            val rotation = p.rotationSpeed * progress

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                when (p.shapeType) {
                    0 -> drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size,
                        center = Offset(x, y)
                    )
                    1 -> drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size, y - p.size * 0.6f),
                        size = Size(p.size * 2f, p.size * 1.2f)
                    )
                    else -> drawRoundRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size * 0.7f, y - p.size * 1.4f),
                        size = Size(p.size * 1.4f, p.size * 2.8f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
            }
        }
    }
}
