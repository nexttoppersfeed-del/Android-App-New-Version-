package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourcesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val filesCol    = firestore.collection("files")
    private val lecturesCol = firestore.collection("lectures")

    // ── Subject alias expansion ────────────────────────────────────────────────
    private fun subjectVariants(subject: String): List<String> {
        val upper = subject.uppercase()
        val lower = subject.lowercase()
        val title = subject.replaceFirstChar { it.uppercaseChar() }
        val base  = listOf(upper, lower, title)
        val extra = when (upper) {
            "MATHS"   -> listOf("Mathematics", "MATHEMATICS", "mathematics", "Math", "MATH")
            "SST"     -> listOf(
                "Social Studies", "SOCIAL STUDIES", "social studies",
                "Social Science", "SOCIAL SCIENCE", "SS"
            )
            "SCIENCE" -> listOf("Sciences", "Phy+Chem+Bio")
            "ENGLISH" -> listOf("English Language", "Eng")
            "HINDI"   -> listOf("Hindi Language")
            else      -> emptyList()
        }
        return (base + extra).distinct().take(10)
    }

    // ── Manual document mappers ────────────────────────────────────────────────

    private fun mapFile(doc: DocumentSnapshot): Resource? {
        val data = doc.data ?: return null
        return try {
            val rawTitle = data["title"] as? String ?: data["name"] as? String ?: ""
            val rawType  = (data["type"] as? String ?: ResourceType.NOTES.name).uppercase()
            Resource(
                id           = doc.id,
                title        = rawTitle,
                subject      = (data["subject"] as? String ?: "").uppercase(),
                type         = rawType,
                description  = data["description"] as? String ?: "",
                thumbnailUrl = data["thumbnailUrl"] as? String
                               ?: data["thumbnail"] as? String ?: "",
                fileUrl      = data["url"] as? String
                               ?: data["downloadUrl"] as? String
                               ?: data["fileUrl"] as? String
                               ?: data["resourceUrl"] as? String
                               ?: data["pdfUrl"] as? String
                               ?: data["pdf_url"] as? String
                               ?: data["file_url"] as? String
                               ?: data["storageUrl"] as? String
                               ?: data["link"] as? String
                               ?: data["filePath"] as? String ?: "",
                // F06: website uses "isPremium" — check it first, fall back to "premium"
                premium      = data["isPremium"] as? Boolean
                               ?: data["premium"] as? Boolean ?: false,
                createdAt    = data["createdAt"] as? Timestamp
                               ?: data["timestamp"] as? Timestamp ?: Timestamp.now(),
                views        = (data["views"] as? Long) ?: 0L,
                uploadedBy   = data["uploadedBy"] as? String
                               ?: data["createdBy"] as? String ?: "Admin",
                pageCount    = ((data["pageCount"] ?: 0L) as? Long)?.toInt() ?: 0,
                tags         = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                folderId     = data["folderId"] as? String
                               ?: data["parentFolderId"] as? String ?: ""
            )
        } catch (e: Exception) { null }
    }

    private fun mapLecture(doc: DocumentSnapshot): Resource? {
        val data = doc.data ?: return null
        return try {
            val rawTitle  = data["title"] as? String ?: data["name"] as? String ?: ""
            val youtubeId = data["youtubeId"] as? String
                            ?: data["youtube_id"] as? String ?: ""
            // website field is "videoUrl"
            val videoUrl  = data["videoUrl"] as? String
                            ?: data["url"] as? String
                            ?: data["fileUrl"] as? String
                            ?: data["hlsUrl"] as? String ?: ""
            val thumbUrl  = data["thumbnail"] as? String
                            ?: data["thumbnailUrl"] as? String
                            ?: if (youtubeId.isNotEmpty())
                                   "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
                               else ""
            Resource(
                id           = doc.id,
                title        = rawTitle,
                subject      = (data["subject"] as? String ?: "").uppercase(),
                type         = ResourceType.LECTURE.name,
                description  = data["description"] as? String ?: "",
                thumbnailUrl = thumbUrl,
                fileUrl      = when {
                    youtubeId.isNotEmpty() -> "https://youtu.be/$youtubeId"
                    videoUrl.isNotEmpty()  -> videoUrl
                    else                   -> ""
                },
                // F06: website uses "isPremium" — check it first, fall back to "premium"
                premium      = data["isPremium"] as? Boolean
                               ?: data["premium"] as? Boolean ?: false,
                createdAt    = data["createdAt"] as? Timestamp
                               ?: data["timestamp"] as? Timestamp ?: Timestamp.now(),
                views        = (data["views"] as? Long) ?: 0L,
                uploadedBy   = data["uploadedBy"] as? String
                               ?: data["createdBy"] as? String ?: "Admin",
                duration     = data["duration"]?.toString() ?: "",
                tags         = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                youtubeId    = youtubeId,
                folderId     = data["folderId"] as? String ?: ""
            )
        } catch (e: Exception) { null }
    }

    // ── Realtime by subject — merges files + lectures ─────────────────────────

    fun observeBySubject(subject: String): Flow<Result<List<Resource>>> = callbackFlow {
        val variants = subjectVariants(subject)

        var filesItems: List<Resource>   = emptyList()
        var lectureItems: List<Resource> = emptyList()

        fun mergeAndSend() {
            val merged = (filesItems + lectureItems)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val filesListener = filesCol
            .whereIn("subject", variants)
            .limit(80)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                filesItems = snap?.documents?.mapNotNull { mapFile(it) } ?: emptyList()
                mergeAndSend()
            }

        val lecturesListener = lecturesCol
            .whereIn("subject", variants)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                lectureItems = snap?.documents?.mapNotNull { mapLecture(it) } ?: emptyList()
                mergeAndSend()
            }

        awaitClose {
            filesListener.remove()
            lecturesListener.remove()
        }
    }

    // ── All resources ──────────────────────────────────────────────────────────

    fun observeAll(): Flow<Result<List<Resource>>> = callbackFlow {
        var filesItems: List<Resource>   = emptyList()
        var lectureItems: List<Resource> = emptyList()

        fun mergeAndSend() {
            val merged = (filesItems + lectureItems)
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val filesListener = filesCol
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(80)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                filesItems = snap?.documents?.mapNotNull { mapFile(it) } ?: emptyList()
                mergeAndSend()
            }

        val lecturesListener = lecturesCol
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                lectureItems = snap?.documents?.mapNotNull { mapLecture(it) } ?: emptyList()
                mergeAndSend()
            }

        awaitClose {
            filesListener.remove()
            lecturesListener.remove()
        }
    }

    // ── By type (within subject) ───────────────────────────────────────────────

    suspend fun getBySubjectAndType(subject: String, type: String): Result<List<Resource>> =
        runCatching {
            val variants  = subjectVariants(subject)
            val typeUpper = type.uppercase()
            if (typeUpper == ResourceType.LECTURE.name) {
                val snap = lecturesCol
                    .whereIn("subject", variants)
                    .limit(50).get().await()
                snap.documents.mapNotNull { mapLecture(it) }
            } else {
                val snap = filesCol
                    .whereIn("subject", variants)
                    .limit(80).get().await()
                snap.documents.mapNotNull { mapFile(it) }
                    .filter { it.type.equals(typeUpper, ignoreCase = true) }
            }
        }

    // ── Single resource ────────────────────────────────────────────────────────

    suspend fun getById(id: String): Result<Resource> = runCatching {
        val fileSnap = filesCol.document(id).get().await()
        if (fileSnap.exists()) return Result.success(
            mapFile(fileSnap) ?: throw Exception("Failed to map file")
        )
        val lectureSnap = lecturesCol.document(id).get().await()
        if (lectureSnap.exists()) return Result.success(
            mapLecture(lectureSnap) ?: throw Exception("Failed to map lecture")
        )
        throw Exception("Resource not found")
    }

    // ── Increment view count ───────────────────────────────────────────────────

    suspend fun incrementViews(id: String) {
        try {
            filesCol.document(id).update("views", FieldValue.increment(1)).await()
        } catch (_: Exception) {
            try {
                lecturesCol.document(id).update("views", FieldValue.increment(1)).await()
            } catch (_: Exception) { }
        }
    }

    // ── Recent across all subjects ─────────────────────────────────────────────

    suspend fun getRecent(limit: Long = 10): Result<List<Resource>> = runCatching {
        val filesSnap = filesCol
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit).get().await()
        val lectureSnap = lecturesCol
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit / 2 + 1).get().await()

        (filesSnap.documents.mapNotNull { mapFile(it) }
                + lectureSnap.documents.mapNotNull { mapLecture(it) })
            .sortedByDescending { it.createdAt.seconds }
            .take(limit.toInt())
    }

    // ── By folder ID ──────────────────────────────────────────────────────────

    suspend fun getByFolderId(folderId: String): Result<List<Resource>> = runCatching {
        val filesSnap    = filesCol.whereEqualTo("folderId", folderId).limit(100).get().await()
        val lecturesSnap = lecturesCol.whereEqualTo("folderId", folderId).limit(50).get().await()
        val files    = filesSnap.documents.mapNotNull { mapFile(it) }
        val lectures = lecturesSnap.documents.mapNotNull { mapLecture(it) }
        (files + lectures)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt.seconds }
    }

    /** Resources whose folderId is blank/empty — appear at root of subject */
    suspend fun getRootResourcesBySubject(subject: String): Result<List<Resource>> = runCatching {
        val variants = subjectVariants(subject)
        val filesSnap = filesCol.whereIn("subject", variants).limit(80).get().await()
        val lecturesSnap = lecturesCol.whereIn("subject", variants).limit(50).get().await()
        val all = (filesSnap.documents.mapNotNull { mapFile(it) } +
                   lecturesSnap.documents.mapNotNull { mapLecture(it) })
            .distinctBy { it.id }
        all.filter { it.folderId.isBlank() }
           .sortedByDescending { it.createdAt.seconds }
    }

    // ── Count per subject ──────────────────────────────────────────────────────

    suspend fun getCountBySubject(): Map<String, Int> {
        return try {
            val filesSnap   = filesCol.get().await()
            val lectureSnap = lecturesCol.get().await()
            val all = filesSnap.documents.mapNotNull { mapFile(it) } +
                      lectureSnap.documents.mapNotNull { mapLecture(it) }
            all.groupBy { it.subject.uppercase() }.mapValues { it.value.size }
        } catch (_: Exception) { emptyMap() }
    }
}
