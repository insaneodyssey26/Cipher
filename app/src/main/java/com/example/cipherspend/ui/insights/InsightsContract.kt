package com.example.cipherspend.ui.insights

import com.example.cipherspend.core.mvi.UiEffect
import com.example.cipherspend.core.mvi.UiIntent
import com.example.cipherspend.core.mvi.UiState
import com.example.cipherspend.ui.dashboard.DashboardContract

class InsightsContract {
    sealed class Intent : UiIntent {
        object LoadInsights : Intent()
    }

    data class State(
        val isLoading: Boolean = true,
        val spendingVelocity: DashboardContract.VelocityData = DashboardContract.VelocityData(),
        val netWorthHistory: List<DashboardContract.Point> = emptyList(),
        val calendarHeatmap: Map<Long, Double> = emptyMap(),
        val categoryBreakdown: List<DashboardContract.CategoryData> = emptyList()
    ) : UiState

    sealed class Effect : UiEffect
}