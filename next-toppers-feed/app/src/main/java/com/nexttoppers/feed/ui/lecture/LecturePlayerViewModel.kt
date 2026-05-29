@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.nexttoppers.feed.ui.lecture

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class LecturePlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoUrl: String =
        URLDecoder.decode(savedStateHandle["url"] ?: "", "UTF-8")
    val title: String =
        URLDecoder.decode(savedStateHandle["title"] ?: "Lecture", "UTF-8")

    private val _player: ExoPlayer = ExoPlayer.Builder(context).build()
    val player: ExoPlayer get() = _player

    init {
        loadMedia()
    }

    private fun loadMedia() {
        if (videoUrl.isBlank()) return
        val mediaItem = when {
            videoUrl.contains(".m3u8") -> MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .build()
            else -> MediaItem.fromUri(videoUrl)
        }
        _player.setMediaItem(mediaItem)
        _player.prepare()
        _player.playWhenReady = true
    }

    override fun onCleared() {
        super.onCleared()
        _player.release()
    }
}
