package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class Announcement(

    val id: String = "",

    val title: String = "",

    val message: String = "",

    // Fixed null crash issue
    val imageUrl: String? = null,

    val createdAt: Timestamp =
        Timestamp.now(),

    // Legacy field
    val important: Boolean = false,

    val pinned: Boolean = false,

    val priority: Int = 0,

    val targetAudience: String = "all",

    val author: String = "Admin"
) {

    val isUrgent: Boolean

        get() =
            important ||
            pinned ||
            priority >= 10
}
