package com.example.calculator.data.gemini

import com.example.BuildConfig
import com.example.calculator.engine.OfflineMathEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class MathAssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val extractedAnswer: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    ASSISTANT,
    SYSTEM
}

class GeminiMathService {

    companion object {
        const val MODEL_OFFLINE = "offline-math-engine" // 100% On-Device, works with NO internet
        const val MODEL_PRO = "gemini-3.1-pro-preview"   // Complex math & STEM reasoning (cloud)
        const val MODEL_FLASH = "gemini-3.5-flash"        // Fast cloud assistant
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val AVAILABLE_MODELS = listOf(
            MODEL_OFFLINE to "Offline Math Engine (100% On-Device • No Internet)",
            MODEL_PRO to "Gemini 3.1 Pro (Cloud • Requires Internet)",
            MODEL_FLASH to "Gemini 3.5 Flash (Cloud • Fast)"
        )
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun askMathAssistant(
        prompt: String,
        calculatorContext: String? = null,
        conversationHistory: List<MathAssistantMessage> = emptyList(),
        model: String = MODEL_OFFLINE
    ): Result<Pair<String, String?>> = withContext(Dispatchers.IO) {
        // If offline mode is requested, or if no API key is available, run 100% offline
        if (model == MODEL_OFFLINE || !isApiKeyConfigured()) {
            val offlineResult = OfflineMathEngine.solve(prompt, calculatorContext)
            return@withContext Result.success(offlineResult)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY

        try {
            val url = "$BASE_URL/$model:generateContent?key=$apiKey"

            val systemInstructionJson = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", """
                            You are the Math Assistant embedded directly inside HuCalc, a high-precision Casio fx-991ES style scientific calculator app.
                            Your job is to:
                            1. Solve mathematical equations, word problems, calculus (derivatives, integrals), linear algebra, trigonometry, statistics, and science/physics calculations.
                            2. Provide clear step-by-step explanations, showing formulas and intermediate steps.
                            3. Whenever possible, conclude your response with a concise line in the format:
                               RESULT: <numerical or algebraic value>
                               (For example: 'RESULT: 42' or 'RESULT: 3.14159' or 'RESULT: 2x + 5'). This allows the user to insert your calculated result directly into HuCalc's calculator display.
                            4. If the user provides their current calculator expression or result, explain its derivation, check for syntax or calculation errors, or simplify it.
                            5. Keep explanations polite, educational, and formatting clean with markdown headings and bullet points.
                        """.trimIndent())
                    })
                }
                put("parts", parts)
            }

            val contentsArray = JSONArray()

            // Include relevant previous conversation turns (up to last 6 for context efficiency)
            val recentHistory = conversationHistory
                .filter { it.sender != MessageSender.SYSTEM }
                .takeLast(6)

            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val turnObj = JSONObject().apply {
                    put("role", role)
                    val turnParts = JSONArray().apply {
                        put(JSONObject().apply { put("text", msg.text) })
                    }
                    put("parts", turnParts)
                }
                contentsArray.put(turnObj)
            }

            // Current prompt turn
            val fullPromptText = buildString {
                if (!calculatorContext.isNullOrBlank()) {
                    appendLine("[Active Calculator Display State: $calculatorContext]")
                    appendLine()
                }
                append(prompt)
            }

            val currentTurnObj = JSONObject().apply {
                put("role", "user")
                val currentParts = JSONArray().apply {
                    put(JSONObject().apply { put("text", fullPromptText) })
                }
                put("parts", currentParts)
            }
            contentsArray.put(currentTurnObj)

            val rootJson = JSONObject().apply {
                put("systemInstruction", systemInstructionJson)
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2) // Low temperature for high precision math
                    put("topP", 0.95)
                })
            }

            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                // Network error: Fall back immediately to Offline Math Engine
                val offlineResult = OfflineMathEngine.solve(prompt, calculatorContext)
                val fallbackNote = offlineResult.first + "\n\n*(Solved via 100% Offline On-Device Engine)*"
                return@withContext Result.success(Pair(fallbackNote, offlineResult.second))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val offlineResult = OfflineMathEngine.solve(prompt, calculatorContext)
                return@withContext Result.success(offlineResult)
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    if (part.has("text")) {
                        textBuilder.append(part.getString("text"))
                    }
                }
            }

            val responseText = textBuilder.toString().ifBlank { "No content generated." }
            val extracted = extractResultValue(responseText)

            Result.success(Pair(responseText, extracted))
        } catch (e: Exception) {
            // If offline / no internet / connection timeout, seamlessly use Offline Math Engine!
            val offlineResult = OfflineMathEngine.solve(prompt, calculatorContext)
            val fallbackNote = offlineResult.first + "\n\n*(Solved via 100% Offline On-Device Engine)*"
            Result.success(Pair(fallbackNote, offlineResult.second))
        }
    }

    private fun extractResultValue(text: String): String? {
        // Look for RESULT: <value> pattern
        val matcher = Pattern.compile("(?i)RESULT:\\s*([^\n\r]+)").matcher(text)
        if (matcher.find()) {
            return matcher.group(1)?.trim()?.removePrefix("`")?.removeSuffix("`")?.trim()
        }
        // Also look for Answer: <value> on its own line
        val answerMatcher = Pattern.compile("(?i)(?:Final Answer|Answer):\\s*\\$?([0-9a-zA-Z.+\\-*/^()√π\\s]+)\\$?").matcher(text)
        if (answerMatcher.find()) {
            val candidate = answerMatcher.group(1)?.trim()
            if (!candidate.isNullOrBlank() && candidate.length < 50) {
                return candidate.removePrefix("`")?.removeSuffix("`")?.trim()
            }
        }
        return null
    }
}
