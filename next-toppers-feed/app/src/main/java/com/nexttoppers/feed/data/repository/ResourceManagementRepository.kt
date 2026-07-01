package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.util.resolveTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResourceManagementRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val resourcesCol = firestore.collection("resources")

    // ── Observe all resources (admin) ────────────────────────────────────────────

    fun observeAllResources(limit: Long = 50): Flow<Result<List<Resource>>> = callbackFlow {
        val query = resourcesCol.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val resources = snap?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Resource::class.java)
                        ?.copy(id = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(resources))
        }
        awaitClose { listener.remove() }
    }

    fun observeResourcesBySubject(subject: String): Flow<Result<List<Resource>>> = callbackFlow {
        val query = resourcesCol
            .whereEqualTo("subject", subject)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val resources = snap?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Resource::class.java)
                        ?.copy(id = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(resources))
        }
        awaitClose { listener.remove() }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────────

    suspend fun createResource(
        title: String,
        description: String,
        subject: String,
        type: String,
        fileUrl: String,
        thumbnailUrl: String = "",
        premium: Boolean = false,
        tags: List<String> = emptyList()
    ): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: "admin"
        val id = UUID.randomUUID().toString()
        val resource = Resource(
            id           = id,
            title        = title.trim(),
            description  = description.trim(),
            subject      = subject,
            type         = type,
            fileUrl      = fileUrl.trim(),
            thumbnailUrl = thumbnailUrl.trim(),
            premium      = premium,
            uploadedBy   = uid,
            createdAt    = Timestamp.now(),
            tags         = tags
        )
        resourcesCol.document(id).set(resourceToMap(resource)).await()
        id
    }

    suspend fun updateResource(
        id: String,
        title: String,
        description: String,
        subject: String,
        type: String,
        fileUrl: String,
        premium: Boolean,
        tags: List<String> = emptyList()
    ): Result<Unit> = runCatching {
        resourcesCol.document(id).update(mapOf(
            "title"       to title.trim(),
            "description" to description.trim(),
            "subject"     to subject,
            "type"        to type,
            "fileUrl"     to fileUrl.trim(),
            // F06: write "isPremium" to match website files/lectures schema
            "isPremium"   to premium,
            "tags"        to tags
        )).await()
    }

    suspend fun deleteResource(id: String): Result<Unit> = runCatching {
        resourcesCol.document(id).delete().await()
    }

    suspend fun togglePremium(id: String, currentPremium: Boolean): Result<Unit> = runCatching {
        // F06: write "isPremium" to match website files/lectures schema
        resourcesCol.document(id).update("isPremium", !currentPremium).await()
    }

    suspend fun getResourceById(id: String): Result<Resource> = runCatching {
        val snap = resourcesCol.document(id).get().await()
        snap.toObject(Resource::class.java)
            ?.copy(id = snap.id, createdAt = snap.resolveTimestamp("createdAt"))
            ?: throw Exception("Resource not found")
    }

    // ── Search ────────────────────────────────────────────────────────────────────

    suspend fun searchResources(query: String): Result<List<Resource>> = runCatching {
        val snap = resourcesCol
            .whereGreaterThanOrEqualTo("title", query)
            .whereLessThanOrEqualTo("title", query + "\uf8ff")
            .limit(20).get().await()
        snap.documents.mapNotNull { doc ->
            try {
                doc.toObject(Resource::class.java)
                    ?.copy(id = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
            } catch (e: Exception) { null }
        }
    }

    private fun resourceToMap(r: Resource): Map<String, Any?> = mapOf(
        "id"           to r.id,
        "title"        to r.title,
        "description"  to r.description,
        "subject"      to r.subject,
        "type"         to r.type,
        "fileUrl"      to r.fileUrl,
        "thumbnailUrl" to r.thumbnailUrl,
        // F06: write "isPremium" to match website files/lectures schema
        "isPremium"    to r.premium,
        "uploadedBy"   to r.uploadedBy,
        "createdAt"    to r.createdAt,
        "views"        to r.views,
        "tags"         to r.tags,
        "duration"     to r.duration,
        "pageCount"    to r.pageCount
    )
}
