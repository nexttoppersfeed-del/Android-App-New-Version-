package com.nexttoppers.feed.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Reads "lastActive" from a Firestore document, handling both data formats
 * transparently so the app works without any Firestore data migration:
 *
 *  - Legacy documents (written before the website schema change) store a Timestamp.
 *  - Current documents (app + website) store a date string "yyyy-MM-dd".
 *
 * Always use this function instead of getString("lastActive") or relying on
 * toObject() for this field. The User data class annotates lastActive with
 * @get:Exclude @field:Exclude so Firestore's reflective mapper skips it;
 * callers must set it manually via .copy(lastActive = snapshot.resolveLastActive()).
 *
 * Note: SimpleDateFormat is created per call (not shared) because it is not
 * thread-safe; repository calls can arrive concurrently on different coroutine
 * dispatchers.
 */
fun DocumentSnapshot.resolveLastActive(): String {
    return when (val raw = get("lastActive")) {
        is String    -> raw
        is Timestamp -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(raw.toDate())
        else         -> ""
    }
}
