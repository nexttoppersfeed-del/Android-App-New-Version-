package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection =
        firestore.collection("users")

    fun observeUser(
        uid: String
    ): Flow<Result<User>> = callbackFlow {

        val listener =
            usersCollection.document(uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        trySend(
                            Result.failure(error)
                        )

                        return@addSnapshotListener
                    }

                    val user =
                        snapshot?.toObject(User::class.java)

                    if (user != null) {

                        trySend(
                            Result.success(user)
                        )
                    }
                }

        awaitClose {
            listener.remove()
        }
    }

    suspend fun getOrCreateUser(
        firebaseUser: FirebaseUser
    ): Result<User> {

        return try {

            val docRef =
                usersCollection.document(firebaseUser.uid)

            val snapshot =
                docRef.get().await()

            if (snapshot.exists()) {

                docRef.update(
                    "lastSeen",
                    Timestamp.now()
                ).await()

                val user =
                    snapshot.toObject(User::class.java)
                        ?: createUserFromFirebase(firebaseUser)

                Result.success(user)

            } else {

                val newUser =
                    createUserFromFirebase(firebaseUser)

                docRef.set(
                    newUser.toMap()
                ).await()

                Result.success(newUser)
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getUser(
        uid: String
    ): Result<User> {

        return try {

            val snapshot =
                usersCollection.document(uid)
                    .get()
                    .await()

            val user =
                snapshot.toObject(User::class.java)
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updateLastSeen(
        uid: String
    ) {

        try {

            usersCollection.document(uid)
                .update(
                    "lastSeen",
                    Timestamp.now()
                )
                .await()

        } catch (_: Exception) {
        }
    }

    private fun createUserFromFirebase(
        firebaseUser: FirebaseUser
    ): User {

        return User(

            uid = firebaseUser.uid,

            name =
                firebaseUser.displayName ?: "",

            email =
                firebaseUser.email ?: "",

            photoUrl =
                firebaseUser.photoUrl?.toString() ?: "",

            xp = 0L,

            streak = 0,

            level = 1,

            quizzesCompleted = 0,

            resourcesOpened = 0,

            isPremium = false,

            premiumType = "free",

            premiumStart = null,

            premiumEnd = null,

            premiumActive = false,

            membershipBadge = "",

            joinedAt = Timestamp.now(),

            lastSeen = Timestamp.now(),

            lastActive = null,

            isAdmin = false,

            banned = false
        )
    }
}
