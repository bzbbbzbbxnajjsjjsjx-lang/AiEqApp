package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EqCapabilityStatus(val title: String, val message: String) {
    SUPPORTED(
        "EQ Active & Supported",
        "Android Equalizer DSP is attached to active player session with full hardware control."
    ),
    RESTRICTED_SESSION_ZERO(
        "Limited: Session 0 Restricted",
        "Global audio mix is restricted by OEM HAL. Play a track in Spotify / YouTube Music to bind directly to player session."
    ),
    NO_ACTIVE_SESSION(
        "No Active Media Session",
        "No active music player detected. Start playing a track in any media app."
    ),
    UNSUPPORTED(
        "Audio Effect Unsupported",
        "This device or audio output does not support Android Equalizer hardware effects."
    )
}

@Serializable
data class AudioOutputInfo(
    val deviceName: String = "Internal Speaker",
    val deviceType: String = "Speaker",
    val isBluetooth: Boolean = false,
    val isHeadphones: Boolean = false,
    val isLeAudio: Boolean = false,
    val activeSessionId: Int = 0,
    val capabilityStatus: EqCapabilityStatus = EqCapabilityStatus.NO_ACTIVE_SESSION
)
