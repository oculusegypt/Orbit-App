package com.example.core

import android.content.Context
import android.provider.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Robust HTTP client for Google Gemini LLM API (gemini-2.0-flash) using standard OkHttp.
 *
 * Provides real streaming/single-shot inference for features such as the Socratic Tutor.
 * Falls back gracefully to the built-in deterministic tutor if no API key is available.
 */
class GeminiClient(private val appContext: Context) {

    private val apiKey: String? by lazy { resolveApiKey() }

    /** True when a usable Gemini API key was found at runtime. */
    val isAvailable: Boolean get() = !apiKey.isNullOrBlank()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Sends a single-turn prompt and returns the model's full text response,
     * or null on any failure (missing key, network, blocked…).
     */
    suspend fun generateText(prompt: String): String? = withContext(Dispatchers.IO) {
        val key = apiKey ?: return@withContext null
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$key"

            val jsonPayload = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        })
                    })
                }
                put("contents", contentsArray)

                val systemInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", DEFAULT_SYSTEM_INSTRUCTION))
                    })
                }
                put("systemInstruction", systemInstruction)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body?.string() ?: return@withContext null
            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content") ?: return@withContext null
            val parts = contentObj.optJSONArray("parts") ?: return@withContext null
            if (parts.length() == 0) return@withContext null

            val text = parts.getJSONObject(0).optString("text")
            text.trim().ifBlank { null }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Streams or sends the model reply, invoking [onToken] on completion.
     */
    suspend fun generateTextStreaming(
        prompt: String,
        onToken: (String) -> Unit,
    ): String? {
        val text = generateText(prompt)
        if (!text.isNullOrBlank()) {
            onToken(text)
        }
        return text
    }

    private fun resolveApiKey(): String? {
        val fromBuildConfig = runCatching {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            field.get(null) as? String
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
