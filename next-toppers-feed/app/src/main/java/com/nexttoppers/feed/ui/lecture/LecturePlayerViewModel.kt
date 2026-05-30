@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.nexttoppers.feed.ui.lecture

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen

    private val _showSpeedMenu = MutableStateFlow(false)
    val showSpeedMenu: StateFlow<Boolean> = _showSpeedMenu

    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

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

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        _player.playbackParameters = PlaybackParameters(speed)
        _showSpeedMenu.value = false
    }

    fun toggleFullscreen() {
        _isFullscreen.value = !_isFullscreen.value
    }

    fun setFullscreen(value: Boolean) {
        _isFullscreen.value = value
    }

    fun toggleSpeedMenu() {
        _showSpeedMenu.value = !_showSpeedMenu.value
    }

    fun dismissSpeedMenu() {
        _showSpeedMenu.value = false
    }

    override fun onCleared() {
        super.onCleared()
        _player.release()
    }
}
