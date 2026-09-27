# ANDROID AUDIO ARCHITECTURE & EQUALIZER HARDENING

## 1. Reality Check: System-Wide Equalization in Modern Android

A common misconception in Android audio engineering is that an application can simply instantiate `Equalizer(0, 0)` (attaching to session 0 / global mix) and expect it to apply to all media playback across all apps and devices.

### Official Findings & Real-World Constraints:
1. **Global Session 0 Deprecation:**
   - In Android 9.0 (API 28) and higher, applying audio effects to global audio session 0 is restricted or ignored on many OEM builds (e.g. Samsung OneUI, Google Pixel, Xiaomi MIUI) for security, battery, and audio HAL reasons.
   - Instantiating `Equalizer(0, 0)` without holding privileged system permissions can throw `IllegalArgumentException` or `UnsupportedOperationException`.
2. **Player Audio Session Intent Protocol:**
   - Standard media players (Spotify, YouTube Music, Deezer, VLC, Poweramp) broadcast `android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION` with `EXTRA_AUDIO_SESSION` when beginning playback.
   - Attaching `Equalizer(priority = 1000, sessionId = extraSessionId)` directly to the player's specific audio session ID guarantees 100% reliable hardware DSP filtering without needing root or system app privileges.
3. **OEM Audio Engine Conflicts:**
   - Many OEMs integrate hardware DSP engines (Dolby Atmos, Dirac, Xiaomi Sound, SoundAlive). If a conflicting global DSP is active, third-party Equalizer effects can be preempted (`equalizer.hasControl() == false`).

---

## 2. Capability Detection Protocol

The application executes a multi-step capability probe before claiming that EQ is active:

```kotlin
fun probeCapability(sessionId: Int): EqCapabilityStatus {
    return try {
        val testEq = Equalizer(0, sessionId)
        val bands = testEq.numberOfBands
        val range = testEq.bandLevelRange
        testEq.release()
        
        if (bands > 0 && range != null && range.size >= 2) {
            EqCapabilityStatus.SUPPORTED
        } else {
            EqCapabilityStatus.UNSUPPORTED
        }
    } catch (e: UnsupportedOperationException) {
        EqCapabilityStatus.UNSUPPORTED
    } catch (e: Exception) {
        if (sessionId == 0) EqCapabilityStatus.RESTRICTED_SESSION_ZERO
        else EqCapabilityStatus.UNSUPPORTED
    }
}
```

---

## 3. Audio Routing & Bluetooth Profile Awareness

The app registers an `AudioDeviceCallback` with `AudioManager` to observe physical output changes:
- `AudioDeviceInfo.TYPE_BLUETOOTH_A2DP`: Standard high-quality Bluetooth audio profile.
- `AudioDeviceInfo.TYPE_BLE_HEADSET` / `TYPE_BLE_SPEAKER`: Bluetooth Low Energy (LE) Audio with LC3 codec.
- `AudioDeviceInfo.TYPE_WIRED_HEADSET` / `TYPE_WIRED_HEADPHONES`: 3.5mm analog output.
- `AudioDeviceInfo.TYPE_USB_HEADSET`: USB-C digital DAC.
- `AudioDeviceInfo.TYPE_BUILTIN_SPEAKER`: Phone internal speakers (EQ automatically bypassed or tuned to prevent hardware damage).
