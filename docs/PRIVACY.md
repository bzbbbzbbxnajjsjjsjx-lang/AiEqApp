# PRIVACY & DATA SAFETY POLICY

## 1. Zero Audio Upload Policy

The AI EQ application never records, captures, or transmits microphone data, raw audio streams, or personal voice files. All acoustic signal processing is performed strictly on-device inside Android's native audio DSP stack.

---

## 2. Minimal Metadata Transmission

When an AI optimization is requested using the external Gemini API:
- **Transmitted Data:** Track title, artist name, headphone model name, and target EQ frequency bands.
- **Never Transmitted:** Audio bytes, user identity, device serial numbers, location, or private files.
- **Offline Alternative:** Users can choose to run entirely offline using the built-in **Local Heuristic Provider**, which transmits zero data outside the local device.

---

## 3. Notification Access Scope

The `NotificationListenerService` is used exclusively to observe active `MediaSession` tokens and track metadata from music apps. All personal notifications (SMS, WhatsApp, emails, banking alerts) are strictly ignored and discarded immediately without logging or storage.
