package com.nexttoppers.feed.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.ResourceFolder
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val foldersCol = firestore.collection("lecture_folders")

    /**
     * Fetch all folders for the given subject from Firestore.
     *
     * Strategy:
     *  1. Try a single `whereIn` across all casing variants (uppercase, lowercase, title-case).
     *     This covers Firestore documents stored as "MATHS", "maths", or "Maths" in one round-trip.
     *  2. If that still returns nothing (Firestore index missing, collection empty, or subject stored
     *     under an entirely different spelling), fall back to fetching ALL folders and filtering
     *     client-side — avoids showing type-based tiles when real folders do exist.
     */
    suspend fun getFoldersForSubject(subject: String): List<ResourceFolder> {
        val upper = subject.uppercase()
        val lower = subject.lowercase()
        // Lowercase-then-capitalize so "MATHS" → "Maths" (not "MATHS")
        val title = lower.replaceFirstChar { it.uppercaseChar() }
        // Build a deduplicated list of at most 10 variants (Firestore whereIn limit)
        val variants = listOf(upper, lower, title).distinct().take(10)

        return try {
            val snap = foldersCol
                .whereIn("subject", variants)
                .get().await()

            if (!snap.isEmpty) {
                snap.documents.mapNotNull { mapFolder(it) }
            } else {
                // Firestore index may be missing or subject is stored differently —
                // fetch all folders and match client-side against all known variants.
                val allSnap = foldersCol.limit(500).get().await()
                val variantSet = variants.map { it.lowercase() }.toSet()
                allSnap.documents
                    .mapNotNull { mapFolder(it) }
                    .filter { it.subject.lowercase() in variantSet }
            }
        } catch (e: Exception) {
            // Index may not exist yet — fall back to full scan + client filter
            try {
                val allSnap = foldersCol.limit(500).get().await()
                val variantSet = variants.map { it.lowercase() }.toSet()
                allSnap.documents
                    .mapNotNull { mapFolder(it) }
                    .filter { it.subject.lowercase() in variantSet }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    /** Fetch ALL folders regardless of subject (used when subject is stored differently) */
    suspend fun getAllFolders(): List<ResourceFolder> {
        return try {
            val snap = foldersCol.limit(500).get().await()
            snap.documents.mapNotNull { mapFolder(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun mapFolder(doc: DocumentSnapshot): ResourceFolder? {
        val data = doc.data ?: return null
        return try {
            ResourceFolder(
                id          = doc.id,
                name        = data["name"] as? String
                              ?: data["title"] as? String
                              ?: data["folderName"] as? String ?: "",
                subject     = (data["subject"] as? String ?: "").uppercase(),
                parentId    = data["parentId"] as? String
                              ?: data["parent"] as? String
                              ?: data["parentFolderId"] as? String ?: "",
                order       = ((data["order"] as? Long) ?: 0L).toInt(),
                description = data["description"] as? String ?: "",
                iconEmoji   = data["emoji"] as? String ?: data["icon"] as? String ?: "📁"
            )
        } catch (e: Exception) { null }
    }
}
