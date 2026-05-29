package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    // files — study materials (notes, PDFs, modules, DPPs)
    private val filesCol    = firestore.collection("files")
    // lectures — YouTube video lectures
    private val lecturesCol = firestore.collection("lectures")

    // ── Manual document mappers ────────────────────────────────────────────────

    private fun mapFile(doc: DocumentSnapshot): Resource? {
        val data = doc.data ?: return null
        return try {
            Resource(
                id           = doc.id,
                title        = data["title"] as? String ?: "",
                subject      = (data["subject"] as? String ?: "").uppercase(),
                type         = (data["type"] as? String ?: ResourceType.NOTES.name).uppercase(),
                description  = data["description"] as? String ?: "",
                thumbnailUrl = data["thumbnailUrl"] as? String
                               ?: data["thumbnail"] as? String ?: "",
                fileUrl      = data["downloadUrl"] as? String
                               ?: data["url"] as? String
                               ?: data["fileUrl"] as? String ?: "",
                premium      = data["premium"] as? Boolean ?: false,
                createdAt    = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                views        = data["views"] as? Long ?: 0L,
                uploadedBy   = data["uploadedBy"] as? String
                               ?: data["createdBy"] as? String ?: "Admin",
                pageCount    = ((data["pageCount"] ?: 0L) as? Long)?.toInt() ?: 0,
                tags         = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) { null }
    }

    private fun mapLecture(doc: DocumentSnapshot): Resource? {
        val data = doc.data ?: return null
        return try {
            val youtubeId = data["youtubeId"] as? String
                            ?: data["youtube_id"] as? String ?: ""
            val videoUrl  = data["videoUrl"] as? String
                            ?: data["url"] as? String ?: ""
            val thumbUrl  = data["thumbnail"] as? String
                            ?: data["thumbnailUrl"] as? String
                            ?: if (youtubeId.isNotEmpty())
                                   "https://img.youtube.com/vi/$youtubeId/mqdefault.jpg"
                               else ""
            Resource(
                id           = doc.id,
                title        = data["title"] as? String ?: "",
                subject      = (data["subject"] as? String ?: "").uppercase(),
                type         = ResourceType.LECTURE.name,
                description  = data["description"] as? String ?: "",
                thumbnailUrl = thumbUrl,
                fileUrl      = when {
                    youtubeId.isNotEmpty() -> "https://youtu.be/$youtubeId"
                    videoUrl.isNotEmpty()  -> videoUrl
                    else                   -> ""
                },
                premium      = data["premium"] as? Boolean ?: false,
                createdAt    = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                views        = data["views"] as? Long ?: 0L,
                uploadedBy   = data["uploadedBy"] as? String
                               ?: data["createdBy"] as? String ?: "Admin",
                duration     = data["duration"] as? String ?: "",
                tags         = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) { null }
    }

    // ── Realtime by subject — merges files + lectures ─────────────────────────

    fun observeBySubject(subject: String): Flow<Result<List<Resource>>> = callbackFlow {
        val subjectUpper = subject.uppercase()
        val subjectVariants = listOf(
            subjectUpper,
            subject.lowercase(),
            subject.replaceFirstChar { it.uppercaseChar() }
        ).distinct()
        var filesItems: List<Resource>   = emptyList()
        var lectureItems: List<Resource> = emptyList()

        fun mergeAndSend() {
            val merged = (filesItems + lectureItems)
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val filesListener = filesCol
            .whereIn("subject", subjectVariants)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                filesItems = snap?.documents?.mapNotNull { mapFile(it) } ?: emptyList()
                mergeAndSend()
            }

        val lecturesListener = lecturesCol
            .whereIn("subject", subjectVariants)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, _ ->
                lectureItems = snap?.documents?.mapNotNull { mapLecture(it) } ?: emptyList()
                mergeAndSend()
            }

        awaitClose {
            filesListener.remove()
            lecturesListener.remove()
        }
    }

    // ── All resources (for search & global feed) — merges files + lectures ────

    fun observeAll(): Flow<Result<List<Resource>>> = callbackFlow {
        var filesItems: List<Resource>   = emptyList()
        var lectureItems: List<Resource> = emptyList()

        fun mergeAndSend() {
            val merged = (filesItems + lectureItems)
                .sortedByDescending { it.createdAt.seconds }
            trySend(Result.success(merged))
        }

        val filesListener = filesCol
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(80)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                filesItems = snap?.documents?.mapNotNull { mapFile(it) } ?: emptyList()
                mergeAndSend()
            }

        val lecturesListener = lecturesCol
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(40)
            .addSnapshotListener { snap, _ ->
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
            val subjectUpper    = subject.uppercase()
            val typeUpper       = type.uppercase()
            val subjectVariants = listOf(
                subjectUpper,
                subject.lowercase(),
                subject.replaceFirstChar { it.uppercaseChar() }
            ).distinct()
            if (typeUpper == ResourceType.LECTURE.name) {
                val snap = lecturesCol
                    .whereIn("subject", subjectVariants)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(30).get().await()
                snap.documents.mapNotNull { mapLecture(it) }
            } else {
                val snap = filesCol
                    .whereIn("subject", subjectVariants)
                    .whereEqualTo("type", typeUpper)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50).get().await()
                snap.documents.mapNotNull { mapFile(it) }
            }
        }

    // ── Single resource — checks files first, then lectures ───────────────────

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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit).get().await()
        val lectureSnap = lecturesCol
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit / 2 + 1).get().await()

        (filesSnap.documents.mapNotNull { mapFile(it) }
                + lectureSnap.documents.mapNotNull { mapLecture(it) })
            .sortedByDescending { it.createdAt.seconds }
            .take(limit.toInt())
    }

    // ── Count per subject (for category cards) ────────────────────────────────

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
