package com.aieq.app

import com.aieq.app.data.ai.LocalHeuristicAiProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiStructuredOutputTest {

    private val provider = LocalHeuristicAiProvider()

    @Test
    fun testValidStructuredOutputGenerated() = runBlocking {
        val track = TrackMetadata(title = "Starlight", artist = "Muse")
        val headphone = AutoEqDatabase.defaultHeadphone
        val autoEq = AutoEqDatabase.getProfile(headphone.id)

        val result = provider.generateAdjustment(
            track = track,
            headphone = headphone,
            autoEq = autoEq,
            preference = SoundPreference.NEUTRAL,
            intensity = 1.0f
        )

        assertTrue(result.isSuccess)
        val adj = result.getOrThrow()
        assertNotNull(adj.reasoning)
        assertEquals(5, adj.adjustments.size)

        // Frequencies must match standard 5 bands: 60, 230, 910, 3600, 14000
        val freqs = adj.adjustments.map { it.frequencyHz }
        assertEquals(listOf(60, 230, 910, 3600, 14000), freqs)

        // Gains must be within reasonable bounds
        for (band in adj.adjustments) {
            assertTrue("Gain ${band.deltaGainDb} out of range", band.deltaGainDb in -5.0f..5.0f)
        }
    }

    @Test
    fun testIntensityScaling() = runBlocking {
        val track = TrackMetadata(title = "Rock Anthem", artist = "AC/DC")
        val headphone = AutoEqDatabase.defaultHeadphone
        val autoEq = AutoEqDatabase.getProfile(headphone.id)

        val fullIntensity = provider.generateAdjustment(track, headphone, autoEq, SoundPreference.NEUTRAL, 1.0f).getOrThrow()
        val halfIntensity = provider.generateAdjustment(track, headphone, autoEq, SoundPreference.NEUTRAL, 0.5f).getOrThrow()

        // At half intensity, delta gains should be halved
        assertEquals(fullIntensity.adjustments[0].deltaGainDb * 0.5f, halfIntensity.adjustments[0].deltaGainDb, 0.01f)
    }
}
