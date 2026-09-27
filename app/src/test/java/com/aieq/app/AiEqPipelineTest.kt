package com.aieq.app

import com.aieq.app.data.ai.LocalHeuristicAiProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.AiEqPipeline
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiEqPipelineTest {

    private val localAi = LocalHeuristicAiProvider()
    private val pipeline = AiEqPipeline(localAi)

    @Test
    fun testPipelineExecutionWithLocalAi() = runBlocking {
        val track = TrackMetadata(title = "Hysteria", artist = "Muse", album = "Absolution")
        val headphone = AutoEqDatabase.getProfile("sony-wh-1000xm4")
        val hpModel = AutoEqDatabase.HEADPHONES.first { it.id == "sony-wh-1000xm4" }

        val finalProfile = pipeline.execute(
            track = track,
            headphone = hpModel,
            autoEq = headphone,
            preference = SoundPreference.BASS_BOOST,
            aiIntensity = 0.8f,
            isAiEnabled = true
        )

        assertNotNull(finalProfile)
        assertTrue(finalProfile.isEnabled)
        assertTrue(finalProfile.isAiEnabled)
        assertEquals(5, finalProfile.bands.size)
        // All bands must satisfy safety clamps
        assertTrue(finalProfile.bands.all { it.gainDb in -12.0f..12.0f })
    }

    @Test
    fun testPipelineWhenAiDisabledReturnsBaseline() = runBlocking {
        val track = TrackMetadata(title = "Quiet Night", artist = "Diana Krall")
        val headphone = AutoEqDatabase.getProfile("sennheiser-hd600")
        val hpModel = AutoEqDatabase.HEADPHONES.first { it.id == "sennheiser-hd600" }

        val finalProfile = pipeline.execute(
            track = track,
            headphone = hpModel,
            autoEq = headphone,
            preference = SoundPreference.NEUTRAL,
            aiIntensity = 1.0f,
            isAiEnabled = false
        )

        assertNotNull(finalProfile)
        assertFalse(finalProfile.isAiEnabled)
        // Neutral preference with AI disabled should directly match AutoEq baseline
        assertEquals(headphone.bands[0].gainDb, finalProfile.bands[0].gainDb, 0.001f)
    }

    @Test
    fun testManualAdjustmentDisablesAiFlag() {
        val profile = com.aieq.app.domain.model.FinalEqProfile.defaultProfile()
        val modified = profile.withManualBandChange(bandIndex = 0, newGainDb = 4.5f)

        assertEquals(4.5f, modified.bands[0].gainDb, 0.001f)
        assertFalse(modified.isAiEnabled)
        assertEquals("Custom manual adjustment", modified.aiReasoning)
    }
}
