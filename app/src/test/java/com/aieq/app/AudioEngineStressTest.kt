package com.aieq.app

import com.aieq.app.audio.AudioEngineManager
import com.aieq.app.domain.model.EqCapabilityStatus
import com.aieq.app.domain.model.FinalEqProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioEngineStressTest {

    @Test
    fun testScenarioA_NoAudioSession() = runTest {
        val engine = AudioEngineManager(isMockMode = true)
        assertEquals(0, engine.outputInfo.value.activeSessionId)
        assertEquals(EqCapabilityStatus.NO_ACTIVE_SESSION, engine.outputInfo.value.capabilityStatus)
        // applyProfile with no active session should safely store profile and return true/safe status without crash
        val profile = FinalEqProfile.defaultProfile()
        val success = engine.applyProfile(profile)
        assertTrue(success)
        assertEquals(profile, engine.appliedProfile.value)
    }

    @Test
    fun testScenarioB_SessionIdZero() = runTest {
        val engine = AudioEngineManager(isMockMode = true)
        val status = engine.attachToSession(0)
        assertNotNull(status)
        assertEquals(0, engine.outputInfo.value.activeSessionId)
    }

    @Test
    fun testScenarioC_InvalidSessionId() = runTest {
        val engine = AudioEngineManager(isMockMode = true)
        val statusNeg = engine.attachToSession(-1)
        assertNotNull(statusNeg)
        val statusLargeNeg = engine.attachToSession(-99999)
        assertNotNull(statusLargeNeg)
    }

    @Test
    fun testScenarioD_E_EqualizerThrowsRuntimeExceptionOrUnavailable() = runTest {
        // Without active real equalizer (null), applyProfile should not crash
        val engine = AudioEngineManager(context = null, isMockMode = false)
        val profile = FinalEqProfile.defaultProfile()
        val applied = engine.applyProfile(profile)
        // Must return false without throwing any exception
        assertFalse(applied)
        assertEquals(profile, engine.appliedProfile.value)
    }

    @Test
    fun testScenarioF_G_H_I_J_RoutingFailureAndSecurityExceptionResilience() = runTest {
        // Test with context = null simulating unavailable services / security restrictions
        val engine = AudioEngineManager(context = null, isMockMode = false)
        // updateOutputRouting should handle null AudioManager safely without crashing
        engine.updateOutputRouting()
        assertNotNull(engine.outputInfo.value)
        assertEquals("Internal Speaker", engine.outputInfo.value.deviceName)

        // Detach session when none attached
        engine.detachCurrentSession()
        assertEquals(0, engine.outputInfo.value.activeSessionId)

        // Release engine safely
        engine.release()
    }
}
