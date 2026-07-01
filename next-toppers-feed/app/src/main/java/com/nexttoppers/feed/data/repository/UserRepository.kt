package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.util.AppLogger
import com.nexttoppers.feed.util.resolveLastActive
import com.nexttoppers.feed.util.resolveTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection = firestore.collection("users")
    // F01: admin role source of truth per Firebase Architecture doc
    private val adminsCollection = firestore.collection("admins")

    fun observeUser(uid: String): Flow<Result<User>> = callbackFlow {

        val listener = usersCollection.document(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                    ?.copy(
                        lastActive = snapshot.resolveLastActive(),
                        lastSeen   = snapshot.resolveTimestamp("lastSeen"),
                        createdAt  = snapshot.resolveTimestamp("createdAt"),
                        updatedAt  = snapshot.resolveTimestamp("updatedAt")
                    )
                if (user != null) {
                    trySend(Result.success(user))
                }
            }

        awaitClose { listener.remove() }
    }

    // F01/F02: After creating/fetching user, resolve role from /admins/{uid}
    suspend fun getOrCreateUser(firebaseUser: FirebaseUser): Result<User> {

        return try {

            val docRef = usersCollection.document(firebaseUser.uid)
            val snapshot = docRef.get().await()

            // Resolve role from /admins/{uid} — this is the website's source of truth
            val role = resolveRole(firebaseUser.uid)

            if (snapshot.exists()) {

                // Upsert role and lastSeen — same as website's setDoc merge: true pattern
                docRef.update(mapOf(
                    "lastSeen" to Timestamp.now(),
                    "role"     to role,
                    "isOnline" to true
                )).await()

                val base = snapshot.toObject(User::class.java)
                    ?.copy(
                        lastActive = snapshot.resolveLastActive(),
                        lastSeen   = snapshot.resolveTimestamp("lastSeen"),
                        createdAt  = snapshot.resolveTimestamp("createdAt"),
                        updatedAt  = snapshot.resolveTimestamp("updatedAt")
                    )
                    ?: createUserFromFirebase(firebaseUser, role)
                Result.success(base.copy(role = role))

            } else {

                val newUser = createUserFromFirebase(firebaseUser, role)
                // F14: website uses setDoc merge:true; we set() with merge for upsert
                docRef.set(newUser.toMap(), SetOptions.merge()).await()
                Result.success(newUser)
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(uid: String): Result<User> {

        return try {

            val snapshot = usersCollection.document(uid).get().await()

            if (!snapshot.exists()) {
                AppLogger.w("UserRepository", "User not found for uid=$uid")
                return Result.failure(Exception("User not found: $uid"))
            }

            val data = snapshot.data ?: run {
                AppLogger.w("UserRepository", "Empty document for uid=$uid")
                return Result.failure(Exception("Empty user document: $uid"))
            }

            val name = listOf("name", "displayName", "username")
                .firstNotNullOfOrNull { key ->
                    (data[key] as? String)?.takeIf { it.isNotBlank() }
                } ?: ""

            // F07: website writes "photoURL" (capital URL) — check that first
            val photoUrl = listOf("photoURL", "photoUrl", "avatar", "profilePicture", "photo")
                .firstNotNullOfOrNull { key ->
                    (data[key] as? String)?.takeIf { it.isNotBlank() }
                } ?: ""

            if (name.isBlank()) {
                AppLogger.w(
                    "UserRepository",
                    "Resolved blank name for uid=$uid (data keys: ${data.keys})"
                )
            }

            val base = snapshot.toObject(User::class.java)
                ?.copy(
                    lastActive = snapshot.resolveLastActive(),
                    lastSeen   = snapshot.resolveTimestamp("lastSeen"),
                    createdAt  = snapshot.resolveTimestamp("createdAt"),
                    updatedAt  = snapshot.resolveTimestamp("updatedAt")
                )
                ?: User(uid = uid)
            val user = base.copy(uid = uid, name = name, photoURL = photoUrl)

            Result.success(user)

        } catch (e: Exception) {

            AppLogger.error("UserRepository", "getUser failed for uid=$uid: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateLastSeen(uid: String) {
        try {
            usersCollection.document(uid)
                .update(mapOf(
                    "lastSeen" to Timestamp.now(),
                    "isOnline" to false
                ))
                .await()
        } catch (_: Exception) {
        }
    }

    // F01: Checks /admins/{uid} and returns the role string matching website's schema
    private suspend fun resolveRole(uid: String): String {
        return try {
            val snap = adminsCollection.document(uid).get().await()
            if (!snap.exists()) return "student"
            when (snap.getString("role")) {
                "owner" -> "owner"
                "admin" -> "admin"
                else    -> "student"
            }
        } catch (_: Exception) {
            "student"
        }
    }

    private fun createUserFromFirebase(firebaseUser: FirebaseUser, role: String = "student"): User {

        // F21: website stores lastActive as date string
        val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        return User(
            uid          = firebaseUser.uid,
            name         = firebaseUser.displayName ?: "",
            email        = firebaseUser.email ?: "",
            // F07: use correct field name matching website
            photoURL     = firebaseUser.photoUrl?.toString() ?: "",
            role         = role,
            xp           = 0L,
            streak       = 0,
            level        = 1,
            totalQuizzes = 0,
            lecturesWatched = 0,
            pdfsRead     = 0,
            isPremium    = false,
            createdAt    = Timestamp.now(),
            lastSeen     = Timestamp.now(),
            lastActive   = todayString
        )
    }
}
