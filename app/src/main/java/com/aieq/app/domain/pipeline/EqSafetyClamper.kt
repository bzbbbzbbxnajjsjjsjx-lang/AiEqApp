package com.aieq.app.domain.pipeline

import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBand
import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.SoundPreference
import kotlin.math.max
import kotlin.math.min

object EqSafetyClamper {

    const val MAX_AI_DELTA_DB = 5.0f // AI adjustment delta clamped to [-5.0dB, +5.0dB]
    const val MIN_AI_DELTA_DB = -5.0f

    const val MAX_FINAL_GAIN_DB = 12.0f // Hardware DSP maximum gain
    const val MIN_FINAL_GAIN_DB = -12.0f // Hardware DSP minimum gain

    /**
     * Synthesizes and clamps AutoEq base curve + AI adjustment deltas + user preference bias.
     * Enforces anti-clipping preamp attenuation.
     */
    fun synthesizeAndClamp(
        autoEq: AutoEqProfile,
        aiAdjustment: AiEqAdjustment?,
        preference: SoundPreference,
        isAiEnabled: Boolean
    ): FinalEqProfile {
        val baseBands = autoEq.bands.ifEmpty { AutoEqProfile.neutralDefault().bands }
        
        // 1. Preference bias mapping across standard 5 bands
        val prefBias = when (preference) {
            SoundPreference.NEUTRAL -> listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            SoundPreference.WARM -> listOf(1.5f, 1.0f, 0.0f, -1.0f, -1.5f)
            SoundPreference.BRIGHT -> listOf(-1.0f, -0.5f, 0.5f, 1.5f, 2.0f)
            SoundPreference.BASS_BOOST -> listOf(3.5f, 2.0f, 0.0f, 0.0f, 0.5f)
        }

        // 2. Synthesize each band
        val synthesizedBands = baseBands.mapIndexed { idx, baseBand ->
            val aiDelta = if (isAiEnabled && aiAdjustment != null) {
                val bandAdj = aiAdjustment.adjustments.find { it.bandIndex == idx || it.frequencyHz == baseBand.centerFreqHz }
                val rawDelta = bandAdj?.deltaGainDb ?: 0.0f
                val safeDelta = if (rawDelta.isNaN() || rawDelta.isInfinite()) 0.0f else rawDelta
                // Clamp AI delta to [-5dB, +5dB]
                val clampedDelta = safeDelta.coerceIn(MIN_AI_DELTA_DB, MAX_AI_DELTA_DB)
                // Scale by intensity
                val scaled = clampedDelta * aiAdjustment.intensity
                if (scaled.isNaN() || scaled.isInfinite()) 0.0f else scaled
            } else {
                0.0f
            }

            val bias = prefBias.getOrElse(idx) { 0.0f }

            val rawCombinedGain = baseBand.gainDb + aiDelta + bias
            val finalClampedGain = if (rawCombinedGain.isNaN() || rawCombinedGain.isInfinite()) {
                baseBand.gainDb
            } else {
                rawCombinedGain.coerceIn(MIN_FINAL_GAIN_DB, MAX_FINAL_GAIN_DB)
            }

            EqBand(
                index = baseBand.index,
                centerFreqHz = baseBand.centerFreqHz,
                gainDb = finalClampedGain,
                qFactor = baseBand.qFactor
            )
        }

        // 3. Preamp compensation (anti-clipping rule)
        // If highest band is +G dB, digital preamp must be at least -G dB to prevent digital clipping
        val maxPositiveGain = synthesizedBands.maxOfOrNull { it.gainDb } ?: 0.0f
        val safeMaxPositiveGain = if (maxPositiveGain.isNaN() || maxPositiveGain.isInfinite()) 0.0f else maxPositiveGain
        val antiClippingPreamp = if (safeMaxPositiveGain > 0f) -safeMaxPositiveGain else 0.0f

        // Respect base AutoEq preamp if it's already more conservative
        val safeBasePreamp = if (autoEq.preampDb.isNaN() || autoEq.preampDb.isInfinite()) 0.0f else autoEq.preampDb
        val finalPreamp = min(safeBasePreamp, antiClippingPreamp)

        val reasoningText = if (isAiEnabled && aiAdjustment != null) {
            aiAdjustment.reasoning
        } else {
            "AutoEq Harman profile with ${preference.label} acoustic bias."
        }

        return FinalEqProfile(
            isEnabled = true,
            isAiEnabled = isAiEnabled,
            preampDb = finalPreamp,
            bands = synthesizedBands,
            soundPreference = preference,
            aiReasoning = reasoningText
        )
    }
}
