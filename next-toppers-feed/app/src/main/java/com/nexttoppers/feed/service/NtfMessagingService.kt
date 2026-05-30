package com.nexttoppers.feed.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NtfMessagingService : FirebaseMessagingService() {

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
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body  = message.notification?.body  ?: message.data["body"]  ?: return
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val manager   = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "ntf_general"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "General Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Next Toppers Feed notifications"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        val launchIntent = packageManager
            .getLaunchIntentForPackage(packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingFlags  = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = if (launchIntent != null)
            PendingIntent.getActivity(this, 0, launchIntent, pendingFlags)
        else null

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply { if (pendingIntent != null) setContentIntent(pendingIntent) }
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
