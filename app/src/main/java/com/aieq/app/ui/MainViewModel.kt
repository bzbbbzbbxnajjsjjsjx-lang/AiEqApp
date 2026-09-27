package com.aieq.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aieq.app.audio.AudioEngineManager
import com.aieq.app.data.ai.GeminiApiProvider
import com.aieq.app.data.ai.LocalHeuristicAiProvider
import com.aieq.app.data.autoeq.AutoEqDatabase
import com.aieq.app.data.security.ApiKeyStore
import com.aieq.app.domain.model.AudioOutputInfo
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.FinalEqProfile
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import com.aieq.app.domain.pipeline.AiEqPipeline
import com.aieq.app.media.MediaDetectionManager
import com.aieq.app.media.MockMediaTrackProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val currentTrack: TrackMetadata = MockMediaTrackProvider.defaultTrack,
    val selectedHeadphone: HeadphoneProfile = AutoEqDatabase.defaultHeadphone,
    val currentAutoEqProfile: AutoEqProfile = AutoEqDatabase.getProfile(AutoEqDatabase.defaultHeadphone.id),
    val finalEqProfile: FinalEqProfile = FinalEqProfile.defaultProfile(),
    val outputInfo: AudioOutputInfo = AudioOutputInfo(),
    val hasNotificationAccess: Boolean = false,
    val isAiEnabled: Boolean = true,
    val aiIntensity: Float = 0.8f,
    val soundPreference: SoundPreference = SoundPreference.NEUTRAL,
    val isCalculating: Boolean = false,
    val showHeadphoneSelector: Boolean = false,
    val showApiKeyDialog: Boolean = false,
    val hasApiKey: Boolean = false
)

class MainViewModel(
    application: Application,
    private val audioEngine: AudioEngineManager = AudioEngineManager(application),
    private val mediaDetection: MediaDetectionManager = MediaDetectionManager(application)
) : AndroidViewModel(application) {

    private val apiKeyStore = ApiKeyStore(application)
    private val aiProvider = GeminiApiProvider(
        apiKeyStore = apiKeyStore,
        fallbackProvider = LocalHeuristicAiProvider()
    )
    private val aiPipeline = AiEqPipeline(aiProvider = aiProvider)

    private val _uiState = MutableStateFlow(
        MainUiState(
            hasApiKey = apiKeyStore.hasApiKey()
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var mockTrackIndex = 0

    init {
        // Observe track changes from MediaDetectionManager
        viewModelScope.launch {
            mediaDetection.currentTrack.collect { track ->
                _uiState.value = _uiState.value.copy(currentTrack = track)
                recomputeAndApplyEq()
            }
        }

        // Observe notification access status
        viewModelScope.launch {
            mediaDetection.hasNotificationAccess.collect { hasAccess ->
                _uiState.value = _uiState.value.copy(hasNotificationAccess = hasAccess)
            }
        }

        // Observe audio output routing info
        viewModelScope.launch {
            audioEngine.outputInfo.collect { output ->
                _uiState.value = _uiState.value.copy(outputInfo = output)
            }
        }

        // Start media listening
        mediaDetection.startListening()

        // Initial EQ calculation
        recomputeAndApplyEq()
    }

    fun recomputeAndApplyEq() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)
            val state = _uiState.value

            val computedProfile = aiPipeline.execute(
                track = state.currentTrack,
                headphone = state.selectedHeadphone,
                autoEq = state.currentAutoEqProfile,
                preference = state.soundPreference,
                aiIntensity = state.aiIntensity,
                isAiEnabled = state.isAiEnabled
            )

            _uiState.value = _uiState.value.copy(
                finalEqProfile = computedProfile,
                isCalculating = false
            )

            // Send to audio engine
            audioEngine.applyProfile(computedProfile)
        }
    }

    fun onHeadphoneSelected(headphone: HeadphoneProfile) {
        val autoEq = AutoEqDatabase.getProfile(headphone.id)
        _uiState.value = _uiState.value.copy(
            selectedHeadphone = headphone,
            currentAutoEqProfile = autoEq
        )
        recomputeAndApplyEq()
    }

    fun onAiToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAiEnabled = enabled)
        recomputeAndApplyEq()
    }

    fun onIntensityChange(intensity: Float) {
        _uiState.value = _uiState.value.copy(aiIntensity = intensity)
        recomputeAndApplyEq()
    }

    fun onSoundPreferenceChange(preference: SoundPreference) {
        _uiState.value = _uiState.value.copy(soundPreference = preference)
        recomputeAndApplyEq()
    }

    fun onManualBandGainChanged(bandIndex: Int, newGainDb: Float) {
        val updated = _uiState.value.finalEqProfile.withManualBandChange(bandIndex, newGainDb)
        _uiState.value = _uiState.value.copy(
            finalEqProfile = updated,
            isAiEnabled = false
        )
        audioEngine.applyProfile(updated)
    }

    fun onResetToAutoEq() {
        val state = _uiState.value
        val autoEqBands = state.currentAutoEqProfile.bands
        val resetProfile = FinalEqProfile(
            isEnabled = true,
            isAiEnabled = false,
            preampDb = state.currentAutoEqProfile.preampDb,
            bands = autoEqBands,
            soundPreference = state.soundPreference,
            aiReasoning = "Reset to pure ${state.selectedHeadphone.displayName} AutoEq Harman curve."
        )
        _uiState.value = _uiState.value.copy(
            finalEqProfile = resetProfile,
            isAiEnabled = false
        )
        audioEngine.applyProfile(resetProfile)
    }

    fun onResetToFlat() {
        val flat = FinalEqProfile.defaultProfile()
        _uiState.value = _uiState.value.copy(
            finalEqProfile = flat,
            isAiEnabled = false
        )
        audioEngine.applyProfile(flat)
    }

    fun nextMockTrack() {
        val tracks = MockMediaTrackProvider.SAMPLE_TRACKS
        mockTrackIndex = (mockTrackIndex + 1) % tracks.size
        val next = tracks[mockTrackIndex]
        mediaDetection.setMockTrack(next)
    }

    fun showHeadphoneSelector(show: Boolean) {
        _uiState.value = _uiState.value.copy(showHeadphoneSelector = show)
    }

    fun showApiKeyDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showApiKeyDialog = show)
    }

    fun saveApiKey(key: String) {
        apiKeyStore.saveApiKey(key)
        _uiState.value = _uiState.value.copy(hasApiKey = apiKeyStore.hasApiKey())
        recomputeAndApplyEq()
    }

    fun clearApiKey() {
        apiKeyStore.clearApiKey()
        _uiState.value = _uiState.value.copy(hasApiKey = false)
        recomputeAndApplyEq()
    }

    fun getApiKey(): String? = apiKeyStore.getApiKey()

    override fun onCleared() {
        super.onCleared()
        mediaDetection.stopListening()
        audioEngine.release()
    }
}
