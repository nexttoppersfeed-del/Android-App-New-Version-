package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.NtfTest
import com.nexttoppers.feed.data.model.TestAttempt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val testsCol    = firestore.collection("tests")
    // F09: website uses top-level "testAttempts" collection
    private val attemptsCol = firestore.collection("testAttempts")

    // ── Document mappers ───────────────────────────────────────────────────────

    private fun mapTest(doc: DocumentSnapshot): NtfTest? {
        val data = doc.data ?: return null
        return try {
            NtfTest(
                id            = doc.id,
                title         = data["title"] as? String ?: "",
                subject       = (data["subject"] as? String ?: "").uppercase(),
                description   = data["description"] as? String ?: "",
                questionCount = ((data["questionCount"]
                                 ?: data["totalQuestions"]
                                 ?: (data["questions"] as? List<*>)?.size?.toLong()
                                 ?: 0L) as? Long)?.toInt() ?: 0,
                // website field is "duration" (minutes)
                timeLimit     = ((data["timeLimit"]
                                 ?: data["duration"] ?: 30L) as? Long)?.toInt() ?: 30,
                // F06: website uses "isPremium" — check it first, fall back to "premium"
                isPremium     = data["isPremium"] as? Boolean
                                ?: data["premium"] as? Boolean ?: false,
                active        = data["active"] as? Boolean ?: true,
                createdAt     = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                category      = data["category"] as? String ?: "",
                difficulty    = data["difficulty"] as? String ?: "MEDIUM",
                totalMarks    = ((data["totalMarks"]
                                 ?: data["maxScore"] ?: 0L) as? Long)?.toInt() ?: 0,
                passMarks     = ((data["passMarks"] ?: 0L) as? Long)?.toInt() ?: 0,
                tags          = (data["tags"] as? List<*>)
                                    ?.filterIsInstance<String>() ?: emptyList()
            )
        } catch (e: Exception) { null }
    }

    private fun mapAttempt(doc: DocumentSnapshot): TestAttempt? {
        val data = doc.data ?: return null
        return try {
            // F09: map all website testAttempts fields
            TestAttempt(
                id              = doc.id,
                userId          = data["userId"] as? String ?: "",
                testId          = data["testId"] as? String ?: "",
                score           = ((data["score"] ?: 0L) as? Long)?.toInt() ?: 0,
                totalMarks      = ((data["totalMarks"]
                                   ?: data["maxScore"] ?: 0L) as? Long)?.toInt() ?: 0,
                totalQuestions  = ((data["totalQuestions"] ?: 0L) as? Long)?.toInt() ?: 0,
                correctAnswers  = ((data["correctAnswers"] ?: 0L) as? Long)?.toInt() ?: 0,
                wrongAnswers    = ((data["wrongAnswers"] ?: 0L) as? Long)?.toInt() ?: 0,
                skipped         = ((data["skipped"] ?: 0L) as? Long)?.toInt() ?: 0,
                timeTaken       = ((data["timeTaken"]
                                   ?: data["timeTakenSeconds"] ?: 0L) as? Long)?.toInt() ?: 0,
                answers         = (data["answers"] as? Map<*, *>)
                                      ?.mapKeys   { it.key as? String ?: "" }
                                      ?.mapValues { it.value?.toString() ?: "" }
                                  ?: emptyMap(),
                completedAt     = data["completedAt"] as? Timestamp ?: Timestamp.now()
            )
        } catch (e: Exception) { null }
    }

    // ── Queries ────────────────────────────────────────────────────────────────

    fun observeAllTests(limit: Long = 50): Flow<Result<List<NtfTest>>> = callbackFlow {
        val listener = testsCol
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val tests = snap?.documents?.mapNotNull { mapTest(it) } ?: emptyList()
                trySend(Result.success(tests))
            }
        awaitClose { listener.remove() }
    }

    fun observeBySubject(subject: String): Flow<Result<List<NtfTest>>> = callbackFlow {
        val listener = testsCol
            .whereEqualTo("subject", subject.uppercase())
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val tests = snap?.documents?.mapNotNull { mapTest(it) } ?: emptyList()
                trySend(Result.success(tests))
            }
        awaitClose { listener.remove() }
    }

    fun observeUserAttempts(userId: String): Flow<Result<List<TestAttempt>>> = callbackFlow {
        val listener = attemptsCol
            .whereEqualTo("userId", userId)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val attempts = snap?.documents?.mapNotNull { mapAttempt(it) } ?: emptyList()
                trySend(Result.success(attempts))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getTestById(testId: String): Result<NtfTest> = runCatching {
        val snap = testsCol.document(testId).get().await()
        mapTest(snap) ?: throw Exception("Test not found")
    }

    // F22: read questions array from the test document (not a subcollection)
    suspend fun getQuestionsForTest(testId: String): Result<List<Map<String, Any>>> = runCatching {
        val snap = testsCol.document(testId).get().await()
        val data = snap.data ?: throw Exception("Test not found")
        @Suppress("UNCHECKED_CAST")
        (data["questions"] as? List<Map<String, Any>>) ?: emptyList()
    }

    suspend fun submitAttempt(attempt: TestAttempt): Result<String> = runCatching {
        val ref = attemptsCol.add(attempt.toMap()).await()
        ref.id
    }

    suspend fun getAttemptForTest(userId: String, testId: String): TestAttempt? {
        return try {
            val snap = attemptsCol
                .whereEqualTo("userId", userId)
                .whereEqualTo("testId", testId)
                .limit(1).get().await()
            snap.documents.firstOrNull()?.let { mapAttempt(it) }
        } catch (_: Exception) { null }
    }

    suspend fun getRecentTests(limit: Long = 6): Result<List<NtfTest>> = runCatching {
        val snap = testsCol
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit).get().await()
        snap.documents.mapNotNull { mapTest(it) }
    }
}
