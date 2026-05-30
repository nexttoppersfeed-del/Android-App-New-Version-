package com.nexttoppers.feed.ui.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.RecentlyOpened
import com.nexttoppers.feed.data.repository.OfflineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed class PdfViewerState {
    object Loading  : PdfViewerState()
    data class Ready(val pageCount: Int) : PdfViewerState()
    data class Error(val message: String) : PdfViewerState()
}

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    private val offlineRepository: OfflineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val resourceId: String    = savedStateHandle["resourceId"] ?: ""
    val resourceTitle: String = java.net.URLDecoder.decode(savedStateHandle["title"]     ?: "Document", "UTF-8")
    val localPath: String     = java.net.URLDecoder.decode(savedStateHandle["localPath"] ?: "",         "UTF-8")
    val fileUrl: String       = java.net.URLDecoder.decode(savedStateHandle["fileUrl"]   ?: "",         "UTF-8")

    private val _state = MutableStateFlow<PdfViewerState>(PdfViewerState.Loading)
    val state: StateFlow<PdfViewerState> = _state

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    // Cache rendered bitmaps by page index (LRU-style, keep ±2 pages)
    private val bitmapCache = LinkedHashMap<Int, Bitmap>(10, 0.75f, true)

    init {
        openPdf()
        recordOpen()
    }

    private fun openPdf() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(localPath)
                if (!file.exists()) {
                    _state.value = PdfViewerState.Error("File not found at: $localPath")
                    return@launch
                }
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                parcelFileDescriptor = pfd
                pdfRenderer = PdfRenderer(pfd)
                val count = pdfRenderer!!.pageCount
                _state.value = PdfViewerState.Ready(count)
            } catch (e: Exception) {
                _state.value = PdfViewerState.Error(e.message ?: "Cannot open PDF")
            }
        }
    }

    /**
     * Renders a single PDF page to Bitmap (called from Composable).
     * Returns cached bitmap if available.
     */
    suspend fun renderPage(index: Int, viewWidth: Int): Bitmap? = withContext(Dispatchers.IO) {
        val renderer = pdfRenderer ?: return@withContext null
        if (index < 0 || index >= renderer.pageCount) return@withContext null

        bitmapCache[index]?.let { return@withContext it }

        return@withContext try {
            val page   = renderer.openPage(index)
            val scale  = if (page.width > 0) viewWidth.toFloat() / page.width else 1f
            val width  = (page.width  * scale).toInt().coerceAtLeast(1)
            val height = (page.height * scale).toInt().coerceAtLeast(1)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Trim cache to 6 bitmaps
            if (bitmapCache.size >= 6) {
                val oldest = bitmapCache.keys.first()
                bitmapCache.remove(oldest)?.recycle()
            }
            bitmapCache[index] = bitmap
            bitmap
        } catch (e: Exception) { null }
    }

    fun onPageChanged(page: Int) { _currentPage.value = page }

    private fun recordOpen() {
        if (resourceId.isBlank()) return
        viewModelScope.launch {
            offlineRepository.recordOpen(
                RecentlyOpened(
                    resourceId = resourceId,
                    title      = resourceTitle,
                    type       = "PDF",
                    subject    = "",
                    localPath  = localPath
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        } catch (_: Exception) {}
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}
