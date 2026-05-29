package com.nexttoppers.feed

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.nexttoppers.feed.util.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NextToppersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        configureFirestoreOfflinePersistence()
        AppLogger.init(BuildConfig.IS_DEBUG)
        AppLogger.i("NextToppersApp", "App started — v${BuildConfig.APP_VERSION}")
    }

    private fun configureFirestoreOfflinePersistence() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        // 100 MB offline cache — generous for educational content
                        .setSizeBytes(100L * 1024 * 1024)
                        .build()
                )
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
            AppLogger.d("NextToppersApp", "Firestore offline persistence enabled (100 MB)")
        } catch (e: Exception) {
            // Already configured (can happen if Firestore was used before settings were applied)
            AppLogger.w("NextToppersApp", "Firestore settings already set: ${e.message}")
        }
    }
}
