package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Announcement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnouncementsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val collection = firestore.collection("announcements")

    fun observeAnnouncements(): Flow<Result<List<Announcement>>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { doc ->
                    val a = doc.toObject(Announcement::class.java) ?: Announcement()
                    a.copy(id = doc.id)
                } ?: emptyList()
                val sorted = items.sortedWith(
                    compareByDescending<Announcement> { it.important }
                        .thenByDescending { it.createdAt }
                )
                trySend(Result.success(sorted))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAnnouncementById(id: String): Result<Announcement?> = runCatching {
        val doc = collection.document(id).get().await()
        doc.toObject(Announcement::class.java)?.copy(id = doc.id)
    }

    suspend fun refreshAnnouncements(): Result<List<Announcement>> {
        return try {
            val snapshot = collection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .await()
            val items = snapshot.documents.map { doc ->
                val a = doc.toObject(Announcement::class.java) ?: Announcement()
                a.copy(id = doc.id)
            }
            val sorted = items.sortedWith(
                compareByDescending<Announcement> { it.important }
                    .thenByDescending { it.createdAt }
            )
            Result.success(sorted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Admin: create ─────────────────────────────────────────────────────────────

    suspend fun createAnnouncement(
        title: String,
        message: String,
        imageUrl: String = "",
        pinned: Boolean = false,
        important: Boolean = false,
        priority: Int = 0,
        targetAudience: String = "all"
    ): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: "admin"
        val id = UUID.randomUUID().toString()
        val announcement = mapOf(
            "id"             to id,
            "title"          to title.trim(),
            "message"        to message.trim(),
            "imageUrl"       to imageUrl.trim(),
            "pinned"         to pinned,
            "important"      to (important || pinned),
            "priority"       to priority,
            "targetAudience" to targetAudience,
            "author"         to uid,
            "createdAt"      to Timestamp.now()
        )
        collection.document(id).set(announcement).await()
        id
    }

    // ── Admin: update ─────────────────────────────────────────────────────────────

    suspend fun updateAnnouncement(
        id: String,
        title: String,
        message: String,
        imageUrl: String = "",
        pinned: Boolean = false,
        important: Boolean = false,
        priority: Int = 0
    ): Result<Unit> = runCatching {
        collection.document(id).update(mapOf(
            "title"     to title.trim(),
            "message"   to message.trim(),
            "imageUrl"  to imageUrl.trim(),
            "pinned"    to pinned,
            "important" to (important || pinned),
            "priority"  to priority
        )).await()
    }

    // ── Admin: delete ─────────────────────────────────────────────────────────────

    suspend fun deleteAnnouncement(id: String): Result<Unit> = runCatching {
        collection.document(id).delete().await()
    }

    // ── Admin: pin/unpin ──────────────────────────────────────────────────────────

    suspend fun setPinned(id: String, pinned: Boolean): Result<Unit> = runCatching {
        collection.document(id).update(mapOf(
            "pinned"    to pinned,
            "important" to pinned
        )).await()
    }

    // ── Admin: set urgent ─────────────────────────────────────────────────────────

    suspend fun setUrgent(id: String, urgent: Boolean): Result<Unit> = runCatching {
        collection.document(id).update(mapOf(
            "important" to urgent,
            "priority"  to if (urgent) 10 else 0
        )).await()
    }
}
