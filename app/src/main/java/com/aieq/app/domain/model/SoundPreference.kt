package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SoundPreference(val label: String, val description: String) {
    NEUTRAL("Neutral", "Reference accurate sound conforming to Harman target."),
    WARM("Warm", "Smooth, relaxed highs with intimate, rich midrange."),
    BRIGHT("Bright", "Airy, clear treble highlighting fine instrument details."),
    BASS_BOOST("Bass", "Energetic low-end punch and sub-bass extension.")
}
