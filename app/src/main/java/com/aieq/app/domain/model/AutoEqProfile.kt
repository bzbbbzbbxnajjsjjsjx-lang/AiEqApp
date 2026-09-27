package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AutoEqProfile(
    val headphoneId: String,
    val source: String = "oratory1990",
    val targetCurve: String = "Harman Target",
    val preampDb: Float = -4.5f,
    val bands: List<EqBand> = emptyList(),
    val isAvailable: Boolean = true
) {
    companion object {
        fun neutralDefault(headphoneId: String = "flat-neutral"): AutoEqProfile {
            val standardFrequencies = listOf(60, 230, 910, 3600, 14000)
            val defaultBands = standardFrequencies.mapIndexed { idx, freq ->
                EqBand(index = idx, centerFreqHz = freq, gainDb = 0.0f)
            }
            return AutoEqProfile(
                headphoneId = headphoneId,
                source = "Flat Neutral",
                targetCurve = "Linear Flat",
                preampDb = 0.0f,
                bands = defaultBands,
                isAvailable = true
            )
        }
    }
}
