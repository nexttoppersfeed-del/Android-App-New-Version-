package com.nexttoppers.feed.ui.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexttoppers.feed.data.model.RecentlyOpened
import com.nexttoppers.feed.data.repository.OfflineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject

sealed class PdfViewerState {
    object Loading  : PdfViewerState()
    object Downloading : PdfViewerState()   // fetching from URL to temp cache
    data class Ready(val pageCount: Int) : PdfViewerState()
    data class Error(val message: String) : PdfViewerState()
}

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    /** Tracks the resolved local path (either passed-in or temp-cached from URL) */
    private val _resolvedPath = MutableStateFlow<String?>(null)
    val resolvedPath: StateFlow<String?> = _resolvedPath

    /** Download progress 0–100 when fetching from URL */
    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var tempCacheFile: File? = null

    // LRU bitmap cache (keep ±3 pages)
    private val bitmapCache = LinkedHashMap<Int, Bitmap>(10, 0.75f, true)

    init {
        openPdf()
        recordOpen()
    }

    private fun openPdf() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when {
                    localPath.isNotEmpty() -> {
                        // Open from already-downloaded local file
                        val file = File(localPath)
                        if (!file.exists()) {
                            _state.value = PdfViewerState.Error("Downloaded file not found.\nTry downloading again.")
                            return@launch
                        }
                        _resolvedPath.value = localPath
                        openFromFile(file)
                    }

                    fileUrl.isNotEmpty() -> {
                        // Stream from URL → temp cache
                        _state.value = PdfViewerState.Downloading
                        val tempFile = downloadToCache(fileUrl)
                        if (tempFile != null) {
                            tempCacheFile = tempFile
                            _resolvedPath.value = tempFile.absolutePath
                            openFromFile(tempFile)
                        } else {
                            _state.value = PdfViewerState.Error(
                                "Cannot load PDF.\nCheck your internet connection and try again."
                            )
                        }
                    }

                    else -> {
                        _state.value = PdfViewerState.Error("No file path or URL provided.")
                    }
                }
            } catch (e: Exception) {
                _state.value = PdfViewerState.Error(e.message ?: "Cannot open PDF")
            }
        }
    }

    private fun openFromFile(file: File) {
        val pfd  = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        parcelFileDescriptor = pfd
        pdfRenderer = PdfRenderer(pfd)
        _state.value = PdfViewerState.Ready(pdfRenderer!!.pageCount)
    }

    private suspend fun downloadToCache(url: String): File? = withContext(Dispatchers.IO) {
        return@withContext try {
            val cacheDir = File(context.cacheDir, "pdf_cache")
            cacheDir.mkdirs()
            val fileName = "pdf_${resourceId.ifBlank { System.currentTimeMillis().toString() }}.pdf"
            val file = File(cacheDir, fileName)

            // If cached version exists and is non-zero, reuse it
            if (file.exists() && file.length() > 0) return@withContext file

            val connection = URL(url).openConnection()
            connection.connectTimeout = 15_000
            connection.readTimeout    = 60_000
            connection.connect()
            val total = connection.contentLength.toLong()

            connection.getInputStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var downloaded = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) {
                            _downloadProgress.value = ((downloaded * 100) / total).toInt()
                        }
                    }
                }
            }
            _downloadProgress.value = 100
            file
        } catch (e: Exception) {
            null
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

    /** Share the PDF via the system share sheet */
    fun sharePdf() {
        val path = _resolvedPath.value ?: return
        val file = File(path)
        if (!file.exists()) return
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, resourceTitle)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF via"))
        } catch (e: Exception) {
            // Ignore share failures
        }
    }

    /** Open PDF in an external app */
    fun openInExternalApp() {
        val path = _resolvedPath.value ?: fileUrl
        if (path.isBlank()) return
        try {
            val intent = if (path.startsWith("http")) {
                Intent(Intent.ACTION_VIEW, Uri.parse(path))
            } else {
                val file = File(path)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // No PDF app installed — fallback to browser
            runCatching {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    /** Returns file size label for the resolved file (e.g. "4.2 MB") */
    fun getFileSizeLabel(): String {
        val path = _resolvedPath.value ?: return ""
        val bytes = File(path).length()
        return when {
            bytes <= 0             -> ""
            bytes < 1024           -> "$bytes B"
            bytes < 1024 * 1024    -> "${bytes / 1024} KB"
            else                   -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }

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
        // Clean up temp file to free cache space
        tempCacheFile?.let { file ->
            if (file.exists()) file.delete()
        }
    }
}
