package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EqBandAdjustment(
    val bandIndex: Int,
    val frequencyHz: Int,
    val deltaGainDb: Float, // Adjustments typically between -4.0f and +4.0f
    val q: Float = 1.0f
)

@Serializable
data class AiEqAdjustment(
    val reasoning: String = "Balanced acoustic response tuned for vocal and instrumental balance.",
    val adjustments: List<EqBandAdjustment> = emptyList(),
    val preampDb: Float = 0.0f,
    val intensity: Float = 1.0f,
    val providerName: String = "AI Engine"
)
