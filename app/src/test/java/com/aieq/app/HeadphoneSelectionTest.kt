package com.aieq.app

import com.aieq.app.data.autoeq.AutoEqDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadphoneSelectionTest {

    @Test
    fun testExactMatchHeadphone() {
        val results = AutoEqDatabase.search("WH-1000XM4")
        assertTrue(results.isNotEmpty())
        val found = results.first()
        assertEquals("Sony", found.manufacturer)
        assertEquals("WH-1000XM4", found.model)
    }

    @Test
    fun testAliasMatchHeadphone() {
        // "xm5" should match Sony WH-1000XM5 and WF-1000XM5
        val resultsXm5 = AutoEqDatabase.search("xm5")
        assertTrue(resultsXm5.isNotEmpty())
        assertTrue(resultsXm5.any { it.model.contains("1000XM5") })

        // "6xx" should match Sennheiser HD 650
        val results6xx = AutoEqDatabase.search("6xx")
        assertTrue(results6xx.isNotEmpty())
        assertEquals("HD 650", results6xx.first().model)
    }

    @Test
    fun testNotFoundHeadphone() {
        val results = AutoEqDatabase.search("NonExistentHeadphone123xyz")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testDefaultHeadphoneAvailable() {
        val defaultHp = AutoEqDatabase.defaultHeadphone
        assertNotNull(defaultHp)
        assertTrue(defaultHp.hasAutoEq)
    }
}
