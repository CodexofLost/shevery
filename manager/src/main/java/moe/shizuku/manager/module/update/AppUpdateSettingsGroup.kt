@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package moe.shizuku.manager.module.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.shizuku.manager.BuildConfig
import moe.shizuku.manager.R
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.ui.compose.GroupDivider
import moe.shizuku.manager.ui.compose.SettingsGroup
import moe.shizuku.manager.ui.compose.SettingsRow
import moe.shizuku.manager.ui.compose.SwitchSettingsRow

@Composable
fun AppUpdateSettingsGroup() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var appUpdateChannel by remember { mutableStateOf(ModuleSettings.getAppUpdateChannel()) }
    var appAutoCheck by remember { mutableStateOf(ModuleSettings.isAppUpdateAutoCheckEnabled()) }
    var appUpdateFrequency by remember { mutableStateOf(ModuleSettings.getAppUpdateFrequency()) }
    var isCheckingAppUpdate by remember { mutableStateOf(false) }
    var appUpdateResult by remember { mutableStateOf<SheveryAppUpdateResult?>(null) }

    SettingsGroup(title = stringResource(R.string.shevery_update_group_title)) {
        SettingsRow(
            icon = R.drawable.ic_server_restart,
            title = stringResource(R.string.shevery_update_check_title),
            summary = stringResource(R.string.shevery_update_check_summary) + " (${BuildConfig.VERSION_NAME})",
            onClick = {
                scope.launch {
                    isCheckingAppUpdate = true
                    appUpdateResult = SheveryUpdateChecker.getInstance().checkAppUpdate(context)
                    isCheckingAppUpdate = false
                }
            }
        )
        GroupDivider()
        AppUpdateChannelDropdown(
            selected = appUpdateChannel,
            onSelect = {
                appUpdateChannel = it
                ModuleSettings.setAppUpdateChannel(it)
            }
        )
        GroupDivider()
        SwitchSettingsRow(
            icon = R.drawable.ic_outline_notifications_active_24,
            title = stringResource(R.string.shevery_update_auto_check),
            summary = stringResource(R.string.shevery_update_auto_check_summary),
            checked = appAutoCheck,
            onCheckedChange = {
                appAutoCheck = it
                ModuleSettings.setAppUpdateAutoCheckEnabled(it)
            }
        )
        GroupDivider()
        UpdateFrequencyDropdownForApp(
            selected = appUpdateFrequency,
            onSelect = {
                appUpdateFrequency = it
                ModuleSettings.setAppUpdateFrequency(it)
            }
        )
    }

    if (isCheckingAppUpdate) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.shevery_update_check_title)) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    Text(stringResource(R.string.shevery_update_checking))
                }
            },
            confirmButton = {}
        )
    }

    appUpdateResult?.let { result ->
        SheveryAppUpdateDialog(
            result = result,
            onDismiss = { appUpdateResult = null }
        )
    }
}

@Composable
private fun AppUpdateChannelDropdown(
    selected: ModuleSettings.AppUpdateChannel,
    onSelect: (ModuleSettings.AppUpdateChannel) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val entries = ModuleSettings.AppUpdateChannel.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        SettingsRow(
            icon = R.drawable.ic_outline_info_24,
            title = stringResource(R.string.shevery_update_channel),
            summary = stringResource(selected.labelRes),
            onClick = { expanded = true },
            trailing = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEach { channel ->
                DropdownMenuItem(
                    text = { Text(stringResource(channel.labelRes)) },
                    onClick = {
                        onSelect(channel)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UpdateFrequencyDropdownForApp(
    selected: ModuleSettings.UpdateFrequency,
    onSelect: (ModuleSettings.UpdateFrequency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        SettingsRow(
            icon = R.drawable.ic_outline_notifications_active_24,
            title = stringResource(R.string.update_settings_frequency_label),
            summary = stringResource(when (selected) {
                ModuleSettings.UpdateFrequency.MANUAL -> R.string.update_settings_frequency_manual
                ModuleSettings.UpdateFrequency.DAILY -> R.string.update_settings_frequency_daily
                ModuleSettings.UpdateFrequency.WEEKLY -> R.string.update_settings_frequency_weekly
            }),
            onClick = { expanded = true },
            trailing = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.update_settings_frequency_manual)) },
                onClick = {
                    onSelect(ModuleSettings.UpdateFrequency.MANUAL)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.update_settings_frequency_daily)) },
                onClick = {
                    onSelect(ModuleSettings.UpdateFrequency.DAILY)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.update_settings_frequency_weekly)) },
                onClick = {
                    onSelect(ModuleSettings.UpdateFrequency.WEEKLY)
                    expanded = false
                }
            )
        }
    }
}
