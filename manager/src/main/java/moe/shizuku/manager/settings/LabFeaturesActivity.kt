package moe.shizuku.manager.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import moe.shizuku.manager.R
import moe.shizuku.manager.app.AppActivity
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.service.WatchdogManager
import moe.shizuku.manager.ui.compose.GroupDivider
import moe.shizuku.manager.ui.compose.SettingsGroup
import moe.shizuku.manager.ui.compose.SettingsRow
import moe.shizuku.manager.ui.compose.ShizukuExpressiveTheme
import moe.shizuku.manager.ui.compose.ShizukuLazyScaffold
import moe.shizuku.manager.ui.compose.SwitchSettingsRow

class LabFeaturesActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var connectorEnabled by remember { mutableStateOf(ModuleSettings.isConnectorEnabled()) }
            var dhizukuEnabled by remember { mutableStateOf(ModuleSettings.isDhizukuEnabled()) }
            var verboseLogging by remember { mutableStateOf(ModuleSettings.isVerboseLogging()) }
            var notifyDeath by remember { mutableStateOf(ModuleSettings.isNotifyOnServiceDeath()) }
            var showUnsafeDialog by remember { mutableStateOf(false) }
            var showDhizukuDialog by remember { mutableStateOf(false) }

            ShizukuExpressiveTheme {
                ShizukuLazyScaffold(
                    title = stringResource(R.string.lab_features_title),
                    onNavigateUp = { finish() }
                ) {
                    item {
                        SettingsGroup(title = stringResource(R.string.lab_features_summary)) {
                            SwitchSettingsRow(
                                icon = R.drawable.ic_baseline_link_24,
                                title = stringResource(R.string.shizuku_connectors_title),
                                summary = stringResource(R.string.shizuku_connectors_summary),
                                checked = connectorEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showUnsafeDialog = true
                                    } else {
                                        connectorEnabled = false
                                        ModuleSettings.setConnectorEnabled(false)
                                    }
                                }
                            )
                            GroupDivider()
                            SwitchSettingsRow(
                                icon = R.drawable.ic_outline_info_24,
                                title = stringResource(R.string.dhizuku_mode_title),
                                summary = stringResource(R.string.dhizuku_mode_summary),
                                checked = dhizukuEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        showDhizukuDialog = true
                                    } else {
                                        dhizukuEnabled = false
                                        ModuleSettings.setDhizukuEnabled(false)
                                    }
                                }
                            )
                        }
                    }

                    item {
                        SettingsGroup(title = stringResource(R.string.lab_service_behavior_title)) {
                            SwitchSettingsRow(
                                icon = R.drawable.ic_outline_notifications_active_24,
                                title = stringResource(R.string.lab_notify_death_title),
                                summary = stringResource(R.string.lab_notify_death_summary),
                                checked = notifyDeath,
                                onCheckedChange = { value ->
                                    notifyDeath = value
                                    ModuleSettings.setNotifyOnServiceDeath(value)
                                }
                            )
                        }
                    }

                    item {
                        SettingsGroup(title = stringResource(R.string.lab_debugging_title)) {
                            SwitchSettingsRow(
                                icon = R.drawable.ic_adb_24dp,
                                title = stringResource(R.string.lab_verbose_logging_title),
                                summary = stringResource(R.string.lab_verbose_logging_summary),
                                checked = verboseLogging,
                                onCheckedChange = { value ->
                                    verboseLogging = value
                                    ModuleSettings.setVerboseLogging(value)
                                }
                            )
                        }
                    }
                }

                if (showUnsafeDialog) {
                    AlertDialog(
                        onDismissRequest = { showUnsafeDialog = false },
                        title = { Text(stringResource(R.string.unsafe_warning_title)) },
                        text = { Text(stringResource(R.string.unsafe_warning_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showUnsafeDialog = false
                                connectorEnabled = true
                                ModuleSettings.setConnectorEnabled(true)
                            }) {
                                Text(stringResource(android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showUnsafeDialog = false }) {
                                Text(stringResource(android.R.string.cancel))
                            }
                        }
                    )
                }

                if (showDhizukuDialog) {
                    AlertDialog(
                        onDismissRequest = { showDhizukuDialog = false },
                        title = { Text(stringResource(R.string.dhizuku_warning_title)) },
                        text = { Text(stringResource(R.string.dhizuku_warning_message)) },
                        confirmButton = {
                            TextButton(onClick = {
                                showDhizukuDialog = false
                                dhizukuEnabled = true
                                ModuleSettings.setDhizukuEnabled(true)
                            }) {
                                Text(stringResource(android.R.string.ok))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDhizukuDialog = false }) {
                                Text(stringResource(android.R.string.cancel))
                            }
                        }
                    )
                }


            }
        }
    }
}
