package com.aieq.app

import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBand
import com.aieq.app.domain.model.EqBandAdjustment
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.pipeline.EqSafetyClamper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyClamperTest {

    @Test
    fun testClampsExcessiveGain() {
        val baseBands = listOf(
            EqBand(0, 60, 10.0f),
            EqBand(1, 230, 0.0f),
            EqBand(2, 910, 0.0f),
            EqBand(3, 3600, 0.0f),
            EqBand(4, 14000, 0.0f)
        )
        val autoEq = AutoEqProfile("test", preampDb = -5.0f, bands = baseBands)

        // Excessive AI delta (+15 dB)
        val wildAi = AiEqAdjustment(
            reasoning = "Excessive test",
            adjustments = listOf(
                EqBandAdjustment(0, 60, deltaGainDb = 15.0f)
            ),
            intensity = 1.0f
        )

        val finalProfile = EqSafetyClamper.synthesizeAndClamp(
            autoEq = autoEq,
            aiAdjustment = wildAi,
            preference = SoundPreference.NEUTRAL,
            isAiEnabled = true
        )

        val band0 = finalProfile.bands[0]
        // Base 10 + clamped AI delta 5 = 15 -> clamped to MAX_FINAL_GAIN_DB = 12.0f
        assertEquals(12.0f, band0.gainDb, 0.001f)
    }

    @Test
    fun testClampsNegativeGainFloor() {
        val baseBands = listOf(
            EqBand(0, 60, -10.0f),
            EqBand(1, 230, 0.0f),
            EqBand(2, 910, 0.0f),
            EqBand(3, 3600, 0.0f),
            EqBand(4, 14000, 0.0f)
        )
        val autoEq = AutoEqProfile("test", preampDb = 0.0f, bands = baseBands)

        // Excessive negative AI delta (-20 dB)
        val wildNegativeAi = AiEqAdjustment(
            adjustments = listOf(
                EqBandAdjustment(0, 60, deltaGainDb = -20.0f)
            ),
            intensity = 1.0f
        )

        val finalProfile = EqSafetyClamper.synthesizeAndClamp(
            autoEq = autoEq,
            aiAdjustment = wildNegativeAi,
            preference = SoundPreference.NEUTRAL,
            isAiEnabled = true
        )

        val band0 = finalProfile.bands[0]
        assertEquals(-12.0f, band0.gainDb, 0.001f)
    }

    @Test
    fun testPreampCompensationPreventsClipping() {
        val baseBands = listOf(
            EqBand(0, 60, 4.0f),
            EqBand(1, 230, 6.0f), // Max positive gain = +6.0 dB
            EqBand(2, 910, 0.0f),
            EqBand(3, 3600, 0.0f),
            EqBand(4, 14000, 0.0f)
        )
        val autoEq = AutoEqProfile("test", preampDb = 0.0f, bands = baseBands)

        val finalProfile = EqSafetyClamper.synthesizeAndClamp(
            autoEq = autoEq,
            aiAdjustment = null,
            preference = SoundPreference.NEUTRAL,
            isAiEnabled = false
        )

        // Since max gain is +6.0 dB, preamp must be at least -6.0 dB to prevent digital clipping!
        assertTrue(finalProfile.preampDb <= -6.0f)
    }
}
