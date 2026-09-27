package com.aieq.app

import com.aieq.app.audio.AudioEngineManager
import com.aieq.app.data.ai.LocalHeuristicAiProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.AiEqPipeline
import com.aieq.app.domain.pipeline.EqCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RapidInitializationAndConcurrencyStressTest {

    @Test
    fun testRapidInitialization1000Times() {
        val startTime = System.currentTimeMillis()
        val cache = EqCacheManager()

        for (i in 0 until 1000) {
            val autoEq = AutoEqDatabase.getProfile(AutoEqDatabase.HEADPHONES[i % AutoEqDatabase.HEADPHONES.size].id)
            val hp = AutoEqDatabase.HEADPHONES[i % AutoEqDatabase.HEADPHONES.size]
            val track = TrackMetadata(title = "Track $i", artist = "Artist $i")
            val pref = SoundPreference.values()[i % SoundPreference.values().size]

            val profile = FinalEqProfile(
                isEnabled = true,
                isAiEnabled = (i % 2 == 0),
                preampDb = autoEq.preampDb,
                bands = autoEq.bands,
                soundPreference = pref,
                aiReasoning = "Rapid test #$i"
            )
            assertNotNull(profile)

            val key = cache.generateKey(track, hp, pref, 0.5f, true)
            cache.put(key, profile)
            val retrieved = cache.get(key)
            assertNotNull(retrieved)
        }
        val duration = System.currentTimeMillis() - startTime
        assertTrue("1000 iterations must complete swiftly (< 2000ms): took ${duration}ms", duration < 2000)
    }

    @Test
    fun testConcurrencyStressAcrossMultipleCoroutines() = runTest {
        val pipeline = AiEqPipeline(aiProvider = LocalHeuristicAiProvider())
        val engine = AudioEngineManager(isMockMode = true)

        val deferredList = (0 until 100).map { idx ->
            async(Dispatchers.Default) {
                val track = TrackMetadata(title = "Async Song $idx", artist = "Async Artist $idx")
                val hp = AutoEqDatabase.HEADPHONES[idx % AutoEqDatabase.HEADPHONES.size]
                val autoEq = AutoEqDatabase.getProfile(hp.id)
                val pref = SoundPreference.values()[idx % SoundPreference.values().size]

                val computed = pipeline.execute(
                    track = track,
                    headphone = hp,
                    autoEq = autoEq,
                    preference = pref,
                    aiIntensity = (idx % 10) / 10.0f,
                    isAiEnabled = true
                )

                engine.applyProfile(computed)
                computed
            }
        }

        val results = deferredList.awaitAll()
        assertEquals(100, results.size)
        for (res in results) {
            assertNotNull(res)
            assertEquals(5, res.bands.size)
        }
    }
}
