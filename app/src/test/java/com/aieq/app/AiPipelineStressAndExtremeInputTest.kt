package com.aieq.app

import com.aieq.app.data.ai.AiEqProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBand
import com.aieq.app.domain.model.EqBandAdjustment
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.AiEqPipeline
import com.aieq.app.domain.pipeline.EqSafetyClamper
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiPipelineStressAndExtremeInputTest {

    @Test
    fun testExtremeInputsSafetyClamping() {
        // Preamp: 999, Gain: 999
        val extremeAutoEq = AutoEqProfile(
            headphoneId = "test",
            source = "test",
            targetCurve = "test",
            preampDb = 999.0f,
            bands = listOf(
                EqBand(0, 60, 999.0f),
                EqBand(1, 230, -999.0f),
                EqBand(2, 910, 500.0f),
                EqBand(3, 3600, -500.0f),
                EqBand(4, 14000, 100.0f)
            )
        )

        val extremeAdjustment = AiEqAdjustment(
            reasoning = "Extreme test",
            adjustments = listOf(
                EqBandAdjustment(0, 60, 999.0f),
                EqBandAdjustment(1, 230, -999.0f),
                EqBandAdjustment(2, 910, 500.0f),
                EqBandAdjustment(3, 3600, -500.0f),
                EqBandAdjustment(4, 14000, 100.0f)
            )
        )

        val finalProfile = EqSafetyClamper.synthesizeAndClamp(extremeAutoEq, extremeAdjustment, SoundPreference.NEUTRAL, true)
        assertNotNull(finalProfile)

        // All band gains must be strictly clamped to [-12, +12]
        for (band in finalProfile.bands) {
            assertTrue("Band gain must be <= 12.0: ${band.gainDb}", band.gainDb <= 12.0f)
            assertTrue("Band gain must be >= -12.0: ${band.gainDb}", band.gainDb >= -12.0f)
        }

        // Anti-clipping preamp compensation: Max net output must not exceed 0 dB
        val maxGain = finalProfile.bands.maxOf { it.gainDb }
        val netPeak = maxGain + finalProfile.preampDb
        assertTrue("Net peak ($netPeak) must be <= 0.05 dB to avoid clipping", netPeak <= 0.05f)
    }

    @Test
    fun testZeroBandsOrExcessiveBandsPipelineResilience() = runTest {
        val excessiveBandsProvider = object : AiEqProvider {
            override val name: String = "100 Bands Provider"
            override val isOnlineRequired: Boolean = false
            override suspend fun generateAdjustment(
                track: TrackMetadata,
                headphone: HeadphoneProfile,
                autoEq: AutoEqProfile,
                preference: SoundPreference,
                intensity: Float
            ): Result<AiEqAdjustment> {
                val list = (0 until 100).map { i ->
                    EqBandAdjustment(i, 20 + i * 200, 3.0f)
                }
                return Result.success(AiEqAdjustment("100 bands", list))
            }
        }

        val pipeline = AiEqPipeline(aiProvider = excessiveBandsProvider)
        val profile = pipeline.execute(
            track = TrackMetadata("Song", "Artist"),
            headphone = AutoEqDatabase.defaultHeadphone,
            autoEq = AutoEqDatabase.getProfile(AutoEqDatabase.defaultHeadphone.id),
            preference = SoundPreference.BASS_BOOST,
            aiIntensity = 1.0f,
            isAiEnabled = true
        )

        assertNotNull(profile)
        // AutoEq base has 5 bands, pipeline safely maps onto the 5 target bands
        assertEquals(5, profile.bands.size)
    }

    @Test
    fun testNaNAndInfiniteFloatsHandledSafely() {
        val nanAdjustment = AiEqAdjustment(
            reasoning = "NaN test",
            adjustments = listOf(
                EqBandAdjustment(0, 60, Float.NaN),
                EqBandAdjustment(1, 230, Float.POSITIVE_INFINITY),
                EqBandAdjustment(2, 910, Float.NEGATIVE_INFINITY)
            )
        )

        val autoEq = AutoEqDatabase.getProfile(AutoEqDatabase.defaultHeadphone.id)
        val finalProfile = EqSafetyClamper.synthesizeAndClamp(autoEq, nanAdjustment, SoundPreference.NEUTRAL, true)
        assertNotNull(finalProfile)

        for (band in finalProfile.bands) {
            assertFalse("Gain must not be NaN", band.gainDb.isNaN())
            assertFalse("Gain must not be Infinite", band.gainDb.isInfinite())
            assertTrue("Gain must be in range [-12, 12]: ${band.gainDb}", band.gainDb in -12.0f..12.0f)
        }
    }
}
