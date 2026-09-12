package moe.shizuku.manager.commandium

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.json.Json
import moe.shizuku.manager.ShizukuSettings
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Prefs-backed store for the user's AI providers plus the active selection.
 *
 * Each provider's API key lives in its own Keystore-encrypted pref entry
 * (comput_api_key_<id>), never in the provider JSON list.
 *
 * First access migrates the legacy single-provider prefs (name/url/model/key,
 * themselves already migrated from the old Gemini-only prefs) into one
 * provider entry, so all existing call sites keep working untouched.
 */
object AiProviderRepository {

    private const val KEY_PROVIDERS = "comput_ai_providers"
    private const val KEY_ACTIVE_ID = "comput_ai_active_id"
    private const val KEY_API_KEY_PREFIX = "comput_api_key_"

    internal const val LEGACY_API_KEY = "comput_api_key"
    internal const val LEGACY_NAME = "comput_ai_name"
    internal const val LEGACY_BASE_URL = "comput_ai_base_url"
    internal const val LEGACY_MODEL = "comput_ai_model"
    internal const val LEGACY_DEFAULT_BASE_URL = "https://openrouter.ai/api/v1/"

    private const val PROVIDER = "AndroidKeyStore"
    private const val ALIAS = "SheveryGeminiKey"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val json = Json { ignoreUnknownKeys = true }

    // -- provider list ---------------------------------------------------

    fun getProviders(): List<AiProvider> {
        migrateFromLegacyIfNeeded()
        val raw = prefs().getString(KEY_PROVIDERS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<AiProvider>>(raw)
        } catch (e: Throwable) {
            emptyList()
        }
    }

    private fun saveProviders(providers: List<AiProvider>) {
        prefs().edit().putString(KEY_PROVIDERS, json.encodeToString(providers)).apply()
    }

    fun getActiveId(): String? {
        migrateFromLegacyIfNeeded()
        return prefs().getString(KEY_ACTIVE_ID, null)
    }

    fun getActive(): AiProvider? {
        val providers = getProviders()
        if (providers.isEmpty()) return null
        val activeId = getActiveId()
        return providers.firstOrNull { it.id == activeId } ?: providers[0]
    }

    fun setActive(id: String) {
        prefs().edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun add(name: String, baseUrl: String, model: String): AiProvider {
        val provider = AiProvider(
            id = UUID.randomUUID().toString(),
            name = name,
            baseUrl = baseUrl.trim(),
            model = model,
        )
        val updated = getProviders() + provider
        saveProviders(updated)
        if (updated.size == 1) setActive(provider.id)
        return provider
    }

    fun update(provider: AiProvider) {
        saveProviders(getProviders().map { if (it.id == provider.id) provider else it })
    }

    /** Removes a provider. Refuses to remove the last one; returns false then. */
    fun remove(id: String): Boolean {
        val providers = getProviders()
        if (providers.size <= 1) return false
        saveProviders(providers.filterNot { it.id == id })
        prefs().edit().remove(KEY_API_KEY_PREFIX + id).apply()
        if (getActiveId() == id) {
            getProviders().firstOrNull()?.let { setActive(it.id) }
        }
        return true
    }

    // -- per-provider keys (Keystore-encrypted, same scheme as before) ---

    fun getKey(id: String): String {
        val raw = prefs().getString(KEY_API_KEY_PREFIX + id, "") ?: ""
        if (raw.isEmpty()) return ""
        if (!raw.contains(":")) {
            // Plain text from a fresh save that failed to encrypt; re-encrypt now.
            try {
                val encrypted = encrypt(raw)
                prefs().edit().putString(KEY_API_KEY_PREFIX + id, encrypted).apply()
                return raw
            } catch (e: Throwable) {
                return raw
            }
        }
        return try {
            decrypt(raw)
        } catch (e: Throwable) {
            ""
        }
    }

    fun setKey(id: String, value: String) {
        val encrypted = try {
            encrypt(value)
        } catch (e: Throwable) {
            value
        }
        prefs().edit().putString(KEY_API_KEY_PREFIX + id, encrypted).apply()
    }

    fun getActiveKey(): String {
        val active = getActive() ?: return ""
        return getKey(active.id)
    }

    fun setActiveKey(value: String) {
        val active = getActive() ?: return
        setKey(active.id, value)
    }

    // -- legacy migration --------------------------------------------------

    private fun migrateFromLegacyIfNeeded() {
        val prefs = prefs()
        if (prefs.contains(KEY_PROVIDERS)) return
        val name = prefs.getString(LEGACY_NAME, "") ?: ""
        val baseUrl = prefs.getString(LEGACY_BASE_URL, LEGACY_DEFAULT_BASE_URL)
            ?: LEGACY_DEFAULT_BASE_URL
        val model = prefs.getString(LEGACY_MODEL, "") ?: ""
        val provider = AiProvider(
            id = UUID.randomUUID().toString(),
            name = name,
            baseUrl = baseUrl,
            model = model,
        )
        saveProviders(listOf(provider))
        prefs.edit().putString(KEY_ACTIVE_ID, provider.id).apply()
        // Carry the existing encrypted key onto the new entry untouched.
        val legacyKey = prefs.getString(LEGACY_API_KEY, "") ?: ""
        if (legacyKey.isNotEmpty()) {
            prefs.edit().putString(KEY_API_KEY_PREFIX + provider.id, legacyKey).apply()
        }
    }

    // -- keystore helpers (moved verbatim from ModuleSettings) -------------

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(PROVIDER).apply { load(null) }
        val key = keyStore.getKey(ALIAS, null) as? SecretKey
        if (key != null) return key

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
          
...[truncated]