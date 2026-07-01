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
     * Returns empty list (silently) if the collection doesn't exist or has no data.
     */
    suspend fun getFoldersForSubject(subject: String): List<ResourceFolder> {
        return try {
            val snap = foldersCol
                .whereEqualTo("subject", subject.uppercase())
                .get().await()
            if (snap.isEmpty) {
                // Also try lowercase / title-case variants
                val snap2 = foldersCol
                    .whereEqualTo("subject", subject.replaceFirstChar { it.uppercaseChar() })
                    .get().await()
                snap2.documents.mapNotNull { mapFolder(it) }
            } else {
                snap.documents.mapNotNull { mapFolder(it) }
            }
        } catch (e: Exception) {
            // Index may not exist yet, or collection is empty — fall back gracefully
            emptyList()
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
