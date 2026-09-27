package com.aieq.app.media

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.aieq.app.domain.model.TrackMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiEqNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerConnected.value = true
        activeNotificationListenerInstance = this
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isListenerConnected.value = false
        if (activeNotificationListenerInstance == this) {
            activeNotificationListenerInstance = null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Check if this is a media notification
        val isMediaStyle = notification.isMediaNotification()
        if (isMediaStyle) {
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val album = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            if (title.isNotBlank()) {
                val metadata = TrackMetadata(
                    title = title,
                    artist = artist,
                    album = album,
                    packageName = sbn.packageName,
                    isPlaying = true
                )
                _latestNotificationTrack.value = metadata
            }
        }
    }

    private fun Notification.isMediaNotification(): Boolean {
        val template = extras?.getString(Notification.EXTRA_TEMPLATE) ?: ""
        if (template.contains("MediaStyle")) return true
        if (extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true) return true
        return false
    }

    companion object {
        private val _isListenerConnected = MutableStateFlow(false)
        val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

        private val _latestNotificationTrack = MutableStateFlow<TrackMetadata?>(null)
        val latestNotificationTrack: StateFlow<TrackMetadata?> = _latestNotificationTrack.asStateFlow()

        var activeNotificationListenerInstance: AiEqNotificationListener? = null

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, AiEqNotificationListener::class.java)
        }
    }
}
