package com.example.core

import android.content.Context
import android.provider.Settings
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CancellationException

/**
 * Thin wrapper around the Firebase AI (Generative AI SDK for Firebase / Gemini on Vertex)
 * client. Provides real, streaming-capable LLM access for features such as the Socratic Tutor.
 *
 * The API key resolution order is:
 *  1. `BuildConfig.GEMINI_API_KEY` injected by the Secrets Gradle plugin from `.env`
 *     (AI Studio injects this automatically at build time).
 *  2. The device `debug` secure-setting (handy for local/emulator testing without
 *     baking a key into the APK).
 *
 * When no key is available the client reports [isAvailable] = false and callers are
 * expected to fall back to their offline behaviour.
 */
class GeminiClient(private val appContext: Context) {

    private val apiKey: String? by lazy { resolveApiKey() }

    /** True when a usable Gemini API key was found at runtime. */
    val isAvailable: Boolean get() = !apiKey.isNullOrBlank()

    @Volatile
    private var model: GenerativeModel? = null

    private fun obtainModel(): GenerativeModel? {
        model?.let { return it }
        val key = apiKey ?: return null
        return GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = key,
            systemInstruction = content { text(DEFAULT_SYSTEM_INSTRUCTION) },
            safetySettings = defaultSafetySettings(),
        ).also { model = it }
    }

    /**
     * Sends a single-turn prompt and returns the model's full text response,
     * or null on any failure (missing key, network, blocked…).
     */
    suspend fun generateText(prompt: String): String? {
        val m = obtainModel() ?: return null
        return try {
            val response = m.generateContent(prompt)
            response.text?.trim()?.ifBlank { null }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            // Any SDK / HTTP / safety error → caller falls back gracefully.
            null
        }
    }

    /**
     * Streams the model reply token-by-token via the SDK's real
     * `generateContentStream` channel, invoking [onToken] on every chunk.
     * Returns the concatenated text, or null when streaming is unavailable
     * (falls back internally to a single-shot request first).
     */
    suspend fun generateTextStreaming(
        prompt: String,
        onToken: (String) -> Unit,
    ): String? {
        val m = obtainModel() ?: return null
        return try {
            val channel = m.generateContentStream(prompt)
            val sb = StringBuilder()
            channel.collect { chunk ->
                val piece = chunk.text
                if (!piece.isNullOrEmpty()) {
                    sb.append(piece)
                    onToken(piece)
                }
            }
            sb.toString().trim().ifBlank { null }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            // Streaming failed — one non-streaming retry before giving up.
            runCatching {
                val full = generateText(prompt)
                if (!full.isNullOrEmpty()) onToken(full)
                full
            }.getOrNull()
        }
    }

    private fun resolveApiKey(): String? {
        val fromBuildConfig = runCatching {
            com.example.BuildConfig.GEMINI_API_KEY
        }.getOrNull()
        if (!fromBuildConfig.isNullOrBlank()) return fromBuildConfig

        val fromSettings = runCatching {
            Settings.Secure.getString(appContext.contentResolver, "gemini_api_key")
        }.getOrNull()
        return fromSettings?.takeIf { it.isNotBlank() && it != "null" }
    }

    companion object {
        const val MODEL_NAME = "gemini-2.0-flash"
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "أنت \"مس سارة\"، مدرّسة حساب ذهني وسوروبان مصرية للأطفال بأسلوب سقراطي: " +
                "لا تعطي الحل النهائي مباشرة أبدًا، بل اطرح أسئلة توجيهية خطوة بخطوة، " +
                "استخدم تشبيه خرزات السوروبان وأصدقاء الأعداد، وشجّع الطفل بإيجاز وبود. " +
                "أجب بالعربية العامية المصرية المبسطة وجملة أو جملتين قصيرتين."
    }
}

private fun defaultSafetySettings(): List<SafetySetting> = listOf(
    SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
    SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
    SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
    SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE),
)
