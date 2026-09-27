package com.aieq.app.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import com.aieq.app.domain.model.AudioOutputInfo
import com.aieq.app.domain.model.EqCapabilityStatus
import com.aieq.app.domain.model.FinalEqProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AudioEngineManager(
    private val context: Context? = null,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val isMockMode: Boolean = false
) {
    private val audioManager = if (!isMockMode) context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager else null

    private var activeEqualizer: Equalizer? = null
    private var currentSessionId: Int = 0

    private val _outputInfo = MutableStateFlow(AudioOutputInfo())
    val outputInfo: StateFlow<AudioOutputInfo> = _outputInfo.asStateFlow()

    private val _appliedProfile = MutableStateFlow<FinalEqProfile?>(null)
    val appliedProfile: StateFlow<FinalEqProfile?> = _appliedProfile.asStateFlow()

    private val audioDeviceCallback = if (!isMockMode) {
        object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                updateOutputRouting()
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                updateOutputRouting()
            }
        }
    } else null

    init {
        if (!isMockMode) {
            updateOutputRouting()
            audioDeviceCallback?.let { audioManager?.registerAudioDeviceCallback(it, null) }
        }

        // Observe player session broadcasts (Spotify, YouTube Music, etc.)
        scope.launch {
            AudioEffectBroadcastReceiver.sessionEvents.collect { event ->
                when (event) {
                    is AudioEffectBroadcastReceiver.SessionEvent.Opened -> {
                        attachToSession(event.sessionId)
                    }
                    is AudioEffectBroadcastReceiver.SessionEvent.Closed -> {
                        if (currentSessionId == event.sessionId) {
                            detachCurrentSession()
                        }
                    }
                }
            }
        }
    }

    fun updateOutputRouting() {
        if (audioManager == null) return

        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        var name = "Internal Speaker"
        var type = "Speaker"
        var isBt = false
        var isHp = false
        var isBle = false

        for (dev in outputs) {
            when (dev.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                    name = dev.productName.toString().ifBlank { "Bluetooth Headset" }
                    type = "Bluetooth A2DP"
                    isBt = true
                    isHp = true
                    break
                }
                AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                    name = dev.productName.toString().ifBlank { "Bluetooth LE Audio" }
                    type = "LE Audio"
                    isBt = true
                    isHp = true
                    isBle = true
                    break
                }
                AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                    name = dev.productName.toString().ifBlank { "Wired Headphones" }
                    type = "Wired 3.5mm"
                    isHp = true
                    break
                }
                AudioDeviceInfo.TYPE_USB_HEADSET -> {
                    name = dev.productName.toString().ifBlank { "USB-C Audio" }
                    type = "USB Audio"
                    isHp = true
                    break
                }
            }
        }

        _outputInfo.value = _outputInfo.value.copy(
            deviceName = name,
            deviceType = type,
            isBluetooth = isBt,
            isHeadphones = isHp,
            isLeAudio = isBle
        )
    }

    fun attachToSession(sessionId: Int): EqCapabilityStatus {
        if (isMockMode) {
            currentSessionId = sessionId
            _outputInfo.value = _outputInfo.value.copy(
                activeSessionId = sessionId,
                capabilityStatus = EqCapabilityStatus.SUPPORTED
            )
            return EqCapabilityStatus.SUPPORTED
        }

        try {
            activeEqualizer?.release()
            activeEqualizer = null

            val eq = Equalizer(1000, sessionId)
            eq.enabled = true
            val hasCtrl = eq.hasControl()

            if (hasCtrl && eq.numberOfBands > 0) {
                activeEqualizer = eq
                currentSessionId = sessionId
                val status = EqCapabilityStatus.SUPPORTED
                _outputInfo.value = _outputInfo.value.copy(
                    activeSessionId = sessionId,
                    capabilityStatus = status
                )

                // Re-apply latest profile if one exists
                _appliedProfile.value?.let { applyProfile(it) }
                return status
            } else {
                eq.release()
                val status = if (sessionId == 0) EqCapabilityStatus.RESTRICTED_SESSION_ZERO else EqCapabilityStatus.UNSUPPORTED
                _outputInfo.value = _outputInfo.value.copy(activeSessionId = sessionId, capabilityStatus = status)
                return status
            }
        } catch (e: UnsupportedOperationException) {
            val status = if (sessionId == 0) EqCapabilityStatus.RESTRICTED_SESSION_ZERO else EqCapabilityStatus.UNSUPPORTED
            _outputInfo.value = _outputInfo.value.copy(activeSessionId = sessionId, capabilityStatus = status)
            return status
        } catch (e: Exception) {
            val status = if (sessionId == 0) EqCapabilityStatus.RESTRICTED_SESSION_ZERO else EqCapabilityStatus.UNSUPPORTED
            _outputInfo.value = _outputInfo.value.copy(activeSessionId = sessionId, capabilityStatus = status)
            return status
        }
    }

    fun detachCurrentSession() {
        activeEqualizer?.enabled = false
        activeEqualizer?.release()
        activeEqualizer = null
        currentSessionId = 0
        _outputInfo.value = _outputInfo.value.copy(
            activeSessionId = 0,
            capabilityStatus = EqCapabilityStatus.NO_ACTIVE_SESSION
        )
    }

    fun applyProfile(profile: FinalEqProfile): Boolean {
        _appliedProfile.value = profile

        if (isMockMode) {
            return true
        }

        val eq = activeEqualizer ?: run {
            // Attempt to bind to session 0 if not attached yet
            if (attachToSession(0) != EqCapabilityStatus.SUPPORTED) {
                return false
            }
            activeEqualizer
        } ?: return false

        return try {
            eq.enabled = profile.isEnabled

            if (profile.isEnabled) {
                val numBands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange // e.g. [-1500, 1500] in millibels
                val minMb = range[0].toInt()
                val maxMb = range[1].toInt()

                // Apply each band
                profile.bands.forEachIndexed { idx, band ->
                    if (idx < numBands) {
                        // 1 dB = 100 millibels
                        val targetMb = (band.gainDb * 100f).roundToInt().coerceIn(minMb, maxMb)
                        eq.setBandLevel(idx.toShort(), targetMb.toShort())
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun release() {
        try {
            audioDeviceCallback?.let { audioManager?.unregisterAudioDeviceCallback(it) }
            activeEqualizer?.enabled = false
            activeEqualizer?.release()
            activeEqualizer = null
        } catch (e: Exception) {
            // Ignore teardown exceptions
        }
    }
}
