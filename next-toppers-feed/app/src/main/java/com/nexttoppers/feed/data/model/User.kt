package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class User(

    val uid: String = "",

    val name: String = "",

    val email: String = "",

    // F07: website writes "photoURL" (capital URL) — must match exactly
    val photoURL: String = "",

    // F01/F02: role comes from /admins/{uid}.role — "owner"/"admin"/"student"
    val role: String = "student",

    val isOnline: Boolean = false,

    // Legacy documents may contain a String instead of a Timestamp.
    // @get:Exclude @field:Exclude prevents toObject() from touching this field;
    // callers must populate it via .copy(lastSeen = snapshot.resolveTimestamp("lastSeen")).
    @get:Exclude @field:Exclude
    val lastSeen: Timestamp = Timestamp.now(),

    // F14: website writes "createdAt" (was "joinedAt")
    // Same dual-annotation guard as lastSeen — resolved manually after toObject().
    @get:Exclude @field:Exclude
    val createdAt: Timestamp = Timestamp.now(),

    val xp: Long = 0L,

    val level: Int = 1,

    val streak: Int = 0,

    // F21: website stores lastActive as a date string e.g. "2025-06-30"
    // Legacy Firestore documents may contain a Timestamp here instead of a String.
    // Both @get:Exclude and @field:Exclude are required: @get:Exclude covers
    // getter-based bean discovery; @field:Exclude covers direct field reflection.
    // Together they guarantee toObject() never touches this field regardless of
    // which reflection path the Firestore SDK takes.
    // Callers must populate it manually via .copy(lastActive = snapshot.resolveLastActive()).
    @get:Exclude @field:Exclude
    val lastActive: String = "",

    // F16: website uses "totalQuizzes" (was "quizzesCompleted")
    val totalQuizzes: Int = 0,

    // F17: missing stat fields now present
    val totalCorrect: Int = 0,

    val totalScore: Int = 0,

    val perfectScores: Int = 0,

    val avgScore: Float = 0f,

    // F15: split from single "resourcesOpened" into two distinct fields
    val lecturesWatched: Int = 0,

    val pdfsRead: Int = 0,

    val quizzesBySubject: Map<String, Int> = emptyMap(),

    // F18: achievements array, unlocked achievement IDs
    val achievements: List<String> = emptyList(),

    // isPremium is mirrored from premiumUsers collection by the website
    val isPremium: Boolean = false,

    // Same dual-annotation guard — resolved manually after toObject().
    @get:Exclude @field:Exclude
    val updatedAt: Timestamp = Timestamp.now(),

    // kept for app-only moderation feature
    val banned: Boolean = false

) {

    // F02: isAdmin and isOwner are computed — never stored as fields
    val isAdmin: Boolean get() = role == "admin" || role == "owner"
    val isOwner: Boolean get() = role == "owner"

    fun toMap(): Map<String, Any?> = mapOf(

        "uid"             to uid,
        "name"            to name,
        "email"           to email,
        "photoURL"        to photoURL,
        "role"            to role,
        "isOnline"        to isOnline,
        "lastSeen"        to lastSeen,
        "createdAt"       to createdAt,
        "xp"              to xp,
        "level"           to level,
        "streak"          to streak,
        "lastActive"      to lastActive,
        "totalQuizzes"    to totalQuizzes,
        "totalCorrect"    to totalCorrect,
        "totalScore"      to totalScore,
        "perfectScores"   to perfectScores,
        "avgScore"        to avgScore,
        "lecturesWatched" to lecturesWatched,
        "pdfsRead"        to pdfsRead,
        "quizzesBySubject" to quizzesBySubject,
        "achievements"    to achievements,
        "isPremium"       to isPremium,
        "updatedAt"       to updatedAt
    )
}
