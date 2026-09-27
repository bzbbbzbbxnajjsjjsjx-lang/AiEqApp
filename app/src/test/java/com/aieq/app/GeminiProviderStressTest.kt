package com.aieq.app

import com.aieq.app.data.ai.AiEqProvider
import com.aieq.app.data.ai.LocalHeuristicAiProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.AiEqPipeline
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class GeminiProviderStressTest {

    private val sampleTrack = TrackMetadata(title = "Bohemian Rhapsody", artist = "Queen", album = "A Night at the Opera")
    private val sampleHeadphone = AutoEqDatabase.defaultHeadphone
    private val sampleAutoEq = AutoEqDatabase.getProfile(sampleHeadphone.id)

    @Test
    fun testOfflineFallbackOnNetworkExceptionOrTimeout() = runTest {
        val failingProvider = object : AiEqProvider {
            override val name: String = "Mock Failing Gemini"
            override val isOnlineRequired: Boolean = true

            override suspend fun generateAdjustment(
                track: TrackMetadata,
                headphone: HeadphoneProfile,
                autoEq: AutoEqProfile,
                preference: SoundPreference,
                intensity: Float
            ): Result<AiEqAdjustment> {
                return Result.failure(SocketTimeoutException("Read timed out"))
            }
        }

        val pipeline = AiEqPipeline(aiProvider = failingProvider)
        val profile = pipeline.execute(
            track = sampleTrack,
            headphone = sampleHeadphone,
            autoEq = sampleAutoEq,
            preference = SoundPreference.WARM,
            aiIntensity = 0.8f,
            isAiEnabled = true
        )

        assertNotNull(profile)
        assertTrue(profile.bands.isNotEmpty())
        assertTrue(profile.aiReasoning.contains("AI unavailable") || profile.aiReasoning.contains("AutoEq"))
    }

    @Test
    fun testHttpErrorCodesFallback() = runTest {
        val errorCodes = listOf(400, 401, 403, 429, 500, 503)
        for (code in errorCodes) {
            val httpErrorProvider = object : AiEqProvider {
                override val name: String = "HTTP $code Provider"
                override val isOnlineRequired: Boolean = true
                override suspend fun generateAdjustment(
                    track: TrackMetadata,
                    headphone: HeadphoneProfile,
                    autoEq: AutoEqProfile,
                    preference: SoundPreference,
                    intensity: Float
                ): Result<AiEqAdjustment> {
                    return Result.failure(IOException("HTTP Error $code"))
                }
            }

            val pipeline = AiEqPipeline(aiProvider = httpErrorProvider)
            val profile = pipeline.execute(
                track = sampleTrack,
                headphone = sampleHeadphone,
                autoEq = sampleAutoEq,
                preference = SoundPreference.BRIGHT,
                aiIntensity = 0.5f,
                isAiEnabled = true
            )
            assertNotNull("Pipeline must safely survive HTTP $code", profile)
            assertTrue(profile.bands.size == 5)
        }
    }

    @Test
    fun testLocalHeuristicAlwaysSucceedsDeterministically() = runTest {
        val provider = LocalHeuristicAiProvider()
        for (preference in SoundPreference.values()) {
            val result = provider.generateAdjustment(sampleTrack, sampleHeadphone, sampleAutoEq, preference, 0.8f)
            assertTrue("Local heuristic must always succeed for $preference", result.isSuccess)
            val adj = result.getOrThrow()
            assertTrue(adj.adjustments.isNotEmpty())
        }
    }
}
