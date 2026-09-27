# MEDIA DETECTION & TRACK METADATA ARCHITECTURE

## 1. Primary Strategy: `MediaSessionManager` via `NotificationListenerService`

Android strictly protects active `MediaSession` instances to prevent malicious background spyware from spying on media consumption. Accessing `MediaSessionManager.getActiveSessions()` requires one of two conditions:
1. `android.permission.MEDIA_CONTENT_CONTROL` (reserved exclusively for system applications / OEMs).
2. Holding an enabled `NotificationListenerService` registered in `AndroidManifest.xml` and granted by the user in `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.

### Execution Flow:
1. User grants Notification Access permission to `AiEqNotificationListener`.
2. `MediaDetectionManager` registers `OnActiveSessionsChangedListener(..., componentName)`.
3. Whenever an active playback session is detected (Spotify, YouTube Music, Tidal, Podcasting apps), the app obtains its `MediaController`.
4. It registers a `MediaController.Callback` listening for:
   - `onMetadataChanged(MediaMetadata metadata)`: Extract title, artist, album, album art, duration.
   - `onPlaybackStateChanged(PlaybackState state)`: Monitor if media is playing or paused.

---

## 2. Secondary Strategy: Direct Notification Fallback

If `MediaSessionManager` returns an empty session list on certain custom Android builds (e.g. heavily modified AOSP forks), `AiEqNotificationListener.onNotificationPosted()` inspects incoming status bar notifications:
- Filter by `notification.extras.getString(Notification.EXTRA_TEMPLATE) == "android.app.Notification$MediaStyle"`.
- Extract `Notification.EXTRA_TITLE`, `Notification.EXTRA_TEXT` (Artist), and `Notification.EXTRA_LARGE_ICON`.
- If `Notification.EXTRA_MEDIA_SESSION` is present in extras, extract the token and create a `MediaController` directly.

---

## 3. Test & Development Strategy: Built-in Mock Track Selector

For local development, automated testing, or environments where no external music player is actively playing, the app includes an interactive Mock Track Provider featuring classic genres (Rock, Electronic, Jazz, Classical, Hip-Hop) with realistic acoustic signatures.
