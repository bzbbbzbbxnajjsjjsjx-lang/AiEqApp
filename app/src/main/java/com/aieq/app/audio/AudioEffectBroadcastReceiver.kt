package com.aieq.app.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AudioEffectBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: ""

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId != -1) {
                    _sessionEvents.tryEmit(SessionEvent.Opened(sessionId, packageName))
                }
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                if (sessionId != -1) {
                    _sessionEvents.tryEmit(SessionEvent.Closed(sessionId, packageName))
                }
            }
        }
    }

    sealed class SessionEvent {
        data class Opened(val sessionId: Int, val packageName: String) : SessionEvent()
        data class Closed(val sessionId: Int, val packageName: String) : SessionEvent()
    }

    companion object {
        private val _sessionEvents = MutableSharedFlow<SessionEvent>(extraBufferCapacity = 16)
        val sessionEvents: SharedFlow<SessionEvent> = _sessionEvents.asSharedFlow()
    }
}
