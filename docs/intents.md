# Intents

Reference of the public (and internal) intents declared by Shevery Manager. Package name: `com.hamondev.shevery`.

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Intents

## Server control (internal)

Receiver: `moe.shizuku.manager.receiver.SheveryControlReceiver` (`exported=false`).

| Action | Effect |
|---|---|
| `moe.shizuku.manager.action.START_SERVER` | Clears the user-stop request; if last launch mode was ADB, enqueues `AdbStartWorker` and refreshes the notification; calls `WatchdogManager.attemptRestart()` |
| `moe.shizuku.manager.action.STOP_SERVER` | `goAsync()` + `WatchdogManager.stopServerAndWait(userInitiated=true)`, then refreshes the notification |

Because the receiver is **not exported**, only Shevery itself (notification buttons, `AdbStartWorker`, and the bundled [Tasker Plugin](tasker-plugin.md)) can send these. Third-party apps: use the Tasker plugin instead of crafting these broadcasts.

```kotlin
// inside Shevery (same UID) — explicit + package-scoped
val intent = Intent("moe.shizuku.manager.action.START_SERVER").apply {
    setPackage("com.hamondev.shevery")
    component = ComponentName(
        "com.hamondev.shevery",
        "moe.shizuku.manager.receiver.SheveryControlReceiver"
    )
}
context.sendBroadcast(intent)
```

> `adb shell am broadcast` with these actions will **not** work from the shell because the receiver is not exported — this is intentional, so random apps can't toggle your server.

## App update status (internal)

- Action: `moe.shizuku.manager.action.APP_UPDATE_INSTALL_STATUS`
- Sent by `PackageInstaller` to `AppUpdateInstaller` (`setPackage(context.packageName)`), consumed with an explicit `IntentFilter`. Not for external use.

## Public API for third-party apps

### Request permission (runtime dialog)

Launches `RequestPermissionActivity` (`exported=true`, requires `android.permission.INTERACT_ACROSS_USERS_FULL`):

- `com.hamondev.shevery.intent.action.REQUEST_PERMISSION`
- `moe.shizuku.privileged.api.intent.action.REQUEST_PERMISSION` (legacy alias)

Always add category `android.intent.category.DEFAULT`. This is the `ShizukuService#requestPermission()` flow — user taps Allow/Deny, decision is stored as `FLAG_ALLOWED` / `FLAG_DENIED`.

### Legacy authorization

- `com.hamondev.shevery.intent.action.REQUEST_AUTHORIZATION`
- `moe.shizuku.privileged.api.intent.action.REQUEST_AUTHORIZATION`

→ `LegacyIsNotSupportedActivity` (guarded by `moe.shizuku.manager.permission.API`). Old Shizuku API clients only.

### Binder request (shell / legacy)

- `rikka.shizuku.intent.action.REQUEST_BINDER` → `ShellRequestHandlerActivity` and `ShizukuReceiver` (`exported=true`, `directBootAware=true`)
- Aliases also handled by `ShizukuReceiver`: `com.hamondev.shevery.intent.action.REQUEST_BINDER`, `moe.shizuku.privileged.api.intent.action.REQUEST_BINDER`

Used by the `rish` / shell path (`ShellBinderRequestHandler`, `ShizukuReceiverStarter`). See also [shizuku-connectors.md](shizuku-connectors.md).

### Binder delivery

- `moe.shizuku.api.action.BINDER_RECEIVED` (broadcast by `ShizukuProvider`, extra `moe.shizuku.privileged.api.intent.extra.BINDER`) — how API clients receive the binder. Permission `moe.shizuku.manager.permission.API_V23`.

### Accessibility manager shortcut

- `com.hamondev.shevery.action.ACCESSIBILITY_MANAGER` (+ `DEFAULT` category) → `AccessibilityManagerActivity` (`exported=true`). Lists system accessibility services, enable/disable/pin via `WRITE_SECURE_SETTINGS`.

## Quick examples

```sh
# open the accessibility manager
adb shell am start -a com.hamondev.shevery.action.ACCESSIBILITY_MANAGER

# legacy permission request (reproduces the API dialog path)
adb shell am start -a com.hamondev.shevery.intent.action.REQUEST_PERMISSION
```

Source of truth: `manager/src/main/AndroidManifest.xml`, `manager/.../receiver/SheveryControlReceiver.kt`, `tasker/.../PluginContract.kt`.
