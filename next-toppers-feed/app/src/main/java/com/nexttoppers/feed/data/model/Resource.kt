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
    val duration: String = "",
    val pageCount: Int = 0,
    val tags: List<String> = emptyList(),
    val folderId: String = "",
    val youtubeId: String = ""
) {
    fun subjectEnum(): ResourceSubject? =
        ResourceSubject.values().firstOrNull { it.name.equals(subject, ignoreCase = true) }

    fun typeEnum(): ResourceType? =
        ResourceType.values().firstOrNull { it.name.equals(type, ignoreCase = true) }

    fun isLecture() = type.equals(ResourceType.LECTURE.name, ignoreCase = true)

    fun isYouTube() = fileUrl.contains("youtu.be") || fileUrl.contains("youtube.com")

    fun isHls() = fileUrl.endsWith(".m3u8") || fileUrl.contains(".m3u8?")

    fun hasPlayableUrl() = fileUrl.isNotEmpty()

    fun hasDownloadableUrl() = fileUrl.isNotEmpty() && !isYouTube() && !isLecture()
}
