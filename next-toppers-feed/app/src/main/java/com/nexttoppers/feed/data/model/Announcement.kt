package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Announcement(

    val id: String = "",

    val title: String = "",

    // F08: website writes "content" not "message" — field name must match exactly
    val content: String = "",

    val type: String = "",

    // Fixed null crash issue
    val imageUrl: String? = null,

    val externalUrl: String = "",

    val priority: Int = 0,

    // Legacy documents may store this as a String; @get:Exclude @field:Exclude prevents
    // toObject() from crashing. Callers must resolve via snapshot.resolveTimestamp("createdAt").
    @get:Exclude @field:Exclude
    val createdAt: Timestamp = Timestamp.now(),

    // Legacy app-only fields kept for backward read compat
    val important: Boolean = false,

    val pinned: Boolean = false,

    val targetAudience: String = "all",

    val author: String = "Admin"

) {

    val isUrgent: Boolean
        get() = important || pinned || priority >= 10
}
