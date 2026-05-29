package com.nexttoppers.feed.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Question
import com.nexttoppers.feed.data.model.Quiz
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("quizzes")

    // ── All quizzes realtime ──────────────────────────────────────────────────
    fun observeAll(): Flow<Result<List<Quiz>>> = callbackFlow {
        val listener = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Quiz::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── By subject ───────────────────────────────────────────────────────────
    fun observeBySubject(subject: String): Flow<Result<List<Quiz>>> = callbackFlow {
        val listener = col
            .whereEqualTo("subject", subject.uppercase())
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Quiz::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Featured (highest XP reward) ─────────────────────────────────────────
    suspend fun getFeatured(limit: Long = 5): Result<List<Quiz>> = runCatching {
        val snap = col
            .orderBy("xpReward", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { it.toObject(Quiz::class.java)?.copy(id = it.id) }
    }

    // ── Recent (newest first) ─────────────────────────────────────────────────
    suspend fun getRecent(limit: Long = 10): Result<List<Quiz>> = runCatching {
        val snap = col
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { it.toObject(Quiz::class.java)?.copy(id = it.id) }
    }

    // ── Single quiz ──────────────────────────────────────────────────────────
    suspend fun getById(id: String): Result<Quiz> = runCatching {
        val doc = col.document(id).get().await()
        doc.toObject(Quiz::class.java)?.copy(id = doc.id)
            ?: error("Quiz $id not found")
    }

    // ── Questions subcollection ───────────────────────────────────────────────
    suspend fun getQuestions(quizId: String): Result<List<Question>> = runCatching {
        val snap = col.document(quizId)
            .collection("questions")
            .orderBy("__name__")   // preserve insertion order
            .get().await()
        snap.documents.mapNotNull { it.toObject(Question::class.java)?.copy(id = it.id) }
    }

    // ── Stats ────────────────────────────────────────────────────────────────
    suspend fun incrementAttempts(quizId: String) {
        runCatching {
            col.document(quizId).update("attempts", FieldValue.increment(1)).await()
        }
    }
}
