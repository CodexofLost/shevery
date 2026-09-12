package moe.shizuku.manager.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.shizuku.manager.R
import moe.shizuku.manager.commandium.AiProvider
import moe.shizuku.manager.commandium.AiProviderRepository
import moe.shizuku.manager.ui.compose.GroupDivider
import moe.shizuku.manager.ui.compose.SettingsGroup
import moe.shizuku.manager.ui.compose.SettingsRow
import moe.shizuku.manager.ui.compose.ShizukuExpressiveTheme
import moe.shizuku.manager.ui.compose.ShizukuLazyScaffold
import moe.shizuku.manager.utils.AiClient

/**
 * Full-screen AI provider manager. Lists every configured provider, marks the
 * active one, and offers add (via presets or custom), edit, delete with
 * confirmation, and per-provider connection testing.
 *
 * All buttons stay enabled and labeled so TalkBack never hits a dead end; the
 * delete affordance is hidden (not disabled) when only one provider remains.
 */
@Composable
fun AiManagerScreen(
    onNavigateUp: () -> Unit,
    onChanged: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var providers by remember { mutableStateOf(AiProviderRepository.getProviders()) }
    var activeId by remember { mutableStateOf(AiProviderRepository.getActiveId()) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AiProvider?>(null) }
    var editingKey by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<AiProvider?>(null) }
    var testStatus by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var testingId by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        providers = AiProviderRepository.getProviders()
        activeId = AiProviderRepository.getActiveId()
        onChanged()
    }

    fun runTest(provider: AiProvider) {
        scope.launch {
            testingId = provider.id
            testStatus = testStatus - provider.id
            val key = AiProviderRepository.getKey(provider.id)
            if (key.isBlank()) {
                testStatus = testStatus + (provider.id to "nokey")
            } else {
                AiClient.listModels(provider.baseUrl, key)
                    .onSuccess { list -> testStatus = testStatus + (provider.id to "ok:" + list.size) }
                    .onFailure { e -> testStatus = testStatus + (provider.id to "fail:" + (e.message ?: "")) }
            }
            testingId = null
        }
    }

    ShizukuExpressiveTheme {
        ShizukuLazyScaffold(
            title = stringResource(R.string.comput_ai_manager_title),
            onNavigateUp = onNavigateUp,
            actions = {
                TextButton(onClick = { showAdd = true }) {
                    Text(stringResource(R.string.comput_ai_add_provider))
                }
            }
        ) {
            item {
                SettingsGroup(title = stringResource(R.string.comput_ai_manager_title)) {
                    providers.forEachIndexed { index, provider ->
                        if (index > 0) GroupDivider()
                        val isActive = provider.id == activeId
                        val hasKey = AiProviderRepository.getKey(provider.id).isNotBlank()
                        val summary = buildString {
                            if (provider.model.isNotBlank()) append(provider.model)
                            if (provider.model.isNotBlank()) append(" - ")
                            append(
                                if (hasKey) "key saved" else "no key"
                            )
                        }
                        SettingsRow(
                            icon = null,
                            title = provider.name.ifBlank { provider.baseUrl },
                            summary = summary,
                            stateDescription = if (isActive) "active" else null,
                            onClick = {
                                AiProviderRepository.setActive(provider.id)
                                refresh()
                            },
                            trailing = {
                                RadioButton(
                                    selected = isActive,
                                    onClick = {
                                        AiProviderRepository.setActive(provider.id)
                                        refresh()
                                    }
                                )
                            }
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { runTest(provider) }) {
                                Text(stringResource(R.string.comput_ai_test_connection))
                            }
                            TextButton(onClick = {
                                editing = provider
                                editingKey = AiProviderRepository.getKey(provider.id)
                            }) {
                                Text(stringResource(R.string.comput_ai_edit_provider))
                            }
                            if (providers.size > 1) {
                                TextButton(onClick = { deleteTarget = provider }) {
                                    Text(stringResource(R.string.comput_ai_delete_provider))
                                }
                            }
                        }
                        val status = testStatus[provider.id]
                        val testing = testingId == provider.id
                        if (testing || status != null) {
                            Text(
                                text = when {
                                    testing -> stringResource(R.string.comput_ai_test_testing)
                                    status == "nokey" -> stringResource(R.string.comput_ai_test_no_key)
                                    status != null && status.startsWith("ok:") ->
                                        stringResource(
                                            R.string.comput_ai_test_ok,
                                            status.removePrefix("ok:").toIntOrNull() ?: 0
                                        )
                                    status != null && status.startsWith("fail:") ->
                                        stringResource(
                                            R.string.comput_ai_test_fail,
                                            status.removePrefix("fail:")
                                        )
                                    else -> ""
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AiProviderDialog(
            currentName = "",
            currentBaseUrl = "",
            currentModel = "",
            currentApiKey = "",
            onDismiss = { showAdd = false },
            onSave = { name, baseUrl, model, apiKey ->
                val created = AiProviderRepository.add(name, baseUrl, model)
                AiProviderRepository.setKey(created.id, apiKey)
                showAdd = false
                refresh()
            }
        )
    }

    val target = editing
    if (target != null) {
        AiProviderDialog(
            currentName = target.name,
            currentBaseUrl = target.baseUrl,
            currentModel = target.model,
            currentApiKey = editingKey,
            onDismiss = { editing = null },
            onSave = { name, baseUrl, model, apiKey ->
                AiProviderRepository.update(target.copy(name = name, baseUrl = baseUrl, model = model))
                AiProviderRepository.setKey(target.id, apiKey)
                editing = null
                refresh()
            }
        )
    }

    val doomed = deleteTarget
    if (doomed != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.comput_ai_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.comput_ai_delete_confirm_text,
                        doomed.name.ifBlank { doomed.baseUrl }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    AiProviderRepository.remove(doomed.id)
                    deleteTarget = null
                    refresh()
                }) {
                    Text(stringResource(R.string.comput_ai_delete_provider))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
