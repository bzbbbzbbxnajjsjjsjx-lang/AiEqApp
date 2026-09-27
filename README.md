# AI EQ

AI-assisted Android headphone equalizer.

AI EQ transforms your mobile listening experience by bridging currently playing track metadata, audiophile-grade AutoEq measurements, and Google Gemini AI sound enhancement into a cohesive, safety-clamped Android audio equalization pipeline.

## Features

- **Now Playing Detection**: Automatic track recognition via `MediaSessionManager`.
- **MediaSession Integration**: Real-time callbacks for track title, artist, album, and artwork.
- **Notification Fallback**: `NotificationListenerService` backup when media session APIs are restricted by background limits.
- **AutoEq Headphone Profiles**: Pre-calibrated parametric Harman target EQ curves (oratory1990 & crinacle measurements) with anti-clipping preamp attenuation.
- **AI EQ Generation**: Generative AI sonic tuning tailored to track genre, mood, headphone frequency response, and user listening preferences.
- **Gemini Provider**: Google Gemini v1beta REST integration with strict structured JSON schema responses.
- **Offline Fallback**: Deterministic heuristic psychoacoustic engine when offline or if API credentials are not provided.
- **EQ Safety Validation**: Strict safety clamping ($\pm 12\text{ dB}$ hard limit, $\pm 5\text{ dB}$ AI maximum) and automatic negative preamp compensation to prevent digital clipping/distortion.
- **Material 3 Expressive UI**: Dynamic color, interactive cubic Hermite/Bézier spline equalizer curve visualizer, animated audio bars, and tonal surface elevation.
- **Manual EQ**: Interactive parametric slider controls allowing custom fine-tuning over AI-generated profiles.
- **Bluetooth/Output Capability Detection**: Real-time monitoring of audio device routing (Bluetooth A2DP, Bluetooth LE Audio, USB Headset, Wired 3.5mm, Speaker) and transparent EQ capability reporting.

## Architecture & Pipeline

```text
Currently Playing Music
        ↓
  Track Metadata
        ↓
Selected Headphone Profile
        ↓
 AutoEq Harman Baseline
        ↓
  AI EQ Adjustment
        ↓
Safety Clamping & Preamp Compensation
        ↓
    Final EQ
        ↓
Android Audio Engine (AudioEffect Equalizer)
```

## Important Device Compatibility Notice

> [!WARNING]
> Android audio effects are device, OEM, and output dependent.
> Many device manufacturers (such as Samsung OneUI or Xiaomi HyperOS) restrict or ignore global audio mix session 0 for external equalizers. Reliable system-wide equalization requires music players that broadcast audio effect session tokens (`ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION`).
> System-wide EQ is not guaranteed on every Android device or proprietary audio path (e.g. exclusive USB DAC mode). The application actively detects and reports its capability status transparently.

## Verification & Status

- **Unit Tests**: 25/25 PASS (100% test coverage across detection, AutoEq, safety clamper, cache, AI parsing, and audio engine capabilities).
- **Build Status**: Gradle Build Successful (AGP 9.0.1, Kotlin 2.3.20, Compile SDK 36, Min SDK 26).
- **Physical Device Audio Verification**: NOT VERIFIED (Tested via comprehensive JVM & Mock test harness).

## Documentation

Full architectural specifications and privacy disclosures are available in the repository:
- [System Architecture Specification](SPEC-AI-EQ.md)
- [Audio Architecture & AudioEffect Sessions](docs/AUDIO-ARCHITECTURE.md)
- [Media Detection & Notification Fallback](docs/MEDIA-DETECTION.md)
- [AutoEq Integration & Filter Parameters](docs/AUTOEQ-INTEGRATION.md)
- [AI EQ Pipeline & Structured JSON Schema](docs/AI-EQ-PIPELINE.md)
- [API Key Security & Keystore Storage](docs/API-KEY-SECURITY.md)
- [Device Compatibility Matrix](docs/DEVICE-COMPATIBILITY.md)
- [Privacy Policy & Permission Hygiene](docs/PRIVACY.md)

## License

Apache License 2.0. AutoEq measurement profiles derived from [jaakkopasanen/AutoEq](https://github.com/jaakkopasanen/AutoEq) under the MIT License.
