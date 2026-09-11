package moe.shizuku.manager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.module.ModuleSettings

object AiExplainUtil {

    private const val GOOGLE_OPENAI_BASE = "https://generativelanguage.googleapis.com/v1beta/openai/"
    private const val DEFAULT_GOOGLE_MODEL = "gemini-3.6-flash"

    private fun resolveModel(baseUrl: String): String {
        val model = ModuleSettings.getComputAiModel()
        if (model.isNotBlank()) return model
        return if (baseUrl.contains("generativelanguage.googleapis.com")) DEFAULT_GOOGLE_MODEL else ""
    }

    suspend fun explainFailure(
        contextStr: String,
        inputDetail: String,
        outputLog: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "API key is empty! Please configure it in Shevery Settings."
        }
        val baseUrl = ModuleSettings.getComputAiBaseUrl()
        val model = resolveModel(baseUrl)
        if (model.isBlank()) {
            return@withContext "AI model is not set. Please configure it in Shevery Settings."
        }

        val currentLocale = java.util.Locale.getDefault()
        val prompt = "CRITICAL: You must write the entire explanation in the following language: ${currentLocale.displayName} (locale code: ${currentLocale.toLanguageTag()}).\n\n" +
                "An error or failure occurred in the application context: $contextStr.\n" +
                "Input / Action details:\n$inputDetail\n\n" +
                "Output / Error Log:\n$outputLog\n\n" +
                "Explain this failure in a clear, concise, and helpful developer-focused way, and suggest how to resolve it."

        val result = AiClient.chatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = null,
            userPrompt = prompt
        )
        result.getOrElse { e ->
            "Failed to reach AI provider: ${e.message ?: "Connection error."}"
        }
    }

    suspend fun generateCommand(
        prompt: String,
        apiKey: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API key is empty! Please configure it in Shevery Settings."))
        }
        val baseUrl = ModuleSettings.getComputAiBaseUrl()
        val model = resolveModel(baseUrl)
        if (model.isBlank()) {
            return@withContext Result.failure(IllegalStateException("AI model is not set. Please configure it in Shevery Settings."))
        }

        val requestPrompt = "You are a shell command assistant. Generate a shell command based on the following user prompt.\n" +
                "CRITICAL: Return ONLY the raw shell command, without any markdown formatting (do not wrap in ``` or `), explanations, or trailing text. The output should be directly executable in a shell.\n\n" +
                "Prompt: $prompt"

        return@withContext AiClient.chatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model,
            systemPrompt = null,
            userPrompt = requestPrompt
        ).map { sanitizeCommand(it) }
    }

    private fun sanitizeCommand(raw: String): String {
        var t = raw.trim()
        t = REGEX_THINKING.replace(t, "")
        return REGEX_FENCE.replace(t) { m -> m.groupValues[1] }.trim()
    }

    private val REGEX_THINKING = Regex("(?is)<thinking>.*?</thinking>")
    private val REGEX_FENCE = Regex("(?is)^```[a-zA-Z0-9_-]*\\s*\\n(.*?)\\n```\\s*$")
}
