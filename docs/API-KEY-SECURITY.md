# API KEY SECURITY & CRYPTOGRAPHIC HARDENING

## 1. Zero Plaintext Invariant

Under no circumstances may the user's Gemini API key be:
- Hardcoded in Kotlin/Java source code.
- Committed to Git version control.
- Output to Android logcat (`Log.d`, `println`, `Timber`).
- Serialized to plain XML/JSON files on storage.

---

## 2. Hardware-Backed Storage via Android Keystore

The API key is encrypted using AES-256-GCM authenticated encryption:
1. `ApiKeyStore` generates or retrieves a master key stored in the hardware-isolated **Android Keystore** (`AndroidKeyStore` provider).
2. The key is managed using `EncryptedSharedPreferences` (`androidx.security.crypto.EncryptedSharedPreferences`), ensuring that even on rooted devices or during local file inspection, the raw API key cannot be recovered without hardware cryptographic authorization.

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_ai_eq_keys",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

---

## 3. Graceful Keyless Operation

If the user does not possess or has not entered an API key, the app functions flawlessly in **Local Heuristic Mode**, utilizing acoustic rule sets without any network calls or error prompts.
