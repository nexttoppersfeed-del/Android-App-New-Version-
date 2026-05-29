package com.nexttoppers.feed.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.nexttoppers.feed.data.model.DownloadedResource
import com.nexttoppers.feed.data.model.RecentlyOpened
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val DOWNLOADS_KEY =
        stringSetPreferencesKey("ntf_downloads_v1")

    private val RECENTS_KEY =
        stringSetPreferencesKey("ntf_recents_v1")

    // ─────────────────────────────────────────────────────────────────────────
    // Downloads
    // ─────────────────────────────────────────────────────────────────────────

    val downloads: Flow<List<DownloadedResource>> =
        dataStore.data.map { prefs ->

            prefs[DOWNLOADS_KEY]
                ?.mapNotNull {
                    DownloadedResource.decode(it)
                }
                ?.sortedByDescending {
                    it.downloadedAt
                }
                ?: emptyList()
        }

    suspend fun saveDownload(
        resource: DownloadedResource
    ) {

        dataStore.edit { prefs ->

            val current =
                (prefs[DOWNLOADS_KEY] ?: emptySet())
                    .toMutableSet()

            // Remove duplicate
            current.removeIf {
                DownloadedResource.decode(it)?.id == resource.id
            }

            current.add(resource.encode())

            prefs[DOWNLOADS_KEY] = current
        }
    }

    suspend fun removeDownload(
        resourceId: String
    ) {

        val localPath =
            getLocalPath(resourceId)

        dataStore.edit { prefs ->

            val updated =
                (prefs[DOWNLOADS_KEY] ?: emptySet())
                    .filter {
                        DownloadedResource.decode(it)?.id != resourceId
                    }
                    .toSet()

            prefs[DOWNLOADS_KEY] = updated
        }

        // Delete local file
        if (!localPath.isNullOrEmpty()) {

            runCatching {
                File(localPath).delete()
            }
        }
    }

    fun isDownloaded(
        resourceId: String
    ): Flow<Boolean> {

        return downloads.map { list ->

            list.any {
                it.id == resourceId
            }
        }
    }

    suspend fun getLocalPath(
        resourceId: String
    ): String? {

        return dataStore.data
            .map { prefs ->

                prefs[DOWNLOADS_KEY]
                    ?.mapNotNull {
                        DownloadedResource.decode(it)
                    }
                    ?.firstOrNull {
                        it.id == resourceId
                    }
                    ?.localPath
            }
            .first()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Recently Opened
    // ─────────────────────────────────────────────────────────────────────────

    val recents: Flow<List<RecentlyOpened>> =
        dataStore.data.map { prefs ->

            prefs[RECENTS_KEY]
                ?.mapNotNull {
                    RecentlyOpened.decode(it)
                }
                ?.sortedByDescending {
                    it.openedAt
                }
                ?.take(20)
                ?: emptyList()
        }

    suspend fun recordOpen(
        item: RecentlyOpened
    ) {

        dataStore.edit { prefs ->

            val current =
                (prefs[RECENTS_KEY] ?: emptySet())
                    .toMutableSet()

            // Remove existing duplicate
            current.removeIf {
                RecentlyOpened.decode(it)?.resourceId == item.resourceId
            }

            current.add(item.encode())

            val trimmed =
                current.mapNotNull {
                    RecentlyOpened.decode(it)
                }
                    .sortedByDescending {
                        it.openedAt
                    }
                    .take(20)
                    .map {
                        it.encode()
                    }
                    .toSet()

            prefs[RECENTS_KEY] = trimmed
        }
    }

    suspend fun clearRecents() {

        dataStore.edit { prefs ->
            prefs.remove(RECENTS_KEY)
        }
    }

    suspend fun clearDownloads() {

        dataStore.edit { prefs ->
            prefs.remove(DOWNLOADS_KEY)
        }
    }
}
