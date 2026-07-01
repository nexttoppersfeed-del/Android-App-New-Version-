@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.nexttoppers.feed.ui.lecture

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class LecturePlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoUrl: String = URLDecoder.decode(savedStateHandle["url"]   ?: "", "UTF-8")
    val title: String    = URLDecoder.decode(savedStateHandle["title"] ?: "Lecture", "UTF-8")

    private val _player: ExoPlayer = ExoPlayer.Builder(context).build()
    val player: ExoPlayer get() = _player

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _isFullscreen = MutableStateFlow(false)
    val isFullscreen: StateFlow<Boolean> = _isFullscreen

    private val _showSpeedMenu = MutableStateFlow(false)
    val showSpeedMenu: StateFlow<Boolean> = _showSpeedMenu

    /** True when ExoPlayer is buffering (STATE_BUFFERING) */
    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering

    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)

    companion object {
        private val POSITIONS_KEY = stringSetPreferencesKey("lecture_positions_v1")
        private const val POSITION_SEPARATOR = "||"
        private const val MAX_SAVED_POSITIONS = 100
    }

    init {
        setupPlayerListener()
        loadMediaAndRestorePosition()
        startPositionAutoSave()
    }

    private fun setupPlayerListener() {
        _player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
            }
        })
    }

    private fun loadMediaAndRestorePosition() {
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

        // Restore saved position
        viewModelScope.launch {
            val savedMs = getSavedPosition(videoUrl)
            if (savedMs > 3_000L) {  // Only resume if > 3 seconds in
                _player.seekTo(savedMs)
            }
        }
    }

    /** Save position every 5 seconds while playing */
    private fun startPositionAutoSave() {
        viewModelScope.launch {
            while (true) {
                delay(5_000L)
                val pos = _player.currentPosition
                if (pos > 0 && _player.playbackState != Player.STATE_IDLE) {
                    savePosition(videoUrl, pos)
                }
            }
        }
    }

    private suspend fun getSavedPosition(url: String): Long {
        return dataStore.data
            .map { prefs ->
                prefs[POSITIONS_KEY]
                    ?.firstOrNull { it.startsWith("$url$POSITION_SEPARATOR") }
                    ?.substringAfterLast(POSITION_SEPARATOR)
                    ?.toLongOrNull() ?: 0L
            }
            .first()
    }

    private fun savePosition(url: String, positionMs: Long) {
        if (url.isBlank()) return
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = (prefs[POSITIONS_KEY] ?: emptySet()).toMutableSet()
                // Remove old entry for this URL
                current.removeIf { it.startsWith("$url$POSITION_SEPARATOR") }
                current.add("$url$POSITION_SEPARATOR$positionMs")
                // Trim to max
                if (current.size > MAX_SAVED_POSITIONS) {
                    val oldest = current.minByOrNull {
                        it.substringAfterLast(POSITION_SEPARATOR).toLongOrNull() ?: 0L
                    }
                    oldest?.let { current.remove(it) }
                }
                prefs[POSITIONS_KEY] = current
            }
        }
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
        // Save final position before releasing
        val finalPos = _player.currentPosition
        if (finalPos > 0 && videoUrl.isNotBlank()) {
            savePosition(videoUrl, finalPos)
        }
        _player.release()
    }
}
