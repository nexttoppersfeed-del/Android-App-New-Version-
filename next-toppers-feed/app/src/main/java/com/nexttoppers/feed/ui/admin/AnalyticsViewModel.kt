package com.nexttoppers.feed.ui.admin

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AnalyticsPlaceholder(
    val label: String,
    val value: String,
    val trend: String = "+0%",
    val trendUp: Boolean = true,
    val color: Long = 0xFF00FF87
)

data class AnalyticsUiState(
    val metrics: List<AnalyticsPlaceholder> = listOf(
        AnalyticsPlaceholder("Daily Active Users", "—", "+0%", true, 0xFF00FF87),
        AnalyticsPlaceholder("Premium Conversion", "—", "+0%", true, 0xFFFFD700),
        AnalyticsPlaceholder("Streak Activity", "—", "+0%", true, 0xFFFF6B35),
        AnalyticsPlaceholder("Quiz Completions", "—", "+0%", true, 0xFF00E5FF),
        AnalyticsPlaceholder("Resource Downloads", "—", "+0%", true, 0xFFB388FF),
        AnalyticsPlaceholder("New Registrations", "—", "+0%", true, 0xFF69FF47)
    ),
    val topSubjects: List<Pair<String, Int>> = listOf(
        "Maths" to 0, "Science" to 0, "SST" to 0, "English" to 0
    ),
    val isPlaceholder: Boolean = true
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
}
