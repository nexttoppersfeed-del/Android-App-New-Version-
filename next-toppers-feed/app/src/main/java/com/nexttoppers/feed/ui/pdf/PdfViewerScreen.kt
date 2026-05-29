package com.nexttoppers.feed.ui.pdf

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceDark
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun PdfViewerScreen(
    onBack: () -> Unit,
    viewModel: PdfViewerViewModel = hiltViewModel()
) {
    val state       by viewModel.state.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        when (val s = state) {
            is PdfViewerState.Loading -> PdfLoadingState()
            is PdfViewerState.Error   -> PdfErrorState(message = s.message, onBack = onBack)
            is PdfViewerState.Ready   -> {
                PdfPager(
                    pageCount   = s.pageCount,
                    currentPage = currentPage,
                    title       = viewModel.resourceTitle,
                    onPageChange = viewModel::onPageChanged,
                    renderPage  = viewModel::renderPage,
                    onBack      = onBack
                )
            }
        }
    }
}

// ── Pager with zoom ────────────────────────────────────────────────────────────
@Composable
private fun PdfPager(
    pageCount: Int,
    currentPage: Int,
    title: String,
    onPageChange: (Int) -> Unit,
    renderPage: suspend (Int, Int) -> Bitmap?,
    onBack: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope      = rememberCoroutineScope()

    // Keep ViewModel in sync
    LaunchedEffect(pagerState.currentPage) { onPageChange(pagerState.currentPage) }

    // UI chrome visibility (tap to toggle)
    var chromeVisible by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        // ── Page pager ────────────────────────────────────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { chromeVisible = !chromeVisible })
                }
        ) { pageIndex ->
            ZoomablePdfPage(
                pageIndex  = pageIndex,
                renderPage = renderPage
            )
        }

        // ── Top chrome ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = chromeVisible,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(BackgroundBlack.copy(0.95f), Color.Transparent)
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
                    .padding(top = 40.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(SurfaceElevated.copy(0.9f), RoundedCornerShape(50.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        title,
                        color  = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 15.sp,
                        maxLines   = 1,
                        modifier   = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Bottom chrome — page indicator + prev/next ────────────────────────
        AnimatedVisibility(
            visible  = chromeVisible,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, BackgroundBlack.copy(0.95f))
                        )
                    )
                    .padding(bottom = 32.dp, top = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Prev
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        enabled  = pagerState.currentPage > 0
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, null,
                            tint = if (pagerState.currentPage > 0) NeonGreen else TextMuted)
                    }

                    // Page counter pill
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(listOf(NeonGreen.copy(0.2f), NeonCyan.copy(0.2f))),
                                RoundedCornerShape(50.dp)
                            )
                            .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "${pagerState.currentPage + 1} / $pageCount",
                            color      = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp
                        )
                    }

                    // Next
                    IconButton(
                        onClick  = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        enabled  = pagerState.currentPage < pageCount - 1
                    ) {
                        Icon(Icons.Rounded.ChevronRight, null,
                            tint = if (pagerState.currentPage < pageCount - 1) NeonGreen else TextMuted)
                    }
                }
            }
        }
    }
}

// ── Single page with pinch-zoom ────────────────────────────────────────────────
@Composable
private fun ZoomablePdfPage(
    pageIndex: Int,
    renderPage: suspend (Int, Int) -> Bitmap?
) {
    var bitmap    by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
    var viewWidth by remember { mutableStateOf(1080) }
    var scale     by remember { mutableFloatStateOf(1f) }
    var offset    by remember { mutableStateOf(Offset.Zero) }

    // Render when width is known
    LaunchedEffect(pageIndex, viewWidth) {
        bitmap = renderPage(pageIndex, viewWidth)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewWidth = it.width }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(1f, 5f)
                    scale  = newScale
                    // Clamp panning within scaled content bounds
                    val maxX = (viewWidth * (scale - 1f) / 2f).coerceAtLeast(0f)
                    offset = Offset(
                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                        y = (offset.y + pan.y)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap!!.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale       = ContentScale.Fit,
                modifier           = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX        = scale
                        scaleY        = scale
                        translationX  = offset.x
                        translationY  = offset.y
                    }
            )
        } else {
            CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(36.dp), strokeWidth = 2.dp)
        }
    }
}

// ── Loading state ──────────────────────────────────────────────────────────────
@Composable
private fun PdfLoadingState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color       = NeonGreen,
            modifier    = Modifier.size(52.dp),
            strokeWidth = 3.dp
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Opening document…",
            style = TextStyle(
                color  = NeonGreen,
                fontSize = 16.sp,
                shadow = Shadow(NeonGreen.copy(0.5f), Offset.Zero, 10f)
            )
        )
        Spacer(Modifier.height(8.dp))
        Text("Please wait", color = TextMuted, fontSize = 13.sp)
    }
}

// ── Error state ────────────────────────────────────────────────────────────────
@Composable
private fun PdfErrorState(message: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Error, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Cannot open PDF", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text(message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .background(NeonGreen.copy(0.12f), RoundedCornerShape(12.dp))
                .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { onBack() }) }
        ) {
            Text("Go Back", color = NeonGreen, fontWeight = FontWeight.SemiBold)
        }
    }
}
