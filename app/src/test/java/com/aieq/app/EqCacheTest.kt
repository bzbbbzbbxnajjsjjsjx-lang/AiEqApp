package com.aieq.app

import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.EqCacheManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EqCacheTest {

    private val cacheManager = EqCacheManager()

    @Test
    fun testCacheHitAndMiss() {
        val track = TrackMetadata(title = "Stairway to Heaven", artist = "Led Zeppelin")
        val headphone = AutoEqDatabase.defaultHeadphone
        val preference = SoundPreference.WARM
        val intensity = 0.8f

        val key = cacheManager.generateKey(track, headphone, preference, intensity, isAiEnabled = true)

        // Cache miss initially
        assertNull(cacheManager.get(key))

        // Store
        val profile = FinalEqProfile(aiReasoning = "Warm tube emulation for classic rock")
        cacheManager.put(key, profile)

        // Cache hit
        val cached = cacheManager.get(key)
        assertNotNull(cached)
        assertEquals("Warm tube emulation for classic rock", cached?.aiReasoning)
    }

    @Test
    fun testCacheKeyDeterminism() {
        val track1 = TrackMetadata(title = "Song A", artist = "Artist B")
        val track2 = TrackMetadata(title = "song a", artist = "artist b") // case insensitive
        val hp = AutoEqDatabase.defaultHeadphone

        val key1 = cacheManager.generateKey(track1, hp, SoundPreference.NEUTRAL, 1.0f, true)
        val key2 = cacheManager.generateKey(track2, hp, SoundPreference.NEUTRAL, 1.0f, true)

        assertEquals(key1, key2)
    }

    @Test
    fun testCacheClear() {
        val track = TrackMetadata(title = "Test", artist = "Test")
        val hp = AutoEqDatabase.defaultHeadphone
        val key = cacheManager.generateKey(track, hp, SoundPreference.NEUTRAL, 1.0f, true)

        cacheManager.put(key, FinalEqProfile.defaultProfile())
        assertEquals(1, cacheManager.size)

        cacheManager.clear()
        assertEquals(0, cacheManager.size)
        assertNull(cacheManager.get(key))
    }
}
