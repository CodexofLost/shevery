@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class
)

package moe.shizuku.manager.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.shizuku.manager.R
import moe.shizuku.manager.module.ModuleSettings
import moe.shizuku.manager.ui.compose.ShizukuLazyScaffold
import moe.shizuku.manager.utils.AiExplainUtil
import moe.shizuku.server.IShizukuService
import org.json.JSONArray
import org.json.JSONObject
import rikka.shizuku.Shizuku
import java.net.HttpURLConnection
import java.net.URL

private data class PresetCommand(
    val title: String,
    val command: String,
    val category: String
)

@Composable
fun ComputScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var command by remember { mutableStateOf("pm list packages -3") }
    var outputLog by remember { mutableStateOf(context.getString(R.string.comput_console_initialized)) }
    var isRunning by remember { mutableStateOf(false) }
    var isAdbMode by remember { mutableStateOf(false) }
    var isExplaining by remember { mutableStateOf(false) }
    var aiExplanation by remember { mutableStateOf("") }
    var showGeminiSection by remember { mutableStateOf(false) }
    var showReCommandPrompt by remember { mutableStateOf(false) }
    var lastFailed by remember { mutableStateOf(false) }

    var showCommandiumSheet by remember { mutableStateOf(false) }
    var showMacrosSheet by remember { mutableStateOf(false) }
    var showPresetsSheet by remember { mutableStateOf(false) }

    var isRecording by remember { mutableStateOf(false) }
    val recordedCommands = remember { mutableStateListOf<String>() }
    var showSaveMacroDialog by remember { mutableStateOf(false) }
    var macroNameInput by remember { mutableStateOf("") }

    var commandiumPrompt by remember { mutableStateOf("") }
    var isCommandiumGenerating by remember { mutableStateOf(false) }
    var generatedCommandiumResult by remember { mutableStateOf("") }

    var savedMacros by remember {
        mutableStateOf<Map<String, List<String>>>(
            try {
                val json = JSONObject(ModuleSettings.getComputMacros())
                val map = mutableMapOf<String, List<String>>()
                json.keys().forEach { key ->
                    val arr = json.getJSONArray(key)
                    val list = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        list.add(arr.getString(i))
                    }
                    map[key] = list
                }
                map.toMap()
            } catch (e: Exception) {
                emptyMap()
            }
        )
    }

    fun saveMacro(name: String) {
        val updated = savedMacros.toMutableMap()
        updated[name] = recordedCommands.toList()
        savedMacros = updated.toMap()
        val json = JSONObject()
        savedMacros.forEach { (k, v) ->
            json.put(k, JSONArray(v))
        }
        ModuleSettings.setComputMacros(json.toString())
        recordedCommands.clear()
    }

    fun deleteMacro(name: String) {
        val updated = savedMacros.toMutableMap()
        updated.remove(name)
        savedMacros = updated.toMap()
        val json = JSONObject()
        savedMacros.forEach { (k, v) ->
            json.put(k, JSONArray(v))
        }
        ModuleSettings.setComputMacros(json.toString())
    }

    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = MaterialTheme.colorScheme.tertiary
    val normalColor = MaterialTheme.colorScheme.onSurface

    val annotatedOutput = remember(outputLog, errorColor, warningColor, normalColor) {
        buildAnnotatedLog(outputLog, errorColor, warningColor, normalColor)
    }

    val statusText = when {
        isRunning -> stringResource(R.string.comput_status_running)
        lastFailed -> stringResource(R.string.comput_status_error)
        else -> stringResource(R.string.comput_status_ready)
    }
    val statusColor = when {
        isRunning -> MaterialTheme.colorScheme.tertiary
        lastFailed -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }

    suspend fun executeCommandInternal(cmd: String): Pair<String, Boolean> {
        val finalCmd = if (isAdbMode) {
            var trimmed = cmd.trim()
            if (trimmed.startsWith("adb shell ")) {
                trimmed = trimmed.substring(10).trim()
            } else if (trimmed.startsWith("adb shell")) {
                trimmed = trimmed.substring(9).trim()
            } else if (trimmed.startsWith("shell ")) {
                trimmed = trimmed.substring(6).trim()
            } else if (trimmed.startsWith("shell")) {
                trimmed = trimmed.substring(5).trim()
            } else if (trimmed.startsWith("adb ")) {
                trimmed = trimmed.substring(4).trim()
            } else if (trimmed == "adb") {
                trimmed = "adb_help"
            }

            if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
                trimmed = trimmed.substring(1, trimmed.length - 1).trim()
            } else if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length >= 2) {
                trimmed = trimmed.substring(1, trimmed.length - 1).trim()
            }

            if (trimmed == "adb_help" || trimmed == "help" || trimmed == "--help" || trimmed == "-h") {
                "adb_internal_help"
            } else if (trimmed == "devices") {
                "adb_internal_devices"
            } else if (trimmed.startsWith("install")) {
                "adb_internal_install"
            } else if (trimmed.startsWith("push") || trimmed.startsWith("pull")) {
                "adb_internal_file_transfer"
            } else {
                trimmed
            }
        } else {
            cmd.trim()
        }

        if (finalCmd.isBlank()) {
            return Pair("[E] Error: Command translates to empty string.", true)
        }

        val result = withContext(Dispatchers.IO) {
            if (isAdbMode) {
                when (finalCmd) {
                    "adb_internal_help" -> {
                        return@withContext Pair("""
                            Android Debug Bridge (Shevery Console Bridge)
                            You are connected to the device privileged shell via Shevery.
                            
                            For on-device shell commands, type them directly without 'adb' or 'adb shell'.
                            Examples:
                              pm list packages
                              settings get secure android_id
                              dumpsys battery
                            
                            Note: Host-side commands like 'adb devices', 'adb push/pull', or 'adb install' are not supported directly inside the device shell. Use standard shell equivalents (e.g. 'pm install').
                        """.trimIndent(), false)
                    }
                    "adb_internal_devices" -> {
                        return@withContext Pair("""
                            List of devices attached
                            local_shevery_device    device
                            
                            [I] You are currently inside the shell of this device.
                        """.trimIndent(), false)
                    }
                    "adb_internal_install" -> {
                        return@withContext Pair("[E] 'adb install' is a host-side command.\nTo install an APK directly on the device, use:\n  pm install <path_to_apk>", true)
                    }
                    "adb_internal_file_transfer" -> {
                        return@withContext Pair("[E] 'adb push' and 'adb pull' are host-side file transfer commands.\nUse 'cp' or 'mv' to copy/move files on the device, or use a file manager.", true)
                    }
                }
            }

            if (!Shizuku.pingBinder()) {
                return@withContext Pair(context.getString(R.string.comput_error_not_running), true)
            }
            try {
                val binder = Shizuku.getBinder() ?: return@withContext Pair(context.getString(R.string.comput_error_no_binder), true)
                val service = IShizukuService.Stub.asInterface(binder)
                val remote = service.newProcess(
                    arrayOf("sh", "-c", finalCmd),
                    null,
                    null
                )
                
                val stdoutPfd = remote.getInputStream()
                val stderrPfd = remote.getErrorStream()

                var stdoutText = ""
                var stderrText = ""
                val stdoutThread = Thread {
                    try {
                        stdoutText = readStreamTail(stdoutPfd)
                    } catch (ignore: Exception) { }
                }
                val stderrThread = Thread {
                    try {
                        stderrText = readStreamTail(stderrPfd)
                    } catch (ignore: Exception) { }
                }
                stdoutThread.start()
                stderrThread.start()
                
                val finished = remote.waitForTimeout(120L, java.util.concurrent.TimeUnit.SECONDS.name)
                val exitCode = if (finished) {
                    remote.exitValue()
                } else {
                    remote.destroy()
                    try { stdoutPfd.close() } catch (ignore: Exception) {}
                    try { stderrPfd.close() } catch (ignore: Exception) {}
                    124
                }
                stdoutThread.join(1000)
                stderrThread.join(1000)
                
                val resString = buildString {
                    if (stdoutText.isNotBlank()) append(stdoutText.trim())
                    if (stderrText.isNotBlank()) {
                        if (isNotEmpty()) append("\n")
                        append("[E] ")
                        append(stderrText.trim())
                    }
                    if (!finished) {
                        if (isNotEmpty()) append("\n")
                        append(context.getString(R.string.comput_timed_out))
                    } else if (exitCode != 0) {
                        if (isNotEmpty()) append("\n")
                        append(context.getString(R.string.comput_exit_code, exitCode))
                    }
                    if (isEmpty()) append(context.getString(R.string.comput_command_no_output))
                }
                val hasFailed = !finished || exitCode != 0 || stderrText.isNotBlank()
                Pair(resString, hasFailed)
            } catch (e: Exception) {
                Pair("[E] Shell execution failed: ${e.message}", true)
            }
        }
        return result
    }

    fun runShellCommand(cmd: String) {
        if (cmd.isBlank()) return
        scope.launch {
            isRunning = true
            lastFailed = false
            aiExplanation = ""
            
            if (isRecording) {
                recordedCommands.add(cmd)
            }

            if (isAdbMode) {
                outputLog = context.getString(R.string.comput_adb_translate, cmd)
            } else {
                outputLog = context.getString(R.string.comput_executing, cmd)
            }

            val (result, hasFailed) = executeCommandInternal(cmd)
            outputLog = result
            isRunning = false
            lastFailed = hasFailed

            if (hasFailed && ModuleSettings.isComputAiExplainEnabled()) {
                val apiKey = ModuleSettings.getComputApiKey()
                if (apiKey.isNotBlank()) {
                    scope.launch {
                        isExplaining = true
                        showGeminiSection = true
                        aiExplanation = AiExplainUtil.explainFailure(
                            contextStr = "Shevery Comput Console Shell Command Execution",
                            inputDetail = cmd,
                            outputLog = result,
                            apiKey = apiKey
                        )
                        isExplaining = false
                    }
                }
            }
        }
    }

    fun runMacro(macroName: String, commands: List<String>) {
        scope.launch {
            isRunning = true
            lastFailed = false
            aiExplanation = ""
            val fullLog = StringBuilder()
            fullLog.append(context.getString(R.string.comput_running_macro, macroName)).append("\n\n")
            outputLog = fullLog.toString()

            for (cmd in commands) {
                fullLog.append(context.getString(R.string.comput_executing_cmd, cmd)).append("\n")
                outputLog = fullLog.toString()
                
                val (result, hasFailed) = executeCommandInternal(cmd)
                fullLog.append(result).append("\n\n")
                outputLog = fullLog.toString()
                lastFailed = hasFailed

                if (hasFailed && ModuleSettings.isComputAiExplainEnabled()) {
                    val apiKey = ModuleSettings.getComputApiKey()
                    if (apiKey.isNotBlank()) {
                        scope.launch {
                            isExplaining = true
                            showGeminiSection = true
                            aiExplanation = AiExplainUtil.explainFailure(
                                contextStr = "Shevery Macro Command Execution ($macroName - $cmd)",
                                inputDetail = cmd,
                                outputLog = result,
                                apiKey = apiKey
                            )
                            isExplaining = false
                        }
                    }
                }
            }
            isRunning = false
        }
    }

    fun requestRun() {
        if (ModuleSettings.isComputRecommandEnabled()) {
            showReCommandPrompt = true
        } else {
            runShellCommand(command)
        }
    }

    fun triggerGeminiExplanation() {
        scope.launch {
            isExplaining = true
            showGeminiSection = true
            val apiKey = ModuleSettings.getComputApiKey()
            aiExplanation = explainCommandWithGemini(command, outputLog, apiKey, context,
                emptyApiKeyMessage = context.getString(R.string.comput_ai_api_key_empty))
            isExplaining = false
        }
    }

    val quickActionChips = remember {
        listOf(
            "pm list packages -3",
            "dumpsys battery",
            "logcat -d -t 30",
            "settings get secure android_id",
            "df -h",
            "getprop ro.build.version.release"
        )
    }

    val presetLibrary = remember {
        listOf(
            PresetCommand("User Installed Apps", "pm list packages -3", "Package Manager"),
            PresetCommand("System Packages", "pm list packages -s", "Package Manager"),
            PresetCommand("Disabled Packages", "pm list packages -d", "Package Manager"),
            PresetCommand("Manager APK Path", "pm path moe.shizuku.manager", "Package Manager"),

            PresetCommand("Android OS Version", "getprop ro.build.version.release", "System Props"),
            PresetCommand("Device Model Name", "getprop ro.product.model", "System Props"),
            PresetCommand("Display Screen Resolution", "wm size", "System Props"),
            PresetCommand("Screen Density (DPI)", "wm density", "System Props"),

            PresetCommand("Battery Health & Level", "dumpsys battery", "Battery & Power"),
            PresetCommand("Simulate Disconnected AC", "dumpsys battery unplug", "Battery & Power"),
            PresetCommand("Reset Battery Stats", "dumpsys battery reset", "Battery & Power"),

            PresetCommand("Disk Usage & Free Space", "df -h", "Storage & Files"),
            PresetCommand("Root Storage File List", "ls -la /sdcard", "Storage & Files"),
            PresetCommand("Download Folder Size", "du -sh /sdcard/Download", "Storage & Files"),

            PresetCommand("Active Network Sockets", "netstat -tulpn", "Network"),
            PresetCommand("Network Interfaces & IPs", "ip addr", "Network")
        )
    }

    ShizukuLazyScaffold(
        title = stringResource(R.string.comput_title),
        onNavigateUp = null,
        bottomInset = 112.dp
    ) {
        // Mode & Tools Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SegmentedButton(
                            selected = !isAdbMode,
                            onClick = { isAdbMode = false },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(R.string.comput_sh_mode),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        SegmentedButton(
                            selected = isAdbMode,
                            onClick = { isAdbMode = true },
                            shape = CircleShape,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(R.string.comput_adb_mode),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Horizontally Scrollable Action Tools Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = { showCommandiumSheet = true },
                            label = {
                                Text(
                                    stringResource(R.string.comput_tab_commandium),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { showMacrosSheet = true },
                            label = {
                                Text(
                                    stringResource(R.string.comput_tab_macros),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                        FilterChip(
                            selected = false,
                            onClick = { showPresetsSheet = true },
                            label = {
                                Text(
                                    stringResource(R.string.comput_tab_presets),
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                    }
                }
            }
        }

        // Quick Command Chips Carousel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                quickActionChips.forEach { chipText ->
                    FilterChip(
                        selected = command == chipText,
                        onClick = { command = chipText },
                        label = {
                            Text(
                                text = chipText,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                maxLines = 1
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        // Command Input Field Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = command,
                        onValueChange = { command = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        ),
                        leadingIcon = {
                            Text(
                                text = if (isAdbMode) "#" else "$",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        trailingIcon = {
                            if (command.isNotEmpty()) {
                                IconButton(onClick = { command = "" }) {
                                    Icon(
                                        Icons.Rounded.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        label = { Text(stringResource(R.string.comput_command_label)) },
                        placeholder = { Text(stringResource(R.string.comput_command_placeholder)) },
                        maxLines = 4,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )

                    Button(
                        onClick = { requestRun() },
                        enabled = !isRunning && command.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isRunning) {
                            LoadingIndicator(
                                Modifier.size(18.dp),
                                MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.comput_status_running), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = stringResource(R.string.comput_run),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.comput_run),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Terminal Output Canvas with Integrated Ask Gemini Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Terminal,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.comput_terminal_header),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isRunning) {
                                LoadingIndicator(
                                    Modifier.size(14.dp),
                                    MaterialTheme.colorScheme.primary
                                )
                            }
                            FilterChip(
                                selected = showGeminiSection,
                                onClick = {
                                    if (!showGeminiSection && aiExplanation.isBlank() && !isExplaining) {
                                        triggerGeminiExplanation()
                                    } else {
                                        showGeminiSection = !showGeminiSection
                                    }
                                },
                                label = { Text(stringResource(R.string.comput_ask_gemini), style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    if (isExplaining) {
                                        LoadingIndicator(Modifier.size(14.dp), MaterialTheme.colorScheme.primary)
                                    } else {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("ADB Output", outputLog))
                                    Toast.makeText(context, context.getString(R.string.comput_output_copied), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ContentCopy,
                                    contentDescription = stringResource(R.string.comput_copy_output),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    command = ""
                                    outputLog = context.getString(R.string.comput_console_cleared)
                                    aiExplanation = ""
                                    showGeminiSection = false
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(R.string.comput_clear),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 380.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        SelectionContainer {
                            Text(
                                text = annotatedOutput,
                                modifier = Modifier.padding(14.dp),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lines: ${outputLog.lineSequence().count()} | Chars: ${outputLog.length}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Integrated Expandable Gemini AI Explanation Block
                    AnimatedVisibility(visible = showGeminiSection) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .animateContentSize(),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.comput_gemini_explain),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (aiExplanation.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("Gemini Analysis", aiExplanation))
                                                    Toast.makeText(context, context.getString(R.string.comput_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Rounded.ContentCopy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp),
                                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { showGeminiSection = false },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Rounded.Clear,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }

                                if (isExplaining) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        LoadingIndicator(Modifier.size(16.dp), MaterialTheme.colorScheme.onSecondaryContainer)
                                        Text("Analyzing command & output...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                } else if (aiExplanation.isNotBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
                                    SelectionContainer {
                                        Text(
                                            text = aiExplanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = { triggerGeminiExplanation() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Click to generate Gemini AI breakdown", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheet for Commandium AI Studio
    if (showCommandiumSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommandiumSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = stringResource(R.string.comput_commandium_assistant),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = stringResource(R.string.comput_commandium_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "List user installed apps",
                        "Check battery temperature & status",
                        "Find files larger than 50MB",
                        "Get device Android model & build"
                    ).forEach { suggestion ->
                        FilterChip(
                            selected = commandiumPrompt == suggestion,
                            onClick = { commandiumPrompt = suggestion },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                            shape = CircleShape
                        )
                    }
                }

                OutlinedTextField(
                    value = commandiumPrompt,
                    onValueChange = { commandiumPrompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    label = { Text(stringResource(R.string.comput_commandium_label)) },
                    placeholder = { Text(stringResource(R.string.comput_commandium_placeholder)) },
                    maxLines = 3
                )

                Button(
                    onClick = {
                        scope.launch {
                            isCommandiumGenerating = true
                            val apiKey = ModuleSettings.getComputApiKey()
                            generatedCommandiumResult = AiExplainUtil.generateCommand(commandiumPrompt, apiKey)
                            isCommandiumGenerating = false
                        }
                    },
                    enabled = !isCommandiumGenerating && commandiumPrompt.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape
                ) {
                    if (isCommandiumGenerating) {
                        LoadingIndicator(Modifier.size(18.dp), MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(stringResource(R.string.comput_ask_commandium), fontWeight = FontWeight.Bold)
                    }
                }

                if (generatedCommandiumResult.isNotBlank()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = stringResource(R.string.comput_generated_command),
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            SelectionContainer {
                                Text(
                                    text = generatedCommandiumResult,
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        command = generatedCommandiumResult
                                        showCommandiumSheet = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = CircleShape
                                ) {
                                    Text(stringResource(R.string.comput_use_command))
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Commandium", generatedCommandiumResult))
                                        Toast.makeText(context, context.getString(R.string.comput_copied_to_clipboard), Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Modal BottomSheet for Macros
    if (showMacrosSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMacrosSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.comput_console_macros),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isRecording) {
                        Button(
                            onClick = {
                                showSaveMacroDialog = true
                                isRecording = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = CircleShape
                        ) {
                            Text(stringResource(R.string.comput_stop, recordedCommands.size))
                        }
                    } else {
                        Button(
                            onClick = {
                                recordedCommands.clear()
                                isRecording = true
                            },
                            shape = CircleShape
                        ) {
                            Text(stringResource(R.string.comput_record_macro))
                        }
                    }
                }

                if (isRecording) {
                    Text(
                        text = stringResource(R.string.comput_recording_active),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = stringResource(R.string.comput_saved_macros),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (savedMacros.isEmpty()) {
                    Text(
                        text = stringResource(R.string.comput_no_macros),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        savedMacros.forEach { (name, cmds) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${cmds.size} cmds",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                showMacrosSheet = false
                                                runMacro(name, cmds)
                                            },
                                            enabled = !isRunning,
                                            modifier = Modifier.weight(1f),
                                            shape = CircleShape
                                        ) {
                                            Text(stringResource(R.string.comput_run))
                                        }
                                        TextButton(
                                            onClick = { deleteMacro(name) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                            shape = CircleShape
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(stringResource(R.string.comput_delete))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Modal BottomSheet for Presets Library
    if (showPresetsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPresetsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.comput_presets_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.comput_presets_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val categories = remember { presetLibrary.map { it.category }.distinct() }
                categories.forEach { cat ->
                    Text(
                        text = cat.uppercase(),
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    presetLibrary.filter { it.category == cat }.forEach { preset ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = preset.command,
                                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            command = preset.command
                                            showPresetsSheet = false
                                        }
                                    ) {
                                        Icon(Icons.Rounded.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    }
                                    Button(
                                        onClick = {
                                            command = preset.command
                                            showPresetsSheet = false
                                            requestRun()
                                        },
                                        shape = CircleShape
                                    ) {
                                        Text(stringResource(R.string.comput_run), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showReCommandPrompt) {
        AlertDialog(
            onDismissRequest = { showReCommandPrompt = false },
            title = { Text(stringResource(R.string.comput_recommand_confirmation_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.comput_recommand_confirmation),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = command,
                            modifier = Modifier.padding(12.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReCommandPrompt = false
                        runShellCommand(command)
                    },
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.comput_execute))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReCommandPrompt = false },
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.comput_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    if (showSaveMacroDialog) {
        AlertDialog(
            onDismissRequest = { showSaveMacroDialog = false },
            title = { Text(stringResource(R.string.comput_save_macro)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.comput_save_macro_hint))
                    OutlinedTextField(
                        value = macroNameInput,
                        onValueChange = { macroNameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        label = { Text(stringResource(R.string.comput_macro_name_label)) },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (macroNameInput.isNotBlank()) {
                            saveMacro(macroNameInput.trim())
                            macroNameInput = ""
                            showSaveMacroDialog = false
                        }
                    },
                    enabled = macroNameInput.isNotBlank(),
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.comput_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        recordedCommands.clear()
                        macroNameInput = ""
                        showSaveMacroDialog = false
                    },
                    shape = CircleShape
                ) {
                    Text(stringResource(R.string.comput_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

private fun buildAnnotatedLog(
    text: String,
    errorColor: Color,
    warningColor: Color,
    normalColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            val isError = line.contains("error", ignoreCase = true) ||
                    line.contains("failed", ignoreCase = true) ||
                    line.contains("exception", ignoreCase = true) ||
                    line.contains("[E]", ignoreCase = true) ||
                    line.contains("denied", ignoreCase = true)
            val isWarning = line.contains("warn", ignoreCase = true) ||
                    line.contains("[W]", ignoreCase = true)

            if (isError) {
                withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                    append(line)
                }
            } else if (isWarning) {
                withStyle(style = SpanStyle(color = warningColor, fontWeight = FontWeight.SemiBold)) {
                    append(line)
                }
            } else {
                withStyle(style = SpanStyle(color = normalColor)) {
                    append(line)
                }
            }
            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

private suspend fun explainCommandWithGemini(
    command: String,
    output: String,
    apiKey: String,
    context: Context,
    emptyApiKeyMessage: String = "Google AI Studio API Key is empty! Please configure it in Shevery Settings (Comput Console Settings)."
): String = withContext(Dispatchers.IO) {
    if (apiKey.isBlank()) {
        return@withContext emptyApiKeyMessage
    }
    try {
        val selectedModel = ModuleSettings.getComputGeminiModel()
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = 15000
        conn.readTimeout = 15000
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")

        val currentLocale = java.util.Locale.getDefault()
        val prompt = "CRITICAL: You must write the entire explanation in the following language: ${currentLocale.displayName} (locale code: ${currentLocale.toLanguageTag()}).\n\n" +
                "Explain the following shell command and its execution output in a clear, concise, and helpful developer-focused way. If there are errors or warnings, explain what caused them and how to resolve them:\n\n" +
                "Command: $command\n\n" +
                "Output:\n$output"
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }

        conn.outputStream.use { os ->
            os.write(requestBody.toString().toByteArray(Charsets.UTF_8))
            os.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == 200) {
            val responseText = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)
            val text = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
            text.trim()
        } else {
            val errText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: context.getString(R.string.comput_gemini_no_details)
            context.getString(R.string.comput_gemini_api_error, responseCode, errText)
        }
    } catch (e: Exception) {
        context.getString(R.string.comput_gemini_failed, e.message ?: context.getString(R.string.comput_gemini_connection_error))
    }
}

private fun readStreamTail(pfd: ParcelFileDescriptor): String {
    return ParcelFileDescriptor.AutoCloseInputStream(pfd).reader(Charsets.UTF_8).use { reader ->
        val buffer = CharArray(8192)
        val tail = StringBuilder()
        while (true) {
            val read = reader.read(buffer)
            if (read <= 0) break
            tail.append(buffer, 0, read)
            if (tail.length > 64 * 1024 * 2) {
                tail.delete(0, tail.length - 64 * 1024)
            }
        }
        if (tail.length > 64 * 1024) {
            tail.substring(tail.length - 64 * 1024)
        } else {
            tail.toString()
        }
    }
}
