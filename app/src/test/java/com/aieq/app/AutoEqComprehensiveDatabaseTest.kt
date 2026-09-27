package com.aieq.app

import com.aieq.app.data.autoeq.AutoEqDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoEqComprehensiveDatabaseTest {

    @Test
    fun testAllEmbeddedHeadphonesHaveValidMetadata() {
        val headphones = AutoEqDatabase.HEADPHONES
        assertTrue("Headphones list cannot be empty", headphones.isNotEmpty())

        for (hp in headphones) {
            assertTrue("ID must not be blank for ${hp.id}", hp.id.isNotBlank())
            assertTrue("Manufacturer must not be blank for ${hp.id}", hp.manufacturer.isNotBlank())
            assertTrue("Model must not be blank for ${hp.id}", hp.model.isNotBlank())
            assertTrue("FormFactor must not be blank for ${hp.id}", hp.formFactor.isNotBlank())
            assertTrue("Measurement source must not be blank for ${hp.id}", hp.measurementSource.isNotBlank())
            assertTrue("Aliases must not be null for ${hp.id}", hp.aliases != null)
        }
    }

    @Test
    fun testAllEmbeddedProfilesAreAcousticallyValid() {
        for (hp in AutoEqDatabase.HEADPHONES) {
            val profile = AutoEqDatabase.getProfile(hp.id)
            assertNotNull(profile)
            assertEquals(hp.id, profile.headphoneId)
            assertTrue("Preamp should be non-positive to prevent clipping: ${profile.preampDb}", profile.preampDb <= 0.0f)
            assertTrue("Bands count must be 5", profile.bands.size == 5)

            var prevFreq = 0
            for (band in profile.bands) {
                assertTrue("Frequencies must be strictly ascending: ${band.centerFreqHz} > $prevFreq", band.centerFreqHz > prevFreq)
                assertTrue("Gain must be within reasonable acoustic range [-20, 20]: ${band.gainDb}", band.gainDb in -20f..20f)
                prevFreq = band.centerFreqHz
            }
        }
    }

    @Test
    fun testMissingOrCorruptedProfileFallback() {
        val fallback = AutoEqDatabase.getProfile("non-existent-headphone-id-xyz")
        assertNotNull(fallback)
        assertEquals(0.0f, fallback.preampDb, 0.01f)
        assertEquals(5, fallback.bands.size)
        // All bands flat
        for (b in fallback.bands) {
            assertEquals(0.0f, b.gainDb, 0.01f)
        }
    }

    @Test
    fun testSearchQueriesWithEdgeCases() {
        // Empty query
        val emptyResult = AutoEqDatabase.search("")
        assertEquals(AutoEqDatabase.HEADPHONES.size, emptyResult.size)

        // Special characters
        val specialResult = AutoEqDatabase.search("@#$%^&*()")
        assertTrue(specialResult.isEmpty())

        // Case insensitivity
        val xm5Lower = AutoEqDatabase.search("xm5")
        val xm5Upper = AutoEqDatabase.search("XM5")
        assertEquals(xm5Lower.size, xm5Upper.size)
    }
}
