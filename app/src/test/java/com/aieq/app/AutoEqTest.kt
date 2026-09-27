package com.aieq.app

import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.domain.model.AutoEqProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEqTest {

    @Test
    fun testValidAutoEqProfileRetrieved() {
        val profile = AutoEqDatabase.getProfile("sony-wh-1000xm4")
        assertNotNull(profile)
        assertEquals("sony-wh-1000xm4", profile.headphoneId)
        assertTrue(profile.bands.size == 5)
        assertTrue(profile.preampDb < 0.0f) // Must have negative preamp to prevent clipping
    }

    @Test
    fun testMissingProfileReturnsNeutralDefault() {
        val unknown = AutoEqDatabase.getProfile("unknown-diy-headphone")
        assertNotNull(unknown)
        assertTrue(unknown.bands.size == 5)
        assertEquals(0.0f, unknown.preampDb, 0.001f)
        assertTrue(unknown.bands.all { it.gainDb == 0.0f })
    }

    @Test
    fun testAllCuratedHeadphonesHaveProfiles() {
        for (hp in AutoEqDatabase.HEADPHONES) {
            val prof = AutoEqDatabase.getProfile(hp.id)
            assertNotNull("Profile missing for ${hp.displayName}", prof)
            assertTrue(prof.bands.isNotEmpty())
            assertTrue(prof.isAvailable)
        }
    }
}
