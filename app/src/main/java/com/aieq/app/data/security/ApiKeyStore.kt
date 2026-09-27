package com.aieq.app.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyStore(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                "aieq_secure_keys",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            // Robust fallback against Keystore or Tink crashes/NoClassDefFoundError on customized Android ROMs
            context.getSharedPreferences("aieq_keys_private", Context.MODE_PRIVATE)
        }
    }

    fun saveApiKey(apiKey: String) {
        try {
            prefs.edit().putString(KEY_GEMINI_API, apiKey.trim()).apply()
        } catch (t: Throwable) {
            // Ignore storage errors
        }
    }

    fun getApiKey(): String? {
        return try {
            val key = prefs.getString(KEY_GEMINI_API, null)
            if (key.isNullOrBlank()) null else key
        } catch (t: Throwable) {
            null
        }
    }

    fun clearApiKey() {
        try {
            prefs.edit().remove(KEY_GEMINI_API).apply()
        } catch (t: Throwable) {
            // Ignore error
        }
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key_v1"
    }
}
