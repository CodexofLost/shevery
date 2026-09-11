# Multi-Provider AI (OpenAI-Compatible) for Comput Implementation Plan

> **For Hermes:** Implement task-by-task via `delegate_task`, one task per child, sequential dispatch with per-task provider/model pins (see delegation-pins skill).

**Goal:** Replace the Gemini-only AI wiring in Comput with a provider abstraction so users can pick OpenAI, DeepSeek, OpenRouter, Groq, Gemini, or any OpenAI-compatible endpoint with their own key and model.

**Architecture:** One user-defined provider (name + base URL + key + model) plus an optional "fetch models" action that lists the endpoint's models for picking instead of typing. A single `AiClient.kt` (OkHttp, already `implementation 'com.squareup.okhttp3:okhttp:4.12.0'` in `manager/build.gradle:233`) serves every Bearer-style OpenAI-compatible endpoint. `AiExplainUtil` keeps its two public signatures and delegates to `AiClient`. Gemini's native `generateContent` endpoint is retired; Gemini is accessed via Google's OpenAI-compatible endpoint (`https://generativelanguage.googleapis.com/v1beta/openai/`), so one request shape serves every provider. Existing Keystore-encrypted key storage in `ModuleSettings` is reused. No hardcoded provider presets (they rot); the form pre-fills OpenRouter's URL as a starting hint.

**Tech Stack:** Kotlin, OkHttp 4.12.0, org.json (already used), SharedPreferences via `ShizukuSettings`, Jetpack Compose settings UI.

---

### Task 1: Add generic provider prefs to ModuleSettings

**Status: DONE (2026-09-11) — implemented in `ModuleSettings.kt` + committed as `feat(ai): generic provider prefs`.**

**Objective:** Store user-defined provider name, base URL, key, and model.

**Files:**
- Modify: `manager/src/main/java/moe/shizuku/manager/module/ModuleSettings.kt` (Comput Settings section, ~lines 291-380)

**Step 1: Add keys**

```kotlin
// Comput AI provider settings (generic OpenAI-compatible endpoint)
private const val KEY_COMPUT_AI_NAME = "comput_ai_name"
private const val KEY_COMPUT_AI_BASE_URL = "comput_ai_base_url"
private const val KEY_COMPUT_AI_MODEL = "comput_ai_model"
```

Reuse the existing Keystore `encrypt()`/`decrypt()` helpers and `KEY_COMPUT_API_KEY` as-is for the single key — no per-provider split needed since there is exactly one configured endpoint.

**Step 2: Add accessors**

```kotlin
fun getComputAiName(): String  // default ""
fun setComputAiName(value: String)
fun getComputAiBaseUrl(): String  // default "https://openrouter.ai/api/v1/"
fun setComputAiBaseUrl(value: String)  // trim trailing spaces; require http(s) scheme, reject blank
fun getComputAiModel(): String  // default = legacy gemini model if migrated, else ""
fun setComputAiModel(value: String)
```

**Step 3: Migration** — on first read, if legacy `comput_gemini_model` exists and `comput_ai_model` is unset: copy the model over, set name to "Gemini", set base URL to Google's OpenAI endpoint, and keep the existing encrypted key untouched. Keep old `getComputGeminiModel` delegating (deprecated) so nothing breaks mid-refactor.

**Step 4: Verify** — no compiler check available on-box (no local builds per project rule); re-read the edited section for syntax. Commit.

```bash
git add manager/src/main/java/moe/shizuku/manager/module/ModuleSettings.kt
git commit -m "feat(ai): generic provider prefs with gemini migration"
```

---

### Task 2: Create AiClient.kt (chat completion + model list)

**Objective:** One object with two functions: post chat completions, and list models from the endpoint.

**Files:**
- Create: `manager/src/main/java/moe/shizuku/manager/utils/AiClient.kt`

**Step 1: Write the client**

