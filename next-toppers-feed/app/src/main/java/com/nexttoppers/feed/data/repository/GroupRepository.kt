package com.nexttoppers.feed.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.Group
import com.nexttoppers.feed.data.model.defaultGroups
import com.nexttoppers.feed.util.resolveTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val groupsCol = firestore.collection("groups")

    fun observeAllGroups(): Flow<Result<List<Group>>> = callbackFlow {
        val listener = groupsCol.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val groups = snap?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Group::class.java)
                        ?.copy(groupId = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
            } ?: emptyList()

            if (groups.isEmpty()) {
                trySend(Result.success(defaultGroups))
            } else {
                trySend(Result.success(groups))
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeGroup(groupId: String): Flow<Result<Group>> = callbackFlow {
        val listener = groupsCol.document(groupId).addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val group = snap?.toObject(Group::class.java)
                ?.copy(groupId = snap.id, createdAt = snap.resolveTimestamp("createdAt"))
                ?: defaultGroups.find { it.groupId == groupId }
            if (group != null) trySend(Result.success(group))
            else trySend(Result.failure(Exception("Group not found")))
        }
        awaitClose { listener.remove() }
    }

    fun observeUserGroups(uid: String): Flow<Result<List<Group>>> = callbackFlow {
        val query = groupsCol.whereArrayContains("members", uid)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val groups = snap?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Group::class.java)
                        ?.copy(groupId = doc.id, createdAt = doc.resolveTimestamp("createdAt"))
                } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(groups))
        }
        awaitClose { listener.remove() }
    }

    suspend fun joinGroup(groupId: String, uid: String): Result<Unit> = runCatching {
        ensureGroupExists(groupId)
        groupsCol.document(groupId).update("members", FieldValue.arrayUnion(uid)).await()
    }

    suspend fun leaveGroup(groupId: String, uid: String): Result<Unit> = runCatching {
        groupsCol.document(groupId).update("members", FieldValue.arrayRemove(uid)).await()
    }

    suspend fun createGroup(group: Group): Result<String> = runCatching {
        val id = group.groupId.ifBlank { java.util.UUID.randomUUID().toString() }
        groupsCol.document(id).set(group.copy(groupId = id).toMap()).await()
        id
    }

    private suspend fun ensureGroupExists(groupId: String) {
        val snap = groupsCol.document(groupId).get().await()
        if (!snap.exists()) {
            val default = defaultGroups.find { it.groupId == groupId }
            if (default != null) {
                groupsCol.document(groupId).set(default.toMap()).await()
            }
        }
    }
}
