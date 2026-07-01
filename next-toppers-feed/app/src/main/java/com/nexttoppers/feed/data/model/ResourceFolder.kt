package com.nexttoppers.feed.data.model

/**
 * Represents a folder from the `lecture_folders` Firestore collection.
 * Mirrors the website's folder hierarchy structure.
 */
data class ResourceFolder(
    val id: String = "",
    val name: String = "",
    val subject: String = "",
    val parentId: String = "",   // empty string = root folder
    val order: Int = 0,
    val description: String = "",
    val iconEmoji: String = "📁"
) {
    val isRoot: Boolean get() = parentId.isBlank()
}

/**
 * A display item for the folder grid — wraps either a real Firestore folder
 * or a virtual type-based folder (fallback when no real folders exist).
 */
data class FolderDisplayItem(
    val id: String,
    val name: String,
    val emoji: String,
    val fileCount: Int,
    val lectureCount: Int,
    val childFolderCount: Int = 0,
    val isRealFolder: Boolean,
    val typeFolder: ResourceType? = null   // only set for virtual type-folders
) {
    val totalCount: Int get() = fileCount + lectureCount
}

/** A breadcrumb navigation step */
data class BreadcrumbItem(
    val id: String?,   // null = subject root
    val name: String
)
