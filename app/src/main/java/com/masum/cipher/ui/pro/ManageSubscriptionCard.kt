package com.masum.cipher.ui.pro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.masum.cipher.R
import com.masum.cipher.ui.theme.DMSans
import com.masum.cipher.ui.theme.EmeraldIncome
import com.masum.cipher.ui.theme.Lato
import com.masum.cipher.ui.theme.Typography
import compose.icons.LucideIcons
import compose.icons.lucideicons.CreditCard
import compose.icons.lucideicons.ExternalLink

internal const val CUSTOMER_PORTAL_URL = "https://customer.dodopayments.com"

private val SUBSCRIPTION_TIERS = setOf(
    "MONTHLY",
    "HALF_YEARLY",
    "6MONTH",
    "6-MONTH",
    "ANNUAL",
    "YEARLY",
    "1-YEAR",
    "1YEAR"
)

internal fun isSubscriptionTier(tier: String): Boolean = tier.trim().uppercase() in SUBSCRIPTION_TIERS

@Composable
internal fun ManageSubscriptionCard(
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textDescription: Color,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = LucideIcons.CreditCard,
                contentDescription = null,
                tint = EmeraldIncome,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.pro_subscription_title),
                style = Typography.titleMedium.copy(
                    fontFamily = DMSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = textPrimary
            )
        }

        Text(
            text = stringResource(R.string.pro_subscription_body),
            style = Typography.bodySmall.copy(
                fontFamily = Lato,
                fontSize = 12.sp,
                lineHeight = 17.sp
            ),
            color = textDescription
        )

        OutlinedButton(
            onClick = onManageClick,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, EmeraldIncome.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                text = stringResource(R.string.pro_subscription_manage),
                style = Typography.labelLarge.copy(
                    fontFamily = Lato,
                    fontWeight = FontWeight.Bold
                ),
                color = textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = LucideIcons.ExternalLink,
                contentDescription = null,
                tint = EmeraldIncome,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
