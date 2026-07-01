package com.nexttoppers.feed.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nexttoppers.feed.MainActivity

// ── Notification channel IDs ───────────────────────────────────────────────────
object NtfChannels {
    const val GENERAL      = "ntf_general"
    const val MESSAGES     = "ntf_messages"
    const val ANNOUNCEMENTS = "ntf_announcements"
    const val RESOURCES    = "ntf_resources"
    const val PREMIUM      = "ntf_premium"
}

class NtfMessagingService : FirebaseMessagingService() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("ntf_fcm", Context.MODE_PRIVATE)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = Firebase.auth.currentUser?.uid ?: return
        Firebase.firestore.collection("users").document(uid)
            .update("fcmToken", token)
            .addOnFailureListener { e ->
                Log.w("NtfFCM", "Failed to save FCM token: ${e.message}")
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data    = message.data
        val notifId = data["notifId"] ?: message.messageId ?: ""

        // ── Deduplication: skip if already shown ──────────────────────────────
        if (notifId.isNotEmpty() && isAlreadyShown(notifId)) {
            Log.d("NtfFCM", "Duplicate notification skipped: $notifId")
            return
        }

        val title      = message.notification?.title ?: data["title"] ?: return
        val body       = message.notification?.body  ?: data["body"]  ?: data["message"] ?: return
        val imageUrl   = message.notification?.imageUrl?.toString() ?: data["imageUrl"] ?: ""
        val route      = data["route"]  ?: data["deepLink"] ?: ""
        val type       = data["type"]   ?: "SYSTEM"
        val channelId  = resolveChannel(type)

        ensureChannelsExist()
        showNotification(title, body, imageUrl, route, type, channelId)

        if (notifId.isNotEmpty()) markAsShown(notifId)
    }

    private fun resolveChannel(type: String): String = when {
        type.contains("MESSAGE") || type.contains("MENTION") -> NtfChannels.MESSAGES
        type.contains("ANNOUNCEMENT") -> NtfChannels.ANNOUNCEMENTS
        type.contains("LECTURE") || type.contains("PDF") || type.contains("NOTES")
                || type.contains("RESOURCE") -> NtfChannels.RESOURCES
        type.contains("PREMIUM") -> NtfChannels.PREMIUM
        else -> NtfChannels.GENERAL
    }

    private fun ensureChannelsExist() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            Triple(NtfChannels.GENERAL,       "General",               NotificationManager.IMPORTANCE_DEFAULT),
            Triple(NtfChannels.MESSAGES,      "Messages",              NotificationManager.IMPORTANCE_HIGH),
            Triple(NtfChannels.ANNOUNCEMENTS, "Announcements",         NotificationManager.IMPORTANCE_HIGH),
            Triple(NtfChannels.RESOURCES,     "New Resources",         NotificationManager.IMPORTANCE_DEFAULT),
            Triple(NtfChannels.PREMIUM,       "Premium Notifications", NotificationManager.IMPORTANCE_HIGH)
        ).forEach { (id, name, importance) ->
            if (manager.getNotificationChannel(id) == null) {
                val channel = NotificationChannel(id, name, importance).apply {
                    description = "Next Toppers Feed – $name"
                    enableVibration(true)
                    setShowBadge(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        imageUrl: String,
        route: String,
        type: String,
        channelId: String
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ── Build deep-link intent ─────────────────────────────────────────────
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (route.isNotEmpty()) putExtra(MainActivity.EXTRA_NOTIFICATION_ROUTE, route)
        }
        val pendingFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(this, route.hashCode(), launchIntent, pendingFlags)

        // ── Group key for notification grouping ───────────────────────────────
        val groupKey = "ntf_group_$channelId"

        // ── Large icon (app logo bitmap) ──────────────────────────────────────
        val largeIcon = runCatching {
            BitmapFactory.decodeResource(resources, android.R.drawable.ic_dialog_info)
        }.getOrNull()

        // ── Priority mapping ──────────────────────────────────────────────────
        val priority = when (channelId) {
            NtfChannels.MESSAGES, NtfChannels.ANNOUNCEMENTS, NtfChannels.PREMIUM ->
                NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .apply { if (largeIcon != null) setLargeIcon(largeIcon) }
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setGroup(groupKey)
            .setNumber(1)
            .build()

        // Use a stable ID per type so notifications of same type replace each other
        val notifId = resolveNotificationId(type)
        manager.notify(notifId, notification)
    }

    private fun resolveNotificationId(type: String): Int = when {
        type.contains("MESSAGE") || type.contains("MENTION") -> 1001
        type.contains("ANNOUNCEMENT") -> 1002
        type.contains("LECTURE")      -> 1003
        type.contains("PDF")          -> 1004
        type.contains("NOTES")        -> 1005
        type.contains("PREMIUM")      -> 1006
        type.contains("TEST")         -> 1007
        else -> System.currentTimeMillis().toInt()
    }

    // ── Dedup helpers (keep last 100 IDs in prefs) ────────────────────────────
    private fun isAlreadyShown(id: String): Boolean {
        val shown = prefs.getStringSet(SHOWN_IDS_KEY, emptySet()) ?: emptySet()
        return shown.contains(id)
    }

    private fun markAsShown(id: String) {
        val shown = prefs.getStringSet(SHOWN_IDS_KEY, emptySet())?.toMutableSet() ?: mutableSetOf()
        shown.add(id)
        if (shown.size > 100) {
            val trimmed = shown.toList().takeLast(100).toMutableSet()
            prefs.edit().putStringSet(SHOWN_IDS_KEY, trimmed).apply()
        } else {
            prefs.edit().putStringSet(SHOWN_IDS_KEY, shown).apply()
        }
    }

    companion object {
        private const val SHOWN_IDS_KEY = "shown_notification_ids"
    }
}
