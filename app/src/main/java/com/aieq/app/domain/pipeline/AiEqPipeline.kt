package com.aieq.app.domain.pipeline

import com.aieq.app.data.ai.AiEqProvider
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata

class AiEqPipeline(
    private val aiProvider: AiEqProvider,
    private val cacheManager: EqCacheManager = EqCacheManager()
) {

    suspend fun execute(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        autoEq: AutoEqProfile,
        preference: SoundPreference,
        aiIntensity: Float,
        isAiEnabled: Boolean
    ): FinalEqProfile {
        // 1. Check cache first
        val cacheKey = cacheManager.generateKey(track, headphone, preference, aiIntensity, isAiEnabled)
        val cached = cacheManager.get(cacheKey)
        if (cached != null) {
            return cached
        }

        // 2. If AI is disabled: return AutoEq baseline with preference bias
        if (!isAiEnabled) {
            val baseline = EqSafetyClamper.synthesizeAndClamp(
                autoEq = autoEq,
                aiAdjustment = null,
                preference = preference,
                isAiEnabled = false
            )
            cacheManager.put(cacheKey, baseline)
            return baseline
        }

        // 3. Request AI Adjustment
        val aiResult = aiProvider.generateAdjustment(
            track = track,
            headphone = headphone,
            autoEq = autoEq,
            preference = preference,
            intensity = aiIntensity
        )

        // 4. Synthesize with Safety Clamper & Preamp Anti-clipping
        val finalProfile = if (aiResult.isSuccess) {
            val adjustment = aiResult.getOrThrow()
            EqSafetyClamper.synthesizeAndClamp(
                autoEq = autoEq,
                aiAdjustment = adjustment,
                preference = preference,
                isAiEnabled = true
            )
        } else {
            // Graceful offline fallback to AutoEq baseline without failing
            EqSafetyClamper.synthesizeAndClamp(
                autoEq = autoEq,
                aiAdjustment = null,
                preference = preference,
                isAiEnabled = false
            ).copy(
                aiReasoning = "AI unavailable. AutoEq Harman baseline applied cleanly."
            )
        }

        // 5. Cache result
        cacheManager.put(cacheKey, finalProfile)
        return finalProfile
    }

    fun clearCache() {
        cacheManager.clear()
    }
}
