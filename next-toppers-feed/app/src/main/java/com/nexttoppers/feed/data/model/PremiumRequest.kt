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
 * F11: field names now match the website's premiumRequests schema:
 *   - uid            (was userId)
 *   - userName       (was username)
 *   - price          (was amount: String) — stored as Double to match website's number type
 *   - transactionId  (was utrId)
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
    // F11: website uses "uid" — kept as uid in model; repository mapper reads both
    val uid: String = "",
    // F11: website uses "userName" (camelCase, capital N)
    val userName: String = "",
    val userEmail: String = "",
    val plan: String = "",
    // F11: website stores price as a number — use Double; 0.0 if unknown
    val price: Double = 0.0,
    // F11: website uses "transactionId" — was utrId
    val transactionId: String = "",
    /** Base64-encoded payment screenshot. Empty string if not provided. */
    val paymentScreenshotBase64: String = "",
    val status: String = PremiumRequestStatus.PENDING.name,
    val createdAt: Timestamp = Timestamp.now(),
    val reviewedAt: Timestamp? = null,
    val reviewedBy: String = "",
    val adminNote: String = "",
    // Additional website fields
    val phoneNumber: String = "",
    val paymentMethod: String = "UPI"
) {
    // Backward-compat properties so existing UI code that references old names still compiles
    val userId: String get() = uid
    val username: String get() = userName
    val amount: String get() = if (price > 0) price.toInt().toString() else ""
    val utrId: String get() = transactionId

    val statusEnum: PremiumRequestStatus
        get() = PremiumRequestStatus.values()
            .firstOrNull { it.name == status } ?: PremiumRequestStatus.PENDING

    // F11: write website-compatible field names
    fun toMap(): Map<String, Any?> = mapOf(
        "requestId"               to requestId,
        "uid"                     to uid,
        "userName"                to userName,
        "userEmail"               to userEmail,
        "plan"                    to plan,
        "price"                   to price,
        "transactionId"           to transactionId,
        "paymentScreenshotBase64" to paymentScreenshotBase64,
        "status"                  to status,
        "createdAt"               to createdAt,
        "reviewedAt"              to reviewedAt,
        "reviewedBy"              to reviewedBy,
        "adminNote"               to adminNote,
        "phoneNumber"             to phoneNumber,
        "paymentMethod"           to paymentMethod
    )
}
