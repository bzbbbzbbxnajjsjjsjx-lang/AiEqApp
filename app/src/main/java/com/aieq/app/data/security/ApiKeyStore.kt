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
        } catch (e: Exception) {
            // Fallback to MODE_PRIVATE on older/restricted test runners
            context.getSharedPreferences("aieq_keys_private", Context.MODE_PRIVATE)
        }
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GEMINI_API, apiKey.trim()).apply()
    }

    fun getApiKey(): String? {
        val key = prefs.getString(KEY_GEMINI_API, null)
        return if (key.isNullOrBlank()) null else key
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_GEMINI_API).apply()
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    companion object {
        private const val KEY_GEMINI_API = "gemini_api_key_v1"
    }
}
