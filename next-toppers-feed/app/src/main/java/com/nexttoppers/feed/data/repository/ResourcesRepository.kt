package com.nexttoppers.feed.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Resource
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
    private val col = firestore.collection("resources")

    // ── Realtime by subject ────────────────────────────────────────────────────
    fun observeBySubject(subject: String): Flow<Result<List<Resource>>> = callbackFlow {
        val listener = col
            .whereEqualTo("subject", subject.uppercase())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Resource::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── All resources (for search & global feed) ───────────────────────────────
    fun observeAll(): Flow<Result<List<Resource>>> = callbackFlow {
        val listener = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Resource::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── By type (within subject) ───────────────────────────────────────────────
    suspend fun getBySubjectAndType(subject: String, type: String): Result<List<Resource>> {
        return try {
            val snap = col
                .whereEqualTo("subject", subject.uppercase())
                .whereEqualTo("type", type.uppercase())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            val items = snap.documents.mapNotNull { doc ->
                doc.toObject(Resource::class.java)?.copy(id = doc.id)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Single resource ────────────────────────────────────────────────────────
    suspend fun getById(id: String): Result<Resource> {
        return try {
            val snap = col.document(id).get().await()
            val resource = snap.toObject(Resource::class.java)?.copy(id = snap.id)
                ?: return Result.failure(Exception("Resource not found"))
            Result.success(resource)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Increment view count ───────────────────────────────────────────────────
    suspend fun incrementViews(id: String) {
        try {
            col.document(id).update("views", com.google.firebase.firestore.FieldValue.increment(1)).await()
        } catch (_: Exception) { }
    }

    // ── Recent across all subjects ─────────────────────────────────────────────
    suspend fun getRecent(limit: Long = 10): Result<List<Resource>> {
        return try {
            val snap = col
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get().await()
            val items = snap.documents.mapNotNull { doc ->
                doc.toObject(Resource::class.java)?.copy(id = doc.id)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Count per subject (for category cards) ────────────────────────────────
    suspend fun getCountBySubject(): Map<String, Int> {
        return try {
            val snap = col.get().await()
            snap.documents
                .mapNotNull { it.toObject(Resource::class.java) }
                .groupBy { it.subject.uppercase() }
                .mapValues { it.value.size }
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
