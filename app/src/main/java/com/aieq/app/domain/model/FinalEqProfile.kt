package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FinalEqProfile(
    val isEnabled: Boolean = true,
    val isAiEnabled: Boolean = true,
    val preampDb: Float = -2.0f,
    val bands: List<EqBand> = emptyList(),
    val soundPreference: SoundPreference = SoundPreference.NEUTRAL,
    val aiReasoning: String = "Harman target baseline with AI harmonic matching.",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun withManualBandChange(bandIndex: Int, newGainDb: Float): FinalEqProfile {
        val updated = bands.map { band ->
            if (band.index == bandIndex) band.copy(gainDb = newGainDb.coerceIn(-12.0f, 12.0f))
            else band
        }
        val maxGain = updated.maxOfOrNull { it.gainDb } ?: 0.0f
        val compensatedPreamp = if (maxGain > 0f) -maxGain else 0.0f
        return copy(
            bands = updated,
            preampDb = minOf(preampDb, compensatedPreamp),
            isAiEnabled = false,
            aiReasoning = "Custom manual adjustment"
        )
    }

    companion object {
        fun defaultProfile(): FinalEqProfile {
            val bands = listOf(
                EqBand(0, 60, 0.0f),
                EqBand(1, 230, 0.0f),
                EqBand(2, 910, 0.0f),
                EqBand(3, 3600, 0.0f),
                EqBand(4, 14000, 0.0f)
            )
            return FinalEqProfile(
                isEnabled = true,
                isAiEnabled = false,
                preampDb = 0.0f,
                bands = bands,
                aiReasoning = "Initial Flat State"
            )
        }
    }
}
