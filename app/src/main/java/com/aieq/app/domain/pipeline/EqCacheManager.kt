package com.aieq.app.domain.pipeline

import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class EqCacheManager {

    private val cache = ConcurrentHashMap<String, FinalEqProfile>()

    fun generateKey(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        preference: SoundPreference,
        aiIntensity: Float,
        isAiEnabled: Boolean
    ): String {
        val raw = "${track.artist.trim().lowercase()}::${track.title.trim().lowercase()}::${headphone.id}::${preference.name}::$aiIntensity::$isAiEnabled"
        val md5 = MessageDigest.getInstance("MD5").digest(raw.toByteArray())
        return md5.joinToString("") { "%02x".format(it) }
    }

    fun get(key: String): FinalEqProfile? {
        return cache[key]
    }

    fun put(key: String, profile: FinalEqProfile) {
        cache[key] = profile
    }

    fun clear() {
        cache.clear()
    }

    val size: Int get() = cache.size
}
