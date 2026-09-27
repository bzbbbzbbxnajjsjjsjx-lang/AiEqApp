package com.aieq.app.data.ai

import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata

interface AiEqProvider {
    val name: String
    val isOnlineRequired: Boolean

    suspend fun generateAdjustment(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        autoEq: AutoEqProfile,
        preference: SoundPreference,
        intensity: Float
    ): Result<AiEqAdjustment>
}
