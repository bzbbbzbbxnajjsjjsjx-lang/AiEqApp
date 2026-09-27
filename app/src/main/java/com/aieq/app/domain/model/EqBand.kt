package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EqBand(
    val index: Int,
    val centerFreqHz: Int,
    val gainDb: Float, // Normalized -12.0f .. +12.0f
    val qFactor: Float = 1.0f
) {
    val displayFreq: String get() = when {
        centerFreqHz >= 1000 -> "${centerFreqHz / 1000}k"
        else -> "${centerFreqHz}Hz"
    }

    val displayGain: String get() = String.format("%.1fdB", gainDb)
}
