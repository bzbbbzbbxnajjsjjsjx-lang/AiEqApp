package com.aieq.app.data.ai

import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBandAdjustment
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata

class LocalHeuristicAiProvider : AiEqProvider {
    override val name: String = "Local Acoustic Heuristics"
    override val isOnlineRequired: Boolean = false

    override suspend fun generateAdjustment(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        autoEq: AutoEqProfile,
        preference: SoundPreference,
        intensity: Float
    ): Result<AiEqAdjustment> {
        val queryText = "${track.title} ${track.artist} ${track.album}".lowercase()

        // 1. Genre / Style Heuristic Inference
        val (genreLabel, rawDeltas) = when {
            queryText.containsAny("rock", "metal", "guitar", "ac/dc", "metallica", "nirvana", "queen") -> {
                "Rock / Heavy Dynamics" to floatArrayOf(1.8f, 0.5f, -0.5f, 1.2f, 1.5f)
            }
            queryText.containsAny("bass", "edm", "techno", "house", "electro", "dance", "synth", "dubstep", "skrillex") -> {
                "Electronic / Dance Rhythm" to floatArrayOf(2.5f, 1.2f, -0.8f, 0.5f, 1.8f)
            }
            queryText.containsAny("hip hop", "rap", "trap", "drake", "eminem", "kendrick", "beat") -> {
                "Hip-Hop / Sub-Bass Focus" to floatArrayOf(2.8f, 1.0f, -0.2f, 0.8f, 0.5f)
            }
            queryText.containsAny("jazz", "acoustic", "folk", "piano", "classical", "orchestra", "chopin", "beethoven", "miles davis") -> {
                "Acoustic & Classical Timbre" to floatArrayOf(-0.5f, 0.0f, 1.2f, 1.5f, 1.0f)
            }
            queryText.containsAny("vocal", "pop", "taylor", "adele", "podcast", "audiobook", "speech") -> {
                "Vocal Intelligibility Focus" to floatArrayOf(-1.0f, 0.5f, 2.0f, 1.2f, -0.5f)
            }
            else -> {
                "Harmonic Balance Tuning" to floatArrayOf(0.8f, 0.2f, 0.5f, 0.8f, 0.5f)
            }
        }

        // 2. Headphone form-factor compensation
        val headphoneCompensation = when {
            headphone.model.contains("HD 600", ignoreCase = true) || headphone.model.contains("HD 650", ignoreCase = true) -> {
                // Open backs lack sub-bass punch, slightly enhance sub-bass
                floatArrayOf(1.0f, 0.2f, 0.0f, 0.0f, 0.0f)
            }
            headphone.model.contains("XM4", ignoreCase = true) || headphone.model.contains("XM5", ignoreCase = true) -> {
                // ANC headphones have heavy mid-bass boom, tighten 230Hz
                floatArrayOf(0.0f, -1.0f, 0.5f, 0.5f, 0.0f)
            }
            headphone.model.contains("DT 990", ignoreCase = true) -> {
                // Beyerdynamic treble peak compensation
                floatArrayOf(0.5f, 0.0f, 0.0f, -1.5f, -2.0f)
            }
            else -> floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
        }

        val standardFrequencies = listOf(60, 230, 910, 3600, 14000)
        val bandAdjustments = standardFrequencies.mapIndexed { idx, freq ->
            val delta = (rawDeltas.getOrElse(idx) { 0.0f } + headphoneCompensation.getOrElse(idx) { 0.0f }) * intensity
            EqBandAdjustment(
                bandIndex = idx,
                frequencyHz = freq,
                deltaGainDb = delta.coerceIn(-5.0f, 5.0f),
                q = 1.0f
            )
        }

        val reasoning = "Offline AI detected '$genreLabel' for '${track.displayTitle}' on ${headphone.displayName}."

        return Result.success(
            AiEqAdjustment(
                reasoning = reasoning,
                adjustments = bandAdjustments,
                intensity = intensity,
                providerName = name
            )
        )
    }

    private fun String.containsAny(vararg terms: String): Boolean {
        return terms.any { this.contains(it, ignoreCase = true) }
    }
}
