package com.masum.cipher.ui.privacy

import android.content.Intent
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.masum.cipher.R
import com.masum.cipher.core.util.performVibrate
import com.masum.cipher.ui.theme.DMSans
import compose.icons.LucideIcons
import compose.icons.lucideicons.ArrowLeft
import compose.icons.lucideicons.ArrowRight
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.Database
import compose.icons.lucideicons.ExternalLink
import compose.icons.lucideicons.FolderDown
import compose.icons.lucideicons.Github
import compose.icons.lucideicons.Globe
import compose.icons.lucideicons.Info
import compose.icons.lucideicons.Key
import compose.icons.lucideicons.Layers
import compose.icons.lucideicons.Lock
import compose.icons.lucideicons.Mail
import compose.icons.lucideicons.RefreshCw
import compose.icons.lucideicons.ShieldAlert
import compose.icons.lucideicons.ShieldCheck
import compose.icons.lucideicons.Smartphone
import compose.icons.lucideicons.Sparkles
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current
    val tabs = listOf(
        stringResource(R.string.tab_privacy_policy),
        stringResource(R.string.tab_terms_of_service)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.privacy_terms_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(LucideIcons.ArrowLeft, contentDescription = stringResource(R.string.action_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp)
            ) {
                val tabWidth = maxWidth / tabs.size
                val scrollProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction
                val indicatorOffset = tabWidth * scrollProgress

                Box(
                    modifier = Modifier
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (pagerState.currentPage != index) {
                                        view.performVibrate(true, isLongPress = false)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                if (page == 0) {
                    PrivacyPolicyPage(
                        onSwitchToTerms = {
                            view.performVibrate(true, isLongPress = false)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                } else {
                    TermsOfServicePage(
                        onSwitchToPrivacy = {
                            view.performVibrate(true, isLongPress = false)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyPage(onSwitchToTerms: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PolicyHeaderCard(
                badgeText = stringResource(R.string.privacy_badge),
                badgeIcon = LucideIcons.ShieldCheck,
                tagline = stringResource(R.string.privacy_policy_tagline)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.ShieldCheck,
                title = stringResource(R.string.privacy_short_version_title),
                body = stringResource(R.string.privacy_short_version_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.Database,
                title = stringResource(R.string.privacy_data_storage_title),
                body = stringResource(R.string.privacy_data_storage_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.Smartphone,
                title = stringResource(R.string.privacy_permissions_title),
                body = stringResource(R.string.privacy_permissions_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.Lock,
                title = stringResource(R.string.privacy_biometrics_title),
                body = stringResource(R.string.privacy_biometrics_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.Globe,
                title = stringResource(R.string.privacy_network_title),
                body = stringResource(R.string.privacy_network_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.FolderDown,
                title = stringResource(R.string.privacy_exports_title),
                body = stringResource(R.string.privacy_exports_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.Github,
                title = stringResource(R.string.privacy_open_source_title),
                body = stringResource(R.string.privacy_open_source_body)
            )
        }

        item {
            PolicyCard(
                icon = LucideIcons.RefreshCw,
                title = stringResource(R.string.privacy_changes_title),
                body = stringResource(R.string.privacy_changes_body)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, "https://github.com/insaneodyssey26/cipher".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(LucideIcons.Github, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("GitHub", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onSwitchToTerms,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.tab_terms_of_service), maxLines = 1)
                        Spacer(Modifier.width(6.dp))
                        Icon(LucideIcons.ArrowRight, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = stringResource(R.string.privacy_last_updated),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TermsOfServicePage(onSwitchToPrivacy: () -> Unit) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PolicyHeaderCard(
                badgeText = stringResource(R.string.terms_badge),
                badgeIcon = LucideIcons.Layers,
                tagline = stringResource(R.string.terms_tagline)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_service_model_title),
                body = stringResource(R.string.terms_service_model_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_licensing_title),
                body = stringResource(R.string.terms_licensing_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_subscriptions_title),
                body = stringResource(R.string.terms_subscriptions_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_distribution_title),
                body = stringResource(R.string.terms_distribution_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_disclaimer_title),
                body = stringResource(R.string.terms_disclaimer_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_data_ownership_title),
                body = stringResource(R.string.terms_data_ownership_body)
            )
        }

        item {
            PolicyCard(
                title = stringResource(R.string.terms_contact_title),
                body = stringResource(R.string.terms_contact_body)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, "mailto:skmasumali.dev@gmail.com".toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(LucideIcons.Mail, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Contact", maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onSwitchToPrivacy,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.tab_privacy_policy), maxLines = 1)
                        Spacer(Modifier.width(6.dp))
                        Icon(LucideIcons.ArrowRight, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = stringResource(R.string.terms_last_updated),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PolicyHeaderCard(
    badgeText: String,
    badgeIcon: ImageVector,
    tagline: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "cipher.",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = DMSans,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 21.sp
            )
        }
    }
}

private val POLICY_LINKS = listOf(
    "Google Play Store" to "https://play.google.com/store/apps/details?id=com.masum.cipher",
    "Google Play" to "https://play.google.com/store/apps/details?id=com.masum.cipher",
    "insaneodyssey26/cipher" to "https://github.com/insaneodyssey26/cipher",
    "skmasumali.dev@gmail.com" to "mailto:skmasumali.dev@gmail.com"
)

private fun buildPolicyAnnotatedString(body: String, linkColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < body.length) {
            var firstMatchIndex = -1
            var matchedPhrase = ""
            var matchedUrl = ""

            for ((phrase, url) in POLICY_LINKS) {
                val index = body.indexOf(phrase, currentIndex)
                if (index != -1 && (firstMatchIndex == -1 || index < firstMatchIndex)) {
                    firstMatchIndex = index
                    matchedPhrase = phrase
                    matchedUrl = url
                }
            }

            if (firstMatchIndex != -1) {
                append(body.substring(currentIndex, firstMatchIndex))
                withLink(
                    LinkAnnotation.Url(
                        url = matchedUrl,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    )
                ) {
                    append(matchedPhrase)
                }
                currentIndex = firstMatchIndex + matchedPhrase.length
            } else {
                append(body.substring(currentIndex))
                break
            }
        }
    }
}

@Composable
private fun PolicyCard(
    title: String,
    body: String,
    icon: ImageVector? = null
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedBody = remember(body, linkColor) {
        buildPolicyAnnotatedString(body, linkColor)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = annotatedBody,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                lineHeight = 22.sp
            )
        }
    }
}
