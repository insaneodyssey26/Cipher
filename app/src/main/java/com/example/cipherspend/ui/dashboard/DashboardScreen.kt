package com.example.cipherspend.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cipherspend.core.data.local.pref.UserPreferences
import com.example.cipherspend.ui.components.*
import com.example.cipherspend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userPreferences: UserPreferences,
    onNavigateToSettings: () -> Unit,
    onNavigateToInsights: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settings by userPreferences.settingsFlow.collectAsState(initial = null)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "CipherSpend",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val privacyMode = settings?.isPrivacyModeEnabled ?: false

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                BalanceOverviewCard(
                    totalBalance = state.totalBalance,
                    income = state.totalIncome,
                    expenses = state.totalExpenses,
                    isPrivacyMode = privacyMode
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    FilledTonalButton(
                        onClick = onNavigateToInsights,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null
                            )
                            Text(
                                text = "View Spending Insights",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (state.transactions.isEmpty()) {
                item {
                    EmptyTransactionsState()
                }
            } else {
                items(
                    items = state.transactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionCard(
                        transaction = transaction,
                        isPrivacyMode = privacyMode,
                        onDelete = { 
                            viewModel.handleIntent(DashboardContract.Intent.DeleteTransaction(transaction)) 
                        }
                    )
                }
            }
        }
    }
}
