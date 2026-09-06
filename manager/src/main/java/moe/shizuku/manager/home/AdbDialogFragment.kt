@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package moe.shizuku.manager.home

import android.Manifest.permission.WRITE_SECURE_SETTINGS
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.ShizukuSettings
import moe.shizuku.manager.adb.AdbMdns
import moe.shizuku.manager.adb.AdbStarter
import moe.shizuku.manager.utils.EnvironmentUtils

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun AdbDiscoveryDialog(
    onDismissRequest: () -> Unit,
    onStartService: (port: Int) -> Unit
) {
    val context = LocalContext.current
    var startCommitted by remember { mutableStateOf(false) }

    val fallbackPort = remember {
        if (ShizukuSettings.isTcpMode()) {
            AdbStarter.TCP_MODE_PORT
        } else {
            EnvironmentUtils.getAdbTcpPort()
        }
    }

    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            val cr = context.contentResolver
            Settings.Global.putInt(cr, "adb_wifi_enabled", 1)
            Settings.Global.putInt(cr, Settings.Global.ADB_ENABLED, 1)
            Settings.Global.putLong(cr, "adb_allowed_connection_time", 0L)
        }

        if (ShizukuSettings.isTcpMode()) {
            withContext(Dispatchers.IO) {
                if (EnvironmentUtils.isAdbPortLive(AdbStarter.TCP_MODE_PORT)) {
                    withContext(Dispatchers.Main) {
                        if (!startCommitted) {
                            startCommitted = true
                            onStartService(AdbStarter.TCP_MODE_PORT)
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val adbMdns = AdbMdns(context, AdbMdns.TLS_CONNECT) { discoveredPort ->
            if (discoveredPort in 1..65535 && !startCommitted) {
                startCommitted = true
                onStartService(discoveredPort)
            }
        }
        adbMdns.start()

        onDispose {
            adbMdns.stop()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.ic_adb_24dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(stringResource(R.string.dialog_adb_discovery))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dialog_adb_discovery_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(R.string.dialog_adb_discovery_message_toggle_wireless_debugging),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                LoadingIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (fallbackPort in 1..65535) {
                    OutlinedButton(
                        onClick = {
                            if (!startCommitted) {
                                startCommitted = true
                                onStartService(fallbackPort)
                            }
                        }
                    ) {
                        Text("$fallbackPort")
                    }
                }
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(":settings:fragment_args_key", "toggle_adb_wireless")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: ActivityNotFoundException) {
                        }
                    }
                ) {
                    Text(stringResource(R.string.development_settings))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge
    )
}
