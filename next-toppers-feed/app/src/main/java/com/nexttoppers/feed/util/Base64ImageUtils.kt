package com.nexttoppers.feed.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Utilities for lightweight Base64 image encoding/decoding.
 *
 * Firebase Storage is NOT used in this project.
 * Small images (banners, payment screenshots, thumbnails) are stored as
 * Base64 strings directly in Firestore documents.
 *
 * Allowed use cases:
 *   • Payment proof screenshots   → [encodePaymentScreenshot]   (max 500 KB decoded)
 *   • Announcement banners        → [bitmapToBase64]             (max 200 KB decoded)
 *   • Popup images / thumbnails   → [bitmapToBase64]             (max 100 KB decoded)
 *
 * NOT allowed (use external URLs instead):
 *   • PDFs, videos, audio
 *   • Profile photos (use photoUrl from Google Sign-In or external CDN)
 *   • Gallery images, bulk media
 *
 * Firestore rule enforces max 700 000 chars (≈ 500 KB decoded) for
 * paymentScreenshotBase64 fields.
 */
object Base64ImageUtils {

    // ── Size limits ────────────────────────────────────────────────────────────

    /** Max decoded size for payment screenshots in bytes (500 KB). */
    const val MAX_PAYMENT_SCREENSHOT_BYTES = 500 * 1024

    /** Max decoded size for banners and popups in bytes (200 KB). */
    const val MAX_BANNER_BYTES = 200 * 1024

    /** Max decoded size for thumbnails in bytes (100 KB). */
    const val MAX_THUMBNAIL_BYTES = 100 * 1024

    // ── Encoding ───────────────────────────────────────────────────────────────

    /**
     * Encode a [Bitmap] to a Base64 string (JPEG, default 70% quality).
     *
     * @param bitmap    Source bitmap to encode.
     * @param quality   JPEG compression quality 0–100 (default 70).
     * @param maxWidth  Downscale bitmap if wider than this (default 800 px).
     * @param maxHeight Downscale bitmap if taller than this (default 800 px).
     * @return Base64-encoded string, or null if encoding fails.
     */
    fun bitmapToBase64(
        bitmap: Bitmap,
        quality: Int = 70,
        maxWidth: Int = 800,
        maxHeight: Int = 800
    ): String? = runCatching {
        val scaled = scaleBitmap(bitmap, maxWidth, maxHeight)
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(10, 100), stream)
        if (scaled !== bitmap) scaled.recycle()
        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }.getOrNull()

    /**
     * Encode a payment screenshot from a [Uri] with extra compression.
     * Targets under [MAX_PAYMENT_SCREENSHOT_BYTES] decoded.
     *
     * @param context  App context for content resolver.
     * @param uri      URI of the image (from gallery / camera intent).
     * @return Base64-encoded string, or null on failure.
     */
    fun encodePaymentScreenshot(context: Context, uri: Uri): String? = runCatching {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        original ?: return null

        // Aggressive compression: max 600 px, 65% quality, target < 500 KB
        var quality = 65
        var result: String?
        do {
            result = bitmapToBase64(original, quality = quality, maxWidth = 600, maxHeight = 600)
            quality -= 10
        } while (result != null &&
            Base64.decode(result, Base64.NO_WRAP).size > MAX_PAYMENT_SCREENSHOT_BYTES &&
            quality > 20)

        original.recycle()
        result
    }.getOrNull()

    /**
     * Encode a banner/popup image from a [Uri].
     * Targets under [MAX_BANNER_BYTES] decoded.
     *
     * @param context  App context.
     * @param uri      Image URI.
     * @return Base64-encoded string, or null on failure.
     */
    fun encodeBanner(context: Context, uri: Uri): String? = runCatching {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val original = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        original ?: return null

        val result = bitmapToBase64(original, quality = 75, maxWidth = 600, maxHeight = 400)
        original.recycle()
        result
    }.getOrNull()

    // ── Decoding ───────────────────────────────────────────────────────────────

    /**
     * Decode a Base64 string back to a [Bitmap].
     *
     * @param base64  Base64-encoded image string.
     * @return Decoded [Bitmap], or null if the string is blank or invalid.
     */
    fun base64ToBitmap(base64: String): Bitmap? {
        if (base64.isBlank()) return null
        return runCatching {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }.getOrNull()
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    /**
     * Returns true if the Base64 string decodes to under [maxBytes] bytes.
     * Use before submitting to Firestore to avoid rule rejections.
     *
     * Firestore rule for paymentScreenshotBase64: max 700 000 chars (≈ 500 KB).
     */
    fun isWithinSizeLimit(base64: String, maxBytes: Int = MAX_PAYMENT_SCREENSHOT_BYTES): Boolean {
        if (base64.isBlank()) return true
        return runCatching {
            Base64.decode(base64, Base64.NO_WRAP).size <= maxBytes
        }.getOrDefault(false)
    }

    /**
     * Returns the decoded byte size of a Base64 string, or -1 on error.
     */
    fun decodedSizeBytes(base64: String): Int {
        if (base64.isBlank()) return 0
        return runCatching {
            Base64.decode(base64, Base64.NO_WRAP).size
        }.getOrDefault(-1)
    }

    /**
     * Returns a human-readable size string for the decoded image.
     * E.g. "342 KB" or "1.2 MB"
     */
    fun decodedSizeLabel(base64: String): String {
        val bytes = decodedSizeBytes(base64).toLong()
        return when {
            bytes < 0       -> "unknown"
            bytes < 1024    -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else            -> "${"%.1f".format(bytes / (1024.0 * 1024.0))} MB"
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxWidth && h <= maxHeight) return bitmap

        val scale = minOf(maxWidth.toFloat() / w, maxHeight.toFloat() / h)
        return Bitmap.createScaledBitmap(
            bitmap,
            (w * scale).toInt(),
            (h * scale).toInt(),
            true
        )
    }
}
