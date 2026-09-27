# DEVICE COMPATIBILITY & LIMITATIONS MATRIX

## 1. Android Audio HAL & OEM Matrix

| Manufacturer / OS | Global Mix (Session 0) | Player Session Intent | Native Equalizer Support | Notes / Workaround |
|---|---|---|---|---|
| **Google Pixel (Stock AOSP)** | Supported (API < 29), Restricted (API 30+) | Supported | Full Hardware DSP | Player session broadcast (`ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION`) is 100% reliable. |
| **Samsung (OneUI)** | Often Overridden by SoundAlive | Supported | Moderate | SoundAlive / Dolby Atmos can preempt session 0. Attaching to Spotify session ID works properly. |
| **Xiaomi (MIUI / HyperOS)** | Restricted | Supported | High | Requires user to disable Mi Sound Enhancer if conflicts arise. |
| **OnePlus / OPPO (OxygenOS)** | Restricted | Supported | Moderate | Dirac Audio Tuner may conflict on headphone output. |
| **Sony (Xperia)** | Partially Supported | Supported | Full | DSEE Ultimate and Dolby Sound co-exist with standard audiofx. |

---

## 2. Headphone Output Protocols

1. **Bluetooth A2DP (Standard Codecs: SBC, AAC, aptX, LDAC):**
   - Completely supported. AudioEffect Equalizer modifies the PCM stream before encoding to Bluetooth packets.
2. **Bluetooth LE Audio (LC3 Codec):**
   - Supported on Android 13+ (API 33+). Audio routing handled automatically via `AudioDeviceInfo.TYPE_BLE_HEADSET`.
3. **USB-C Digital DAC & 3.5mm Headphone Jack:**
   - Low latency, full 5-band / 10-band hardware equalizer support.

---

## 3. Explicit Status Indicators in UI

The application explicitly displays one of four clear capability states:
- **`EQ ACTIVE & SUPPORTED`**: Equalizer successfully attached to active player session with hardware control verified.
- **`RESTRICTED (SESSION 0)`**: No player session broadcast received yet; global mix is restricted by OEM audio HAL. Prompt user to play a track in Spotify / YouTube Music.
- **`NO ACTIVE MEDIA SESSION`**: No media player detected. Prompt user to start playback.
- **`UNSUPPORTED AUDIO EFFECT`**: Hardware HAL does not support `android.media.audiofx.Equalizer`.
