package com.nexttoppers.feed.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service.
 * Full notification handling (channels, deep-links, foreground alerts)
 * will be implemented in a future prompt.
 */
class NtfMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO (Prompt N): persist token to Firestore users/{uid}/fcmToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO (Prompt N): display notification with NotificationManager
    }
}
