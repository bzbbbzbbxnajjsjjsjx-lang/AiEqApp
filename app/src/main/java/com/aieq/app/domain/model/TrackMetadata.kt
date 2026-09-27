package com.aieq.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrackMetadata(
    val title: String = "No Track Detected",
    val artist: String = "Unknown Artist",
    val album: String = "",
    val albumArtUri: String? = null,
    val durationMs: Long = 0L,
    val packageName: String = "",
    val mediaId: String = "",
    val isPlaying: Boolean = false
) {
    val isValid: Boolean get() = title.isNotBlank() && title != "No Track Detected"
    
    val displayTitle: String get() = if (title.isBlank()) "Unknown Track" else title
    val displayArtist: String get() = if (artist.isBlank()) "Unknown Artist" else artist
}
