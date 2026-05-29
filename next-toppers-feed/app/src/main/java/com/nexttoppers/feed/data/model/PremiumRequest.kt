package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class PremiumRequestStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

/**
 * Represents a user's premium membership payment request.
 *
 * Payment proof is stored as a Base64-encoded image string directly in Firestore.
 * Firebase Storage is NOT used — this project uses Firestore + external URLs only.
 *
 * Field: [paymentScreenshotBase64]
 *   - Compressed JPEG/PNG encoded as Base64 string
 *   - Max ~500 KB decoded (≈ 700 000 Base64 chars — enforced by Firestore rules)
 *   - Use [com.nexttoppers.feed.util.Base64ImageUtils] to encode before submitting
 */
data class PremiumRequest(
    val requestId: String = "",
    val userId: String = "",
    val username: String = "",
    val userEmail: String = "",
    val plan: String = "",
    val amount: String = "",
    val utrId: String = "",
    /** Base64-encoded payment screenshot. Empty string if not provided. */
    val paymentScreenshotBase64: String = "",
    val status: String = PremiumRequestStatus.PENDING.name,
    val createdAt: Timestamp = Timestamp.now(),
    val reviewedAt: Timestamp? = null,
    val reviewedBy: String = "",
    val adminNote: String = ""
) {
    val statusEnum: PremiumRequestStatus
        get() = PremiumRequestStatus.values()
            .firstOrNull { it.name == status } ?: PremiumRequestStatus.PENDING

    fun toMap(): Map<String, Any?> = mapOf(
        "requestId"                to requestId,
        "userId"                   to userId,
        "username"                 to username,
        "userEmail"                to userEmail,
        "plan"                     to plan,
        "amount"                   to amount,
        "utrId"                    to utrId,
        "paymentScreenshotBase64"  to paymentScreenshotBase64,
        "status"                   to status,
        "createdAt"                to createdAt,
        "reviewedAt"               to reviewedAt,
        "reviewedBy"               to reviewedBy,
        "adminNote"                to adminNote
    )
}
