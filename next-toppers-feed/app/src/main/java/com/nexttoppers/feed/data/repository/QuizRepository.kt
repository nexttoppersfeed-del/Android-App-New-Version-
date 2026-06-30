package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.Question
import com.nexttoppers.feed.data.model.Quiz
import com.nexttoppers.feed.data.model.mapRawQuestion
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
    // F04: website stores quizzes/tests in "tests" collection — not "quizzes"
    private val col = firestore.collection("tests")

    // ── Mapper: tests document → Quiz model ────────────────────────────────────
    private fun mapDoc(doc: DocumentSnapshot): Quiz? {
        val data = doc.data ?: return null
        return try {
            Quiz(
                id             = doc.id,
                title          = data["title"] as? String ?: "",
                subject        = (data["subject"] as? String ?: "").uppercase(),
                chapter        = data["category"] as? String ?: "",
                // website field "duration" is in minutes
                duration       = ((data["duration"] ?: data["timeLimit"] ?: 10L) as? Long)?.toInt() ?: 10,
                // F06: website uses "isPremium" not "premium"
                premium        = data["isPremium"] as? Boolean
                                 ?: data["premium"] as? Boolean ?: false,
                totalQuestions = ((data["totalQuestions"]
                                   ?: data["questionCount"]
                                   ?: (data["questions"] as? List<*>)?.size?.toLong()
                                   ?: 0L) as? Long)?.toInt() ?: 0,
                createdAt      = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                xpReward       = ((data["xpReward"] ?: 50L) as? Long)?.toInt() ?: 50,
                difficulty     = data["difficulty"] as? String ?: "MEDIUM",
                description    = data["description"] as? String ?: "",
                attempts       = (data["attempts"] as? Long) ?: 0L,
                tags           = (data["tags"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) { null }
    }

    // ── All quizzes realtime ──────────────────────────────────────────────────
    fun observeAll(): Flow<Result<List<Quiz>>> = callbackFlow {
        val listener = col
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { mapDoc(it) } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── By subject ───────────────────────────────────────────────────────────
    fun observeBySubject(subject: String): Flow<Result<List<Quiz>>> = callbackFlow {
        val listener = col
            .whereEqualTo("subject", subject.uppercase())
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { mapDoc(it) } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Featured (highest XP reward) ─────────────────────────────────────────
    suspend fun getFeatured(limit: Long = 5): Result<List<Quiz>> = runCatching {
        val snap = col
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { mapDoc(it) }
    }

    // ── Recent (newest first) ─────────────────────────────────────────────────
    suspend fun getRecent(limit: Long = 10): Result<List<Quiz>> = runCatching {
        val snap = col
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { mapDoc(it) }
    }

    // ── Single quiz ──────────────────────────────────────────────────────────
    suspend fun getById(id: String): Result<Quiz> = runCatching {
        val doc = col.document(id).get().await()
        mapDoc(doc) ?: error("Quiz/Test $id not found")
    }

    // ── F22: Questions come from the "questions" array IN the tests document ──
    // (website does NOT use a subcollection — questions are embedded in the doc)
    suspend fun getQuestions(quizId: String): Result<List<Question>> = runCatching {
        val doc  = col.document(quizId).get().await()
        val data = doc.data ?: error("Test $quizId not found")
        @Suppress("UNCHECKED_CAST")
        val rawList = data["questions"] as? List<Map<String, Any>> ?: emptyList()
        rawList.mapIndexed { idx, raw -> mapRawQuestion(raw, idx) }
    }

    // ── Stats ────────────────────────────────────────────────────────────────
    suspend fun incrementAttempts(quizId: String) {
        runCatching {
            col.document(quizId).update("attempts", FieldValue.increment(1)).await()
        }
    }
}
