package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiService {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    /**
     * Resolves the active API key. Checks custom key first, falls back to BuildConfig.GEMINI_API_KEY.
     */
    fun resolveApiKey(customKey: String?): String {
        if (!customKey.isNullOrBlank()) {
            return customKey.trim()
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return buildKey.ifBlank { "" }
    }

    /**
     * Non-streaming content generation.
     */
    suspend fun generateContent(
        model: String,
        request: GeminiRequest,
        customKey: String? = null
    ): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in Settings or AI Studio Secrets panel.")
            )
        }

        val effectiveModel = sanitizeModel(model)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:generateContent?key=$apiKey"
        val jsonPayload = requestAdapter.toJson(request)

        val okRequest = Request.Builder()
            .url(url)
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = client.newCall(okRequest).execute()
            val bodyString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = parseErrorMessage(bodyString, response.code)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val parsed = responseAdapter.fromJson(bodyString)
            val text = parsed?.candidates?.firstOrNull()?.content?.parts?.mapNotNull { it.text }?.joinToString("") ?: ""
            val totalTokens = parsed?.usageMetadata?.totalTokenCount ?: 0

            Result.success(Pair(text, totalTokens))
        } catch (e: Exception) {
            Log.e("GeminiApiService", "API call failed", e)
            Result.failure(e)
        }
    }

    /**
     * Streaming generation using Server-Sent Events (SSE).
     */
    suspend fun streamGenerateContent(
        model: String,
        request: GeminiRequest,
        customKey: String? = null,
        onChunk: (String) -> Unit
    ): Result<Pair<String, Int>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your key in Settings or AI Studio Secrets panel.")
            )
        }

        val effectiveModel = sanitizeModel(model)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$effectiveModel:streamGenerateContent?alt=sse&key=$apiKey"
        val jsonPayload = requestAdapter.toJson(request)

        val okRequest = Request.Builder()
            .url(url)
            .post(jsonPayload.toRequestBody("application/json".toMediaType()))
            .build()

        val fullTextBuilder = StringBuilder()
        var totalTokens = 0

        try {
            val call = client.newCall(okRequest)
            val response = call.execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                val errorMsg = parseErrorMessage(errorBody, response.code)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val inputStream = response.body?.byteStream()
                ?: return@withContext Result.failure(Exception("Empty response stream"))

            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = null
                while (coroutineContext.isActive && reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (!currentLine.startsWith("data:")) continue

                    val rawData = currentLine.removePrefix("data:").trim()
                    if (rawData.isEmpty() || rawData == "[DONE]") continue

                    try {
                        val parsed = responseAdapter.fromJson(rawData)
                        val chunkText = parsed?.candidates?.firstOrNull()?.content?.parts
                            ?.mapNotNull { it.text }
                            ?.joinToString("")

                        if (!chunkText.isNullOrEmpty()) {
                            fullTextBuilder.append(chunkText)
                            onChunk(chunkText)
                        }

                        parsed?.usageMetadata?.totalTokenCount?.let {
                            totalTokens = it
                        }
                    } catch (e: Exception) {
                        Log.w("GeminiApiService", "Failed to parse SSE chunk: $rawData", e)
                    }
                }
            }

            Result.success(Pair(fullTextBuilder.toString(), totalTokens))
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Stream error", e)
            Result.failure(e)
        }
    }

    private fun sanitizeModel(model: String): String {
        return when (model) {
            "gemini-3.5-flash" -> "gemini-3.5-flash"
            "gemini-3.1-pro-preview" -> "gemini-3.1-pro-preview"
            "gemini-2.5-flash-image" -> "gemini-2.5-flash-image"
            "gemini-3.1-flash-lite-preview" -> "gemini-3.1-flash-lite-preview"
            else -> "gemini-3.5-flash"
        }
    }

    private fun parseErrorMessage(rawBody: String, httpCode: Int): String {
        return try {
            val json = JSONObject(rawBody)
            val errorObj = json.optJSONObject("error")
            val msg = errorObj?.optString("message") ?: "HTTP $httpCode"
            "Error ($httpCode): $msg"
        } catch (_: Exception) {
            "HTTP $httpCode: $rawBody"
        }
    }
}
