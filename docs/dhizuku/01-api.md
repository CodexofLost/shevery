# Dhizuku user-service API — reference

AIDL: `manager/src/main/aidl/moe/shizuku/manager/dhizuku/IDhizukuService.aidl` (full(:

```aidl
interface IDhizukuService {
    void runCommand(String command);
    boolean enableAdb();
    int getAdbPort();
    boolean bindAdbTcp(int port);
}
```

Impl: `manager/src/main/java/moe/shizuku/manager/dhizuku/DhizukuService.kt`
**The impl process runs as the app's own UID** — every method below is subject to "what an app process + DO DPM grants can do."



## `runCommand(command)` — fire-and-forget shell

```kotlin
Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
```
- **Fully async**: stdout/stderr are drained but discarded; no exit code surfaces to caller; no output capture. Caller cannot distinguish success/failure via this method alone.
- Runs as **app uid**. Anything needing shell/root/SELinux-special (binder server startup, `ctl.restart`, property writes( will fail silently here.

Used by: Starter (`runCommand(Starter.internalCommand)`(, Watchdog (`runCommand(Starter.internalCommand)`(. `Starter.internalCommand` = `"<nativeLibDir>/libshizuku.so --apk=<sourceDir>"` — the actual native server, which normally wants **shell/root** when run via `adb shell` / root `Shell.cmd(`.



## `enableAdb()` — the only DPM-native method

```kotlin
val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val ownerAdmin = ... // active admin whose pkg isDeviceOwnerApp()
dpm.setGlobalSetting(ownerAdmin, Settings.Global.ADB_ENABLED, "1")
dpm.setGlobalSetting(ownerAdmin, "adb_wifi_enabled","1")  // API 30+
```
- Uses **real DO power** (`setGlobalSetting`( — this one **can legitimately succeed** as a DO app.
- Toggles global `adb_enabled` / `adb_wifi_enabled` — does **not** set `service.adb.tcp.port`, does **not** restart adbd, does **not** open TCP **5555**.
- Wireless-debugging daemon may appear on a **random pairing port** — not the fixed `5555` `AdbStarter.TCP_MODE_PORT`.



## `getAdbPort()`

Reads `service.adb.tcp.port` → `persist.adb.tcp.port` → `adb_wifi_port` (global setting(. Best-effort reflection, no side effects.



## `bindAdbTcp(port)` — likely-broken on modern Android

```kotlin
val cmd = "setprop service.adb.tcp.port $port; setprop ctl.restart adbd || (stop adbd; start adbd)"
Runtime.getRuntime().exec(arrayOf("sh","-c",cmd))
// then polls 127.0.0.1:port for ~5s
```
- Both halves are **shell/root-gated** ops:
  - `setprop service.adb.tcp.port` — setting `service.*` props is restricted; read-only for app/DO uid on AOSP 8+ denials, certain on 14+/HyperOS.

  - `setprop ctl.restart adbd` / `stop|start adbd` — `ctl.*` control + servicemanager restart requires **shell/root**; app uid → SELinux denial/`Operation not permitted` almost universally on 11+, certainly 14+/16.
- Even if both somehow succeeded, ADB must be `enabled` first (that's `enableAdb()`'s job( — and 5555 only appears if the adbd service honors the prop (debuggable/eng builds or permissive SELinux(.
- **Verdict:** as an app-uid user service, `bindAdbTcp` is a structural dead-end on modern Android. Do not build flows that require it to succeed. ← core finding behind `04-issue-153.md`.



## Privilege table (what actually works from the Dhizuku user-service process(

| Op | Works? | Why |
|---|---|---|
| `dpm.setGlobalSetting` (DO( | ✅ | Device Owner power — real |
| `Shell/root`-class ops | ❌ | app uid; SELinux |
| `setprop service.adb.tcp.port` | ❌ | read-only prop |
| `ctl.restart adbd` | ❌ | servicemanager control = shell/root |
| `runCommand(native server(` | ⚠️ | only if server can start as app uid → normally needs shell/root** |