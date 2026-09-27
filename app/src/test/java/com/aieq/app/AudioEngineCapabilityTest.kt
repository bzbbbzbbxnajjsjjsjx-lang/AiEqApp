package com.aieq.app

import com.aieq.app.audio.AudioEngineManager
import com.aieq.app.domain.model.EqCapabilityStatus
import com.aieq.app.domain.model.FinalEqProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AudioEngineCapabilityTest {

    private lateinit var audioEngine: AudioEngineManager

    @Before
    fun setUp() {
        audioEngine = AudioEngineManager(isMockMode = true)
    }

    @Test
    fun testAttachToSessionUpdatesStatus() {
        val status = audioEngine.attachToSession(42)
        assertEquals(EqCapabilityStatus.SUPPORTED, status)
        assertEquals(42, audioEngine.outputInfo.value.activeSessionId)
    }

    @Test
    fun testApplyProfileSucceeds() {
        val profile = FinalEqProfile.defaultProfile()
        val success = audioEngine.applyProfile(profile)
        assertTrue(success)
        assertEquals(profile, audioEngine.appliedProfile.value)
    }

    @Test
    fun testDetachClearsSession() {
        audioEngine.attachToSession(101)
        audioEngine.detachCurrentSession()
        assertEquals(0, audioEngine.outputInfo.value.activeSessionId)
        assertEquals(EqCapabilityStatus.NO_ACTIVE_SESSION, audioEngine.outputInfo.value.capabilityStatus)
    }
}
