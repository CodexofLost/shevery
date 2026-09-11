package moe.shizuku.manager.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import moe.shizuku.manager.R
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.utils.AiClient

private fun urlIsValid(url: String): Boolean =
    url.startsWith("http://") || url.startsWith("https://")

@Composable
fun AiProviderDialog(
    currentName: String,
    currentBaseUrl: String,
    currentModel: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    var baseUrl by remember { mutableStateOf(currentBaseUrl) }
    var model by remember { mutableStateOf(currentModel) }
    var apiKey by remember { mutableStateOf(ModuleSettings.getComputApiKey()) }
    var keyVisible by remember { mutableStateOf(false) }
    var modelOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelsUnavailable by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var baseUrlError by remember { mutableStateOf(false) }

    LaunchedEffect(baseUrl.trim(), apiKey.trim()) {

        val url = baseUrl.trim()
        val key = apiKey.trim()
        if (url.isEmpty() || key.isEmpty()) {
            modelOptions = emptyList()
            loadingModels = false
            modelsUnavailable = false
            return@LaunchedEffect
        }
        loadingModels = true
        modelsUnavailable = false
        delay(400)
        AiClient.listModels(url, key)
            .onSuccess { list ->
                modelOptions = list
                loadingModels = false
            }
            .onFailure {
                modelOptions = emptyList()
                modelsUnavailable = true
                loadingModels = false
            }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.comput_ai_name_label)) },
                    placeholder = { Text(stringResource(R.string.comput_ai_name_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = {
                        baseUrl = it
                        baseUrlError = false
                    },
                    label = { Text(stringResource(R.string.comput_ai_base_url_label)) },
                    placeholder = { Text(stringResource(R.string.comput_ai_base_url_placeholder)) },
                    singleLine = true,
                    isError = baseUrlError,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.comput_api_key_label)) },
                    singleLine = true,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { keyVisible = !keyVisible }) {
                            Text(
                                text = if (keyVisible) stringResource(R.string.comput_hide_api_key) else stringResource(R.string.comput_show_api_key),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Box {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text(stringResource(R.string.comput_ai_model_label)) },
                        placeholder = { Text(stringResource(R.string.comput_ai_model_placeholder)) },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { menuExpanded = !menuExpanded }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (modelOptions.isNotEmpty()) {
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            modelOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        model = option
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (loadingModels) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.comput_ai_model_loading),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (modelsUnavailable) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.comput_ai_model_unavailable),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedUrl = baseUrl.trim()
                    if (trimmedUrl.isNotEmpty() && !urlIsValid(trimmedUrl)) {
                        baseUrlError = true
                    } else {
                        baseUrlError = false
                        ModuleSettings.setComputApiKey(apiKey.trim())
                        onSave(name.trim(), baseUrl.trim(), model.trim())
                    }
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun computProviderSummary(name: String, model: String): String {
    return when {
        name.isBlank() -> stringResource(R.string.comput_ai_provider_not_configured)
        model.isBlank() -> name
        else -> name + " - " + model
    }
}