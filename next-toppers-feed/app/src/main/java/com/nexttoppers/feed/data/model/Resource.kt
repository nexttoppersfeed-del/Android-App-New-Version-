package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class ResourceSubject(val displayName: String, val emoji: String) {
    MATHS("Maths", "📐"),
    SCIENCE("Science", "🔬"),
    SST("SST", "🌍"),
    ENGLISH("English", "📖"),
    HINDI("Hindi", "🇮🇳"),
    PREMIUM("Premium", "⭐")
}

enum class ResourceType(val displayName: String, val emoji: String) {
    NOTES("Notes", "📝"),
    PDF("PDF", "📄"),
    MODULE("Module", "📦"),
    DPP("DPP", "📋"),
    LECTURE("Lecture", "🎬"),
    PRACTICE("Practice", "✏️")
}

data class Resource(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val type: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val fileUrl: String = "",
    val premium: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val views: Long = 0L,
    val uploadedBy: String = "Admin",
    val duration: String = "",       // for lectures, e.g. "45 min"
    val pageCount: Int = 0,          // for PDFs/notes
    val tags: List<String> = emptyList()
) {
    fun subjectEnum(): ResourceSubject? =
        ResourceSubject.values().firstOrNull { it.name.equals(subject, ignoreCase = true) }

    fun typeEnum(): ResourceType? =
        ResourceType.values().firstOrNull { it.name.equals(type, ignoreCase = true) }

    fun isLecture() = type.equals(ResourceType.LECTURE.name, ignoreCase = true)
}
