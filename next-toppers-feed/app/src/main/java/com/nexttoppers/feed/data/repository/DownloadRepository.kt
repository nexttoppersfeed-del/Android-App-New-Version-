package com.nexttoppers.feed.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.DownloadStatus
import com.nexttoppers.feed.data.model.DownloadedResource
import com.nexttoppers.feed.data.model.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val offlineRepository: OfflineRepository
) {

    private val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * Start downloading a resource
     */
    fun startDownload(resource: Resource): Long {

        val subjectDir = resource.subject
            .lowercase()
            .replaceFirstChar { it.uppercase() }

        val safeTitle = resource.title
            .replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            .take(80)

        val fileName = "$safeTitle.pdf"

        val subDir = "Downloads/$subjectDir"

        val destinationDir =
            File(context.getExternalFilesDir(null), subDir)

        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        val request = DownloadManager.Request(
            Uri.parse(resource.fileUrl)
        )
            .setTitle(resource.title)
            .setDescription("Downloading for offline reading...")
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            .setDestinationInExternalFilesDir(
                context,
                null,
                "$subDir/$fileName"
            )
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)

        return downloadManager.enqueue(request)
    }

    /**
     * Observe download progress
     */
    fun observeProgress(
        downloadId: Long,
        resource: Resource
    ): Flow<DownloadStatus> = flow {

        emit(DownloadStatus.Queued)

        var active = true

        while (active) {

            val query = DownloadManager.Query()
                .setFilterById(downloadId)

            val cursor = downloadManager.query(query)

            if (!cursor.moveToFirst()) {
                cursor.close()
                break
            }

            val status = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_STATUS
                )
            )

            val downloaded = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                )
            )

            val total = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                )
            )

            val localUri = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_LOCAL_URI
                )
            )

            val reason = cursor.getInt(
                cursor.getColumnIndexOrThrow(
                    DownloadManager.COLUMN_REASON
                )
            )

            cursor.close()

            when (status) {

                DownloadManager.STATUS_PENDING -> {
                    emit(DownloadStatus.Queued)
                }

                DownloadManager.STATUS_RUNNING -> {

                    val progress =
                        if (total > 0) {
                            ((downloaded * 100) / total).toInt()
                        } else {
                            0
                        }

                    emit(
                        DownloadStatus.Progress(progress)
                    )
                }

                DownloadManager.STATUS_PAUSED -> {
                    emit(DownloadStatus.Paused)
                }

                DownloadManager.STATUS_SUCCESSFUL -> {

                    val localPath =
                        localUri?.let {
                            Uri.parse(it).path
                        } ?: ""

                    val sizeBytes =
                        if (localPath.isNotEmpty()) {
                            File(localPath).length()
                        } else {
                            total
                        }

                    offlineRepository.saveDownload(
                        DownloadedResource(
                            id = resource.id,
                            title = resource.title,
                            subject = resource.subject,
                            type = resource.type,
                            localPath = localPath,
                            sizeBytes = sizeBytes,
                            premium = resource.premium
                        )
                    )

                    incrementFirestoreDownloads(
                        resource.id
                    )

                    emit(
                        DownloadStatus.Completed(localPath)
                    )

                    active = false
                }

                DownloadManager.STATUS_FAILED -> {

                    emit(
                        DownloadStatus.Failed(
                            "Error code $reason"
                        )
                    )

                    active = false
                }
            }

            if (active) {
                delay(500L)
            }
        }
    }

    /**
     * Cancel running download
     */
    fun cancelDownload(downloadId: Long) {
        downloadManager.remove(downloadId)
    }

    /**
     * Increment Firestore downloads count
     */
    private suspend fun incrementFirestoreDownloads(
        resourceId: String
    ) {

        runCatching {

            firestore.collection("resources")
                .document(resourceId)
                .update(
                    "downloads",
                    FieldValue.increment(1)
                )
                .await()
        }
    }

    /**
     * Check if a download is active
     */
    fun isActiveDownload(
        downloadId: Long
    ): Boolean {

        val cursor = downloadManager.query(
            DownloadManager.Query()
                .setFilterById(downloadId)
        )

        val exists = cursor.moveToFirst()

        cursor.close()

        return exists
    }

    /**
     * Count active downloads
     */
    fun activeDownloadCount(): Int {

        val cursor = downloadManager.query(
            DownloadManager.Query()
                .setFilterByStatus(
                    DownloadManager.STATUS_RUNNING
                )
        )

        val count = cursor.count

        cursor.close()

        return count
    }
}
