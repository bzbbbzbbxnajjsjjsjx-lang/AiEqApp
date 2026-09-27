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
    private val audioManager = if (!isMockMode) {
        try {
            context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        } catch (t: Throwable) {
            null
        }
    } else null

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
            try {
                updateOutputRouting()
                audioDeviceCallback?.let { audioManager?.registerAudioDeviceCallback(it, null) }
            } catch (t: Throwable) {
                // Safeguard against platform routing or looper initialization issues
            }
        }

        // Observe player session broadcasts (Spotify, YouTube Music, etc.)
        scope.launch {
            try {
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
            } catch (t: Throwable) {
                // Keep coroutine alive
            }
        }
    }

    fun updateOutputRouting() {
        val am = audioManager ?: return

        try {
            val outputs = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            var name = "Internal Speaker"
            var type = "Speaker"
            var isBt = false
            var isHp = false
            var isBle = false

            for (dev in outputs) {
                // Accessing productName on Bluetooth devices can throw SecurityException on Android 12+ if BLUETOOTH_CONNECT is missing
                val safeDevName = try {
                    dev.productName?.toString()?.ifBlank { null }
                } catch (t: Throwable) {
                    null
                }

                when (dev.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                        name = safeDevName ?: "Bluetooth Headset"
                        type = "Bluetooth A2DP"
                        isBt = true
                        isHp = true
                        break
                    }
                    AudioDeviceInfo.TYPE_BLE_HEADSET, AudioDeviceInfo.TYPE_BLE_SPEAKER -> {
                        name = safeDevName ?: "Bluetooth LE Audio"
                        type = "LE Audio"
                        isBt = true
                        isHp = true
                        isBle = true
                        break
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> {
                        name = safeDevName ?: "Wired Headphones"
                        type = "Wired 3.5mm"
                        isHp = true
                        break
                    }
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        name = safeDevName ?: "USB-C Audio"
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
        } catch (t: Throwable) {
            // Guard against platform/permission restrictions during device query
        }
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

            val eq = Equalizer(0, sessionId)
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
        } catch (t: Throwable) {
            val status = if (sessionId == 0) EqCapabilityStatus.RESTRICTED_SESSION_ZERO else EqCapabilityStatus.UNSUPPORTED
            _outputInfo.value = _outputInfo.value.copy(activeSessionId = sessionId, capabilityStatus = status)
            return status
        }
    }

    fun detachCurrentSession() {
        try {
            activeEqualizer?.enabled = false
            activeEqualizer?.release()
        } catch (t: Throwable) {
            // Ignore teardown errors
        } finally {
            activeEqualizer = null
            currentSessionId = 0
            _outputInfo.value = _outputInfo.value.copy(
                activeSessionId = 0,
                capabilityStatus = EqCapabilityStatus.NO_ACTIVE_SESSION
            )
        }
    }

    fun applyProfile(profile: FinalEqProfile): Boolean {
        _appliedProfile.value = profile

        if (isMockMode) {
            return true
        }

        // CRITICAL STARTUP SAFETY:
        // Do NOT eagerly instantiate Equalizer or attach to session 0 at startup!
        // Only apply to active hardware if a valid session is already attached.
        val eq = activeEqualizer ?: return false

        return try {
            eq.enabled = profile.isEnabled

            if (profile.isEnabled) {
                val numBands = eq.numberOfBands.toInt()
                val range = eq.bandLevelRange
                val minMb = range[0].toInt()
                val maxMb = range[1].toInt()

                profile.bands.forEachIndexed { idx, band ->
                    if (idx < numBands) {
                        val targetMb = (band.gainDb * 100f).roundToInt().coerceIn(minMb, maxMb)
                        eq.setBandLevel(idx.toShort(), targetMb.toShort())
                    }
                }
            }
            true
        } catch (t: Throwable) {
            false
        }
    }

    fun release() {
        try {
            audioDeviceCallback?.let { audioManager?.unregisterAudioDeviceCallback(it) }
            activeEqualizer?.enabled = false
            activeEqualizer?.release()
            activeEqualizer = null
        } catch (t: Throwable) {
            // Ignore teardown exceptions
        }
    }
}
