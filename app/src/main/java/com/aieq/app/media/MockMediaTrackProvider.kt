package com.aieq.app.media

import com.aieq.app.domain.model.TrackMetadata

object MockMediaTrackProvider {

    val SAMPLE_TRACKS: List<TrackMetadata> = listOf(
        TrackMetadata(
            title = "Back in Black",
            artist = "AC/DC",
            album = "Back in Black",
            durationMs = 255000L,
            packageName = "com.spotify.music",
            mediaId = "mock-rock-1",
            isPlaying = true
        ),
        TrackMetadata(
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 243000L,
            packageName = "com.spotify.music",
            mediaId = "mock-edm-1",
            isPlaying = true
        ),
        TrackMetadata(
            title = "HUMBLE.",
            artist = "Kendrick Lamar",
            album = "DAMN.",
            durationMs = 177000L,
            packageName = "com.google.android.apps.youtube.music",
            mediaId = "mock-hiphop-1",
            isPlaying = true
        ),
        TrackMetadata(
            title = "So What",
            artist = "Miles Davis",
            album = "Kind of Blue",
            durationMs = 562000L,
            packageName = "com.spotify.music",
            mediaId = "mock-jazz-1",
            isPlaying = true
        ),
        TrackMetadata(
            title = "Clair de Lune",
            artist = "Claude Debussy",
            album = "Suite bergamasque",
            durationMs = 302000L,
            packageName = "com.apple.android.music",
            mediaId = "mock-classical-1",
            isPlaying = true
        ),
        TrackMetadata(
            title = "Easy On Me",
            artist = "Adele",
            album = "30",
            durationMs = 224000L,
            packageName = "com.spotify.music",
            mediaId = "mock-vocal-1",
            isPlaying = true
        )
    )

    val defaultTrack: TrackMetadata get() = SAMPLE_TRACKS.first()
}
