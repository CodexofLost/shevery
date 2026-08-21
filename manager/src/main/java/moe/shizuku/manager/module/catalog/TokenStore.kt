package moe.shizuku.manager.module.catalog

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException

object TokenStore {
    private const val PREFS_NAME = "catalog_token_prefs"
    private const val KEY_GITHUB_PAT = "github_pat"
    private const val TAG = "TokenStore"
    private const val ENCRYPTED_PREFS_NAME_SUFFIX = ".enc"

    @Volatile
    private var cachedPrefs: SharedPreferences? = null

    private fun createEncryptedPrefs(appContext: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME + ENCRYPTED_PREFS_NAME_SUFFIX,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Migration cleanup: on first launch after this fix is deployed, any stale .enc
     * file from a prior partial/broken migration is removed so we start clean.
     * This is ONLY called during migration exception recovery — NOT on the normal
     * getOrCreateEncryptedPrefs success path, to avoid deleting the active token.
     */
    private fun cleanupStaleEncrypedPrefs(appContext: Context) {
        try {
            appContext.deleteSharedPreferences(PREFS_NAME + ENCRYPTED_PREFS_NAME_SUFFIX)
        } catch (_: Exception) {
            // file may not exist — ignore
        }
    }

    private fun getOrCreateEncryptedPrefs(context: Context): SharedPreferences {
        val appContext = context.applicationContext
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return try {
            // Normal path: just create (or open existing) the encrypted prefs.
            // DO NOT delete here — that would lose the persisted token.
            createEncryptedPrefs(appContext, masterKey)
        } catch (e: GeneralSecurityException) {
            Log.w(TAG, "EncryptedSharedPreferences creation failed (keystore), migrating: ${e.message}")
            migrateAndRecreate(appContext, masterKey)
        } catch (e: IOException) {
            Log.w(TAG, "EncryptedSharedPreferences creation failed (IO), migrating: ${e.message}")
            migrateAndRecreate(appContext, masterKey)
        }
    }

    /**
     * Atomic-ish migration: read the legacy plaintext token, create the new encrypted
     * prefs on a SEPARATE filename (so the legacy plaintext file is never clobbered),
     * copy the token in, then delete the legacy plaintext file. If any step fails,
     * the legacy file (and the token) survives.
     */
    private fun migrateAndRecreate(appContext: Context, masterKey: MasterKey): SharedPreferences {
        // 1) Best-effort read of the legacy plaintext token
        var legacyToken: String? = null
        try {
            val plain = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            legacyToken = plain.getString(KEY_GITHUB_PAT, null)
        } catch (_: Exception) {
            // ignore — best-effort migration
        }

        // 2) Clean any stale .enc file from a prior partial migration, then create fresh
        cleanupStaleEncrypedPrefs(appContext)
        val fresh = try {
            createEncryptedPrefs(appContext, masterKey)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create new encrypted prefs; legacy file kept", e)
            throw e
        }

        // 3) Copy token into encrypted prefs BEFORE deleting legacy file
        if (!legacyToken.isNullOrBlank()) {
            try {
                fresh.edit().putString(KEY_GITHUB_PAT, legacyToken).apply()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to persist migrated token into encrypted prefs", e)
                throw e
            }
        }

        // 4) Only now invalidate cache + delete the legacy plaintext file
        synchronized(this) { cachedPrefs = null }
        try {
            appContext.deleteSharedPreferences(PREFS_NAME)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete legacy prefs after migration (non-fatal)", e)
        }
        return fresh
    }

    private fun getCachedPrefs(context: Context): SharedPreferences {
        return cachedPrefs ?: synchronized(this) {
            cachedPrefs ?: getOrCreateEncryptedPrefs(context).also { cachedPrefs = it }
        }
    }

    fun getToken(context: Context): String? {
        return getCachedPrefs(context).getString(KEY_GITHUB_PAT, null)
    }

    fun setToken(context: Context, token: String) {
        getCachedPrefs(context).edit().putString(KEY_GITHUB_PAT, token).apply()
    }

    fun clearToken(context: Context) {
        getCachedPrefs(context).edit().remove(KEY_GITHUB_PAT).apply()
    }

    fun isValidTokenFormat(token: String): Boolean {
        val trimmed = token.trim()
        return trimmed.length >= 30 &&
                (trimmed.startsWith("ghp_") || trimmed.startsWith("github_pat_"))
    }
}
