package com.aieq.app.data.ai

import com.aieq.app.data.security.ApiKeyStore
import com.aieq.app.domain.model.AiEqAdjustment
import com.aieq.app.domain.model.AutoEqProfile
import com.aieq.app.domain.model.EqBandAdjustment
import com.aieq.app.domain.model.HeadphoneProfile
import com.aieq.app.domain.model.SoundPreference
import com.aieq.app.domain.model.TrackMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
private data class GeminiBandDto(
    val bandIndex: Int,
    val frequencyHz: Int,
    val deltaGainDb: Float
)

@Serializable
private data class GeminiResponseDto(
    val reasoning: String,
    val adjustments: List<GeminiBandDto>
)

class GeminiApiProvider(
    private val apiKeyStore: ApiKeyStore,
    private val fallbackProvider: LocalHeuristicAiProvider = LocalHeuristicAiProvider()
) : AiEqProvider {

    override val name: String = "Google Gemini 1.5 Flash"
    override val isOnlineRequired: Boolean = true

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun generateAdjustment(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        autoEq: AutoEqProfile,
        preference: SoundPreference,
        intensity: Float
    ): Result<AiEqAdjustment> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyStore.getApiKey()
        if (apiKey.isNullOrBlank()) {
            // No API key provided: seamlessly use local acoustic heuristics without failing
            return@withContext fallbackProvider.generateAdjustment(track, headphone, autoEq, preference, intensity)
        }

        try {
            val prompt = buildPrompt(track, headphone, autoEq, preference, intensity)
            val responseJson = executeGeminiRequest(apiKey, prompt)
            val parsed = parseGeminiStructuredResponse(responseJson)

            val bandAdjustments = parsed.adjustments.map { dto ->
                EqBandAdjustment(
                    bandIndex = dto.bandIndex,
                    frequencyHz = dto.frequencyHz,
                    deltaGainDb = dto.deltaGainDb.coerceIn(-5.0f, 5.0f) * intensity
                )
            }

            Result.success(
                AiEqAdjustment(
                    reasoning = parsed.reasoning,
                    adjustments = bandAdjustments,
                    intensity = intensity,
                    providerName = name
                )
            )
        } catch (e: Exception) {
            // In case of network timeout, bad JSON, or API error: gracefully fall back to local heuristic
            val fallback = fallbackProvider.generateAdjustment(track, headphone, autoEq, preference, intensity)
            if (fallback.isSuccess) {
                val fbResult = fallback.getOrThrow()
                Result.success(
                    fbResult.copy(
                        reasoning = "Gemini unavailable (${e.message ?: "Network error"}). Applied local acoustic heuristics."
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    private fun buildPrompt(
        track: TrackMetadata,
        headphone: HeadphoneProfile,
        autoEq: AutoEqProfile,
        preference: SoundPreference,
        intensity: Float
    ): String {
        return """
            You are an expert acoustic mastering engineer.
            Given this track and headphone with its AutoEq baseline, recommend subtle, musical EQ adjustments.
            
            TRACK INFO:
            - Title: ${track.displayTitle}
            - Artist: ${track.displayArtist}
            - Album: ${track.album}
            
            HEADPHONE INFO:
            - Model: ${headphone.displayName}
            - Form Factor: ${headphone.formFactor}
            - Measurement Lab: ${headphone.measurementSource}
            - AutoEq Harman Base Preamp: ${autoEq.preampDb} dB
            
            USER PREFERENCE:
            - Target Sound: ${preference.label} (${preference.description})
            - AI Intensity: $intensity (0.0 to 1.0)
            
            STANDARD 5-BAND FREQUENCIES:
            Band 0: 60 Hz (Sub-bass / Punch)
            Band 1: 230 Hz (Warmth / Low-mids)
            Band 2: 910 Hz (Body / Vocal fundamentals)
            Band 3: 3600 Hz (Clarity / Presence)
            Band 4: 14000 Hz (Air / Treble shimmer)
            
            CONSTRAINTS:
            - deltaGainDb MUST be between -4.0 dB and +4.0 dB.
            - Output MUST be valid JSON only. No markdown fences.
            
            JSON FORMAT:
            {
              "reasoning": "<1-2 sentence concise acoustic rationale>",
              "adjustments": [
                {"bandIndex": 0, "frequencyHz": 60, "deltaGainDb": 0.0},
                {"bandIndex": 1, "frequencyHz": 230, "deltaGainDb": 0.0},
                {"bandIndex": 2, "frequencyHz": 910, "deltaGainDb": 0.0},
                {"bandIndex": 3, "frequencyHz": 3600, "deltaGainDb": 0.0},
                {"bandIndex": 4, "frequencyHz": 14000, "deltaGainDb": 0.0}
              ]
            }
        """.trimIndent()
    }

    private fun executeGeminiRequest(apiKey: String, promptText: String): String {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.doOutput = true

        val requestPayload = """
            {
              "contents": [
                {
                  "parts": [{"text": ${escapeJson(promptText)}}]
                }
              ],
              "generationConfig": {
                "temperature": 0.3,
                "response_mime_type": "application/json"
              }
            }
        """.trimIndent()

        OutputStreamWriter(conn.outputStream).use { writer ->
            writer.write(requestPayload)
            writer.flush()
        }

        val code = conn.responseCode
        if (code !in 200..299) {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $code"
            throw RuntimeException("Gemini API Error ($code): $err")
        }

        return conn.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseGeminiStructuredResponse(responseJson: String): GeminiResponseDto {
        // Extract candidate text from Gemini response structure
        val textMatch = Regex(""""text":\s*"((?:\\.|[^"\\])*)"""").find(responseJson)
        val rawText = textMatch?.groupValues?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\")
            ?: responseJson

        val cleaned = rawText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return jsonParser.decodeFromString<GeminiResponseDto>(cleaned)
    }

    private fun escapeJson(string: String): String {
        return buildString {
            append('"')
            for (c in string) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(c)
                }
            }
            append('"')
        }
    }
}