```kotlin
package moe.shizuku.manager.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AiClient {
    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun chatCompletion(
        baseUrl: String,
        apiKey: String,
        model: String,
        systemPrompt: String?,
        userPrompt: String,
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext Result.failure(IllegalStateException("empty_key"))
        if (model.isBlank()) return@withContext Result.failure(IllegalStateException("empty_model"))
        try {
            val messages = JSONArray()
            if (!systemPrompt.isNullOrBlank()) {
                messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
            }
            messages.put(JSONObject().put("role", "user").put("content", userPrompt))
            val body = JSONObject()
                .put("model", model)
                .put("messages", messages)
                .toString()
                .toRequestBody("application/json".toMediaType())
            val url = baseUrl.trimEnd('/') + "/chat/completions"
            val reqBuilder = Request.Builder().url(url).post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $apiKey")
            // OpenRouter attribution (optional, harmless elsewhere)
            if (baseUrl.contains("openrouter.ai")) {
                reqBuilder.header("HTTP-Referer", "https://github.com/HmnDev-Tech/shevery")
                    .header("X-Title", "Shevery")
            }
            val resp = http.newCall(reqBuilder.build()).execute()
            resp.use {
                val text = it.body?.string().orEmpty()
                if (!it.isSuccessful) {
                    return@withContext Result.failure(RuntimeException("HTTP ${it.code}: $text"))
                }
                val content = JSONObject(text)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                Result.success(content.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Step 2: Add listModels**

```kotlin
suspend fun listModels(baseUrl: String, apiKey: String): Result<List<String>> =
    withContext(Dispatchers.IO) {
        try {
            val url = baseUrl.trimEnd('/') + "/models"
            val req = Request.Builder().url(url)
                .header("Authorization", "Bearer $apiKey")
                .get()
                .build()
            http.newCall(req).execute().use {
                val text = it.body?.string().orEmpty()
                if (!it.isSuccessful) {
                    return@withContext Result.failure(RuntimeException("HTTP ${it.code}: $text"))
                }
                val ids = mutableListOf<String>()
                val data = JSONObject(text).optJSONArray("data") ?: JSONArray()
                for (i in 0 until data.length()) {
                    data.optJSONObject(i)?.optString("id")?.takeIf { id -> id.isNotBlank() }?.let(ids::add)
                }
                Result.success(ids.sorted())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
```

Note: not every endpoint implements `/models` (returning 404 or an empty list is normal) — callers treat failure as "keep the free-text field", never as a blocking error.

**Step 3: Verify** — re-read for syntax (no local builds). Commit.

```bash
git add manager/src/main/java/moe/shizuku/manager/utils/AiClient.kt
git commit -m "feat(ai): OpenAI-compatible client with model listing"
```

---

### Task 3: Rewire AiExplainUtil onto AiClient

**Objective:** Keep `explainFailure()` and `generateCommand()` signatures; swap Gemini HTTP for `AiClient`.

**Files:**
- Modify: `manager/src/main/java/moe/shizuku/manager/utils/AiExplainUtil.kt`

**Step 1: Replace bodies** — each becomes: resolve provider/baseUrl/key/model from `ModuleSettings`, call `AiClient.chatCompletion()`, map `Result` to display string. Preserve the locale-prefix prompt in `explainFailure` and the raw-command-only prompt in `generateCommand` as system/user messages. Preserve existing user-visible error strings for empty key ("...API Key is empty...") so UI copy doesn't change.

**Step 2: Verify callers untouched** — `ComputScreen.kt:444-451,484-485,507-518` call the same signatures; `ModulesScreen.kt` model references must be updated to the new getter (see Task 4). Commit.

```bash
git add manager/src/main/java/moe/shizuku/manager/utils/AiExplainUtil.kt
git commit -m "feat(ai): route explain/generate through provider client"
```

---

### Task 4: Settings UI — provider form with fetch-models

**Objective:** Replace the Gemini-model dialog with a provider form: name, base URL, key, model (free text + fetch-list picker).

**Files:**
- Modify: `manager/src/main/java/moe/shizuku/manager/settings/SettingsScreen.kt` (comput state ~lines 195-206, restore block ~280-290, dialogs ~880+)
- Modify: `manager/src/main/java/moe/shizuku/manager/module/ModulesScreen.kt` (wherever `getComputGeminiModel` is referenced)

**Step 1: State** — replace `computGeminiModel`/`showGeminiModelDialog` state with `computAiName`, `computAiBaseUrl`, `computAiModel`, plus dialog flags. Restore block reads the new getters.

**Step 2: UI rows** — name field, base URL field (pre-filled hint `https://openrouter.ai/api/v1/`), API key uses the existing dialog unchanged, and a model row that works like this: whenever base URL + key are both present (on entering the screen and after either changes, debounced), the screen auto-calls `AiClient.listModels()` with a loading indicator. On success the model row is a dropdown of the returned ids. On failure the row shows a plain message ("This server doesn't share its model list — type the model name.") with a free-text field. No manual fetch button; typing always remains available as an override. API key uses the existing dialog unchanged.

**Step 3: Verify** — grep that no references to `getComputGeminiModel`/`setComputGeminiModel` remain outside the deprecated delegators. Commit.

```bash
git add manager/src/main/java/moe/shizuku/manager/settings/SettingsScreen.kt manager/src/main/java/moe/shizuku/manager/module/ModulesScreen.kt
git commit -m "feat(ai): provider form with model fetching"
```

---

### Task 5: End-to-end validation via CI build + tester

**Objective:** Prove all providers work on-device (no local builds per project rule).

- Push branch, open PR to `dev`, wait for `build` check green.
- Tester installs CI APK, sets OpenRouter key (one key covers many models) and runs: (a) ReCommand generate, (b) failed-command explain, (c) switches provider to Gemini with Google key and repeats.
- Validate: raw command with no markdown fences; explanation in device locale; sensible HTTP-error surfacing with a bad key (401 text, not a crash).
- Tester captures logcat filtered on the AI path if anything fails.

---

## Risks / Tradeoffs

- **Gemini endpoint change:** retiring native `generateContent` for Google's OpenAI-compat URL is the linchpin; if its behavior differs (model ids, system prompts), Gemini users regress. Mitigation: Task 5 tests Gemini explicitly; default model updated from `gemini-3.6-flash` to whatever is current at implementation time.
- **Stored legacy keys:** migration copies plaintext-era keys too — the existing encrypt-on-read path handles that, unchanged.
- **YAGNI, deferred:** no streaming (SSE), no model-list fetching, no image input, no per-provider system-prompt tuning. Streaming is the natural follow-up for terminal feel.
- **Key security:** keys live Keystore-encrypted as today; per-provider split doesn't weaken that. Never log keys; `AiClient` error strings include response bodies — verify no provider echoes the key (they don't).

## Open Questions

1. Own-keys-per-provider vs. a built-in default key — decision needed before Task 4 copy (current plan assumes own keys, matching today's Gemini behavior).
2. ~~Should Custom allow overriding headers (e.g. Azure OpenAI)?~~ DECIDED 2026-09-11: No — Bearer-only. Non-standard header providers (Azure) are out of scope unless users ask later.
