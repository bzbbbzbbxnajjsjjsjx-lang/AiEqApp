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
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    private val _currentTrack = MutableStateFlow(MockMediaTrackProvider.defaultTrack)
    val currentTrack: StateFlow<TrackMetadata> = _currentTrack.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(false)
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromMetadata(metadata, activeController?.playbackState?.state == PlaybackState.STATE_PLAYING)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            val isPlaying = state?.state == PlaybackState.STATE_PLAYING
            _currentTrack.value = _currentTrack.value.copy(isPlaying = isPlaying)
        }
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handleControllersChanged(controllers)
    }

    fun startListening() {
        checkAndRegisterSessions()

        // Also observe notification listener updates
        scope.launch {
            AiEqNotificationListener.latestNotificationTrack.collect { track ->
                if (track != null && track.isValid && !isLiveControllerPlaying()) {
                    _currentTrack.value = track
                }
            }
        }
    }

    fun checkAndRegisterSessions() {
        if (mediaSessionManager == null) return
        val componentName = AiEqNotificationListener.getComponentName(context)

        try {
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            _hasNotificationAccess.value = true
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsListener)
            mediaSessionManager.addOnActiveSessionsChangedListener(sessionsListener, componentName)
            handleControllersChanged(controllers)
        } catch (e: SecurityException) {
            _hasNotificationAccess.value = false
        } catch (e: Exception) {
            // Ignore other unexpected platform exceptions
        }
    }

    private fun handleControllersChanged(controllers: List<MediaController>?) {
        if (controllers.isNullOrEmpty()) {
            return
        }

        // Find currently playing controller or the first one
        val playingController = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.firstOrNull()

        if (playingController != activeController) {
            activeController?.unregisterCallback(controllerCallback)
            activeController = playingController
            activeController?.registerCallback(controllerCallback)
        }

        activeController?.let { ctrl ->
            val isPlaying = ctrl.playbackState?.state == PlaybackState.STATE_PLAYING
            updateFromMetadata(ctrl.metadata, isPlaying, ctrl.packageName)
        }
    }

    private fun updateFromMetadata(metadata: MediaMetadata?, isPlaying: Boolean, pkg: String = activeController?.packageName ?: "") {
        if (metadata == null) return

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
    }

    private fun isLiveControllerPlaying(): Boolean {
        return activeController?.playbackState?.state == PlaybackState.STATE_PLAYING
    }

    fun setMockTrack(track: TrackMetadata) {
        _currentTrack.value = track
    }

    fun stopListening() {
        try {
            activeController?.unregisterCallback(controllerCallback)
            activeController = null
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (e: Exception) {
            // Ignore teardown exceptions
        }
    }
}
