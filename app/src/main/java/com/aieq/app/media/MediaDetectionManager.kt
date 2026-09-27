package com.aieq.app.media

import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.aieq.app.domain.model.TrackMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaDetectionManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private val mediaSessionManager = try {
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    } catch (t: Throwable) {
        null
    }

    private val _currentTrack = MutableStateFlow(MockMediaTrackProvider.defaultTrack)
    val currentTrack: StateFlow<TrackMetadata> = _currentTrack.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(false)
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            try {
                updateFromMetadata(metadata, activeController?.playbackState?.state == PlaybackState.STATE_PLAYING)
            } catch (t: Throwable) {
                // Ignore callback failure
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            try {
                val isPlaying = state?.state == PlaybackState.STATE_PLAYING
                _currentTrack.value = _currentTrack.value.copy(isPlaying = isPlaying)
            } catch (t: Throwable) {
                // Ignore callback failure
            }
        }
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        try {
            handleControllersChanged(controllers)
        } catch (t: Throwable) {
            // Ignore listener error
        }
    }

    fun startListening() {
        try {
            checkAndRegisterSessions()
        } catch (t: Throwable) {
            // Ensure listening startup never crashes
        }

        // Also observe notification listener updates
        scope.launch {
            try {
                AiEqNotificationListener.latestNotificationTrack.collect { track ->
                    if (track != null && track.isValid && !isLiveControllerPlaying()) {
                        _currentTrack.value = track
                    }
                }
            } catch (t: Throwable) {
                // Keep coroutine alive
            }
        }
    }

    fun checkAndRegisterSessions() {
        val msm = mediaSessionManager ?: return
        val componentName = AiEqNotificationListener.getComponentName(context)

        try {
            val controllers = msm.getActiveSessions(componentName)
            _hasNotificationAccess.value = true
            try {
                msm.removeOnActiveSessionsChangedListener(sessionsListener)
            } catch (ignored: Throwable) {}
            msm.addOnActiveSessionsChangedListener(sessionsListener, componentName)
            handleControllersChanged(controllers)
        } catch (e: SecurityException) {
            _hasNotificationAccess.value = false
        } catch (t: Throwable) {
            _hasNotificationAccess.value = false
        }
    }

    private fun handleControllersChanged(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            return
        }

        val playingController = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.firstOrNull()

        if (playingController != activeController) {
            try {
                activeController?.unregisterCallback(controllerCallback)
            } catch (ignored: Throwable) {}
            activeController = playingController
            try {
                activeController?.registerCallback(controllerCallback)
            } catch (ignored: Throwable) {}
        }

        activeController?.let { ctrl ->
            val isPlaying = ctrl.playbackState?.state == PlaybackState.STATE_PLAYING
            updateFromMetadata(ctrl.metadata, isPlaying, ctrl.packageName)
        }
    }

    private fun updateFromMetadata(metadata: MediaMetadata?, isPlaying: Boolean, pkg: String = activeController?.packageName ?: "") {
        if (metadata == null) return

        try {
            val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: ""
            val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: ""
            val album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
            val artUri = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
                ?: metadata.getString(MediaMetadata.METADATA_KEY_ART_URI)
            val duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)
            val mediaId = metadata.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) ?: ""

            if (title.isNotBlank()) {
                _currentTrack.value = TrackMetadata(
                    title = title,
                    artist = artist,
                    album = album,
                    albumArtUri = artUri,
                    durationMs = duration,
                    packageName = pkg,
                    mediaId = mediaId,
                    isPlaying = isPlaying
                )
            }
        } catch (t: Throwable) {
            // Guard against corrupted metadata
        }
    }

    private fun isLiveControllerPlaying(): Boolean {
        return try {
            activeController?.playbackState?.state == PlaybackState.STATE_PLAYING
        } catch (t: Throwable) {
            false
        }
    }

    fun setMockTrack(track: TrackMetadata) {
        _currentTrack.value = track
    }

    fun stopListening() {
        try {
            activeController?.unregisterCallback(controllerCallback)
            activeController = null
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (t: Throwable) {
            // Ignore teardown exceptions
        }
    }
}
