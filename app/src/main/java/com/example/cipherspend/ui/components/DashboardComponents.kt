package com.example.cipherspend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cipherspend.core.data.local.entity.TransactionEntity
import com.example.cipherspend.ui.theme.ExpenseRed
import com.example.cipherspend.ui.theme.IncomeGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BalanceOverviewCard(
    totalBalance: Double,
    income: Double,
    expenses: Double,
    isPrivacyMode: Boolean = false
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        color = colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (isPrivacyMode) "₹••••" else currencyFormatter.format(totalBalance),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        ),
                        color = colorScheme.onPrimaryContainer
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatItem(
                    label = "Income",
                    amount = income,
                    color = IncomeGreen,
                    isPrivacyMode = isPrivacyMode,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Expenses",
                    amount = expenses,
                    color = colorScheme.onPrimaryContainer,
                    isPrivacyMode = isPrivacyMode,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    amount: Double,
    color: Color,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
        Text(
            text = if (isPrivacyMode) "₹•••" else currencyFormatter.format(amount),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    isPrivacyMode: Boolean = false,
    onDelete: (TransactionEntity) -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }
    }

    val timeFormatter = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    val dayFormatter = remember {
        SimpleDateFormat("MMM dd", Locale.getDefault())
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        headlineContent = {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "${dayFormatter.format(Date(transaction.timestamp))} • ${timeFormatter.format(Date(transaction.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Text(
                text = if (isPrivacyMode) {
                    "${if (transaction.isIncome) "+" else "-"} ₹••"
                } else {
                    "${if (transaction.isIncome) "+" else "-"} ${currencyFormatter.format(transaction.amount).replace("₹", "")}"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
                )
            )
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.isIncome) Icons.Default.AccountBalanceWallet else Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (transaction.isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Your transactions will appear here as they are received via SMS.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
