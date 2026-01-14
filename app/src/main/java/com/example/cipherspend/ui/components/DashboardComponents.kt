package com.example.cipherspend.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
    expenses: Double
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(240.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val path = Path().apply {
                        moveTo(0f, size.height * 0.7f)
                        quadraticBezierTo(
                            size.width * 0.3f, size.height * 0.6f,
                            size.width * 0.6f, size.height * 0.8f
                        )
                        quadraticBezierTo(
                            size.width * 0.8f, size.height * 0.9f,
                            size.width, size.height * 0.7f
                        )
                        lineTo(size.width, size.height)
                        lineTo(0f, size.height)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)
                        )
                    )
                }
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL BALANCE",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = currencyFormatter.format(totalBalance),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MiniStatItem(
                        label = "Income",
                        amount = income,
                        icon = Icons.Default.ArrowDownward,
                        color = IncomeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MiniStatItem(
                        label = "Spent",
                        amount = expenses,
                        icon = Icons.Default.ArrowUpward,
                        color = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MiniStatItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = currencyFormatter.format(amount),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onDelete: (TransactionEntity) -> Unit
) {
    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
        }
    }

    val timeFormatter = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }

    val dayFormatter = remember {
        SimpleDateFormat("dd MMM", Locale.getDefault())
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High-end Date Badge
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .padding(end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayFormatter.format(Date(transaction.timestamp)).split(" ")[0],
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = dayFormatter.format(Date(transaction.timestamp)).split(" ")[1].uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Merchant Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = timeFormatter.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Amount with Type indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.isIncome) "+" else "-"}₹${"%,.2f".format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (transaction.isIncome) IncomeGreen else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (transaction.isIncome) "CREDIT" else "DEBIT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    ),
                    color = if (transaction.isIncome) IncomeGreen.copy(alpha = 0.7f) else ExpenseRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Clean Slate",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Waiting for your first transaction...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
