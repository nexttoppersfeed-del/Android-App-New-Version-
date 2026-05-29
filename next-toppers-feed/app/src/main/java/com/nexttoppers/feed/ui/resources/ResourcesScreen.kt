package com.nexttoppers.feed.ui.resources

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.SkeletonRow
import com.nexttoppers.feed.ui.resources.components.ResourceListCard
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private data class SubjectCardData(
    val subject: ResourceSubject,
    val icon: ImageVector,
    val iconBg: Color,
    val description: String,
    val badge: String
)

private val subjectCards = listOf(
    SubjectCardData(ResourceSubject.MATHS,   Icons.Rounded.Calculate,  Color(0xFF3B82F6), "Algebra, Geometry, Trigonometry, Calculus", "Core Subject"),
    SubjectCardData(ResourceSubject.SCIENCE, Icons.Rounded.Science,    Color(0xFF10B981), "Physics, Chemistry, Biology — theory & practicals", "Core Subject"),
    SubjectCardData(ResourceSubject.SST,     Icons.Rounded.Public,     Color(0xFF8B5CF6), "History, Geography, Civics & Economics", "Core Subject"),
    SubjectCardData(ResourceSubject.ENGLISH, Icons.Rounded.MenuBook,   Color(0xFFF59E0B), "Grammar, Literature, Comprehension & Writing", "Language"),
    SubjectCardData(ResourceSubject.HINDI,   Icons.Rounded.Translate,  Color(0xFFEF4444), "Vyakaran, Sahitya, Nibandh & Comprehension", "Language"),
)

@Composable
fun ResourcesScreen(
    onNavigateToSubject: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ResourcesViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        when (val state = uiState) {
            is ResourcesUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SkeletonRow()
                    repeat(5) { SkeletonCard(height = 90.dp) }
                }
            }

            is ResourcesUiState.Error -> {
                Text(state.message, color = TextSecondary, modifier = Modifier.align(Alignment.Center))
            }

            is ResourcesUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        // ── Header ────────────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 56.dp, bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        "Subjects",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Select a subject to access all files,\nlectures, and study resources.",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                                Text("🎓", fontSize = 48.sp)
                            }
                        }
                    }

                    // ── Search bar ────────────────────────────────────────
                    item {
                        OutlinedTextField(
                            value         = searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            placeholder   = { Text("Search subjects, notes, lectures...", color = TextMuted, fontSize = 14.sp) },
                            leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 8.dp),
                            shape  = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard,
                                focusedBorderColor      = AccentCyan.copy(0.6f),
                                unfocusedBorderColor    = SurfaceElevated,
                                focusedTextColor        = TextPrimary,
                                unfocusedTextColor      = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    // ── Search results overlay ────────────────────────────
                    when (val s = searchState) {
                        is SearchState.Searching -> item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(28.dp))
                            }
                        }
                        is SearchState.Empty -> item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No results for \"$searchQuery\"", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                        is SearchState.Results -> {
                            item { Spacer(Modifier.height(8.dp)) }
                            items(s.items) { resource ->
                                ResourceListCard(
                                    resource  = resource,
                                    onClick   = { onNavigateToDetail(resource.id) },
                                )
                            }
                        }
                        else -> {
                            // ── Subject cards ─────────────────────────────
                            items(subjectCards) { card ->
                                val count = state.subjectCounts.firstOrNull {
                                    it.subject == card.subject
                                }?.count ?: 0
                                SubjectCard(
                                    data    = card,
                                    count   = count,
                                    onClick = { onNavigateToSubject(card.subject.name) }
                                )
                            }

                            // ── Free resources banner ─────────────────────
                            item {
                                FreeResourcesBanner()
                            }

                            // ── Recent resources ───────────────────────────
                            if (state.recent.isNotEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        SectionHeader("Recent Resources")
                                    }
                                }
                                items(state.recent.take(5)) { resource ->
                                    ResourceListCard(
                                        resource = resource,
                                        onClick  = { onNavigateToDetail(resource.id) },
                                    
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectCard(data: SubjectCardData, count: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, data.iconBg.copy(0.12f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(data.iconBg, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                data.icon, null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    data.subject.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = data.iconBg
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(data.iconBg.copy(0.12f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(data.badge, fontSize = 10.sp, color = data.iconBg, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                data.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 17.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Folder, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    if (count > 0) "$count resources" else "Explore",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(data.iconBg.copy(0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.ChevronRight, null, tint = data.iconBg, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FreeResourcesBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NeonGreen.copy(0.06f))
            .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("📗", fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "All resources are free for enrolled students",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                "PDFs, lecture recordings, DPPs and more — curated by your teachers.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
