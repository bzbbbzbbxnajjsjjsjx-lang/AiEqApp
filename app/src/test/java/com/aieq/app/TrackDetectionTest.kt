package com.aieq.app

import com.aieq.app.domain.model.TrackMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackDetectionTest {

    @Test
    fun testTrackMetadataFound() {
        val track = TrackMetadata(
            title = "Bohemian Rhapsody",
            artist = "Queen",
            album = "A Night at the Opera",
            isPlaying = true
        )
        assertTrue(track.isValid)
        assertEquals("Bohemian Rhapsody", track.displayTitle)
        assertEquals("Queen", track.displayArtist)
    }

    @Test
    fun testTrackMetadataMissing() {
        val emptyTrack = TrackMetadata()
        assertFalse(emptyTrack.isValid)
        assertEquals("No Track Detected", emptyTrack.displayTitle)
        assertEquals("Unknown Artist", emptyTrack.displayArtist)

        val blankTrack = TrackMetadata(title = "   ", artist = "")
        assertFalse(blankTrack.isValid)
        assertEquals("Unknown Track", blankTrack.displayTitle)
        assertEquals("Unknown Artist", blankTrack.displayArtist)
    }

    @Test
    fun testTrackChangeDetection() {
        val track1 = TrackMetadata(title = "Track A", artist = "Artist 1")
        val track2 = TrackMetadata(title = "Track B", artist = "Artist 2")
        assertNotEquals(track1, track2)
    }

    @Test
    fun testSameTrackEquality() {
        val track1 = TrackMetadata(title = "Track A", artist = "Artist 1", isPlaying = true)
        val track2 = TrackMetadata(title = "Track A", artist = "Artist 1", isPlaying = true)
        assertEquals(track1, track2)
    }
}
