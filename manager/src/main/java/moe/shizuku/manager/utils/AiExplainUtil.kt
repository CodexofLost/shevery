package moe.shizuku.manager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.commandium.AiProviderRepository
import moe.shizuku.manager.module.ModuleSettings

object AiExplainUtil {

    private const val GOOGLE_OPENAI_BASE = "https://generativelanguage.googleapis.com/v1beta/openai/"

    /**
     * Model is taken exclusively from the active provider (explicit pick in the
     * AI switcher / provider dialog). The old single "comput_ai_model" setting
     * held Gemini-era flat slugs that 404 on OpenAI-style endpoints after the
     * upgrade (OpenRouter: "Model not found: gemini-3.6-flash"), so it is no
     * longer consulted — a blank model yields a clear message instead.
     */
    private fun resolveModel(baseUrl: String): String =
        if (baseUrl.startsWith(GOOGLE_OPENAI_BASE)) "gemini-3.6-flash" else ""

    suspend fun explainFailure(
        contextStr: String,
        inputDetail: String,
        outputLog: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {
        val active = AiProviderRepository.getActive()
        val baseUrl = active?.baseUrl?.takeIf { it.isNotBlank() }
            ?: ModuleSettings.getComputAiBaseUrl()
        val model = active?.model?.takeIf { it.isNotBlank() } ?: resolveModel(baseUrl)
        if (model.isBlank()) {
            return@withContext "No AI model is selected. Open the AI switcher (console → Ask AI) and pick a model."
        }
        val resolvedKey = active?.let { p -> AiProviderRepository.getKey(p.id).takeIf { it.isNotBlank() } }
            ?: apiKey
        if (resolvedKey.isBlank()) {
            return@withContext "API key is empty! Please configure it in Shevery Settings."
        }

        val currentLocale = java.util.Locale.getDefault()
        val prompt = "CRITICAL: You must write the entire explanation in the following language: ${currentLocale.getDisplayName(java.util.Locale.ENGLISH)} (locale code: ${currentLocale.toLanguageTag()}).\n\n" +
                "An error or failure occurred in the application context: $contextStr.\n" +
                "Input / Action details:\n$inputDetail\n\n" +
                "Output / Error Log:\n$outputLog\n\n" +
                "Explain this failure in a clear, concise, and helpful developer-focused way, and suggest how to resolve it."

        val result = AiClient.chatCompletion(
            baseUrl = baseUrl,
            apiKey = resolvedKey,
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
        val active = AiProviderRepository.getActive()
        val baseUrl = active?.baseUrl?.takeIf { it.isNotBlank() }
            ?: ModuleSettings.getComputAiBaseUrl()
        val model = active?.model?.takeIf { it.isNotBlank() } ?: resolveModel(baseUrl)
        if (model.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No AI model is selected. Open the AI switcher (console → Ask AI) and pick a model."))
        }
        val resolvedKey = active?.let { p -> AiProviderRepository.getKey(p.id).takeIf { it.isNotBlank() } }
            ?: apiKey
        if (resolvedKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("API key is empty! Please configure it in Shevery Settings."))
        }

        val requestPrompt = "You are a shell command assistant for an Android device.\n" +
                "This shell is the device's privileged shell served by Shizuku/Shevery; it runs Android's toybox " +
                "with commands such as pm, am, dumpsys, settings, cmd, service, getprop, toybox, run-as and standard " +
                "text utilities. Linux-host commands like apt, dpkg, systemctl, journalctl, ifconfig, iptables are " +
                "NOT available and must never be used.\n" +
                "Generate a single shell command that fulfills the user's request on this Android device. " +
                "If the request is about apps, prefer pm/am/dumpsys. If it is about device state, prefer " +
                "dumpsys/settings/getprop.\n" +
                "CRITICAL: Return ONLY the raw shell command, without any markdown formatting (do not wrap in ``` or `), " +
                "explanations, or trailing text. The output should be directly executable in a shell.\n\n" +
                "Prompt: $prompt"

        return@withContext AiClient.chatCompletion(
            baseUrl = baseUrl,
            apiKey = resolvedKey,
            model = model,
            systemPrompt = null,
            userPrompt = requestPrompt
        ).map { CommandSanitizer.sanitize(it) }
    }
}
