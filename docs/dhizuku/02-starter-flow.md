# Dhizuku starter flow — code walkthrough

File: `manager/src/main/java/moe/shizuku/manager/starter/StarterActivity.kt` → `startDhizuku()` (~302-433(. The flow below mirrors **every log line** the user sees in StarterActivity's output — the in-app log **is** the instrumentation (see `03-debugging.md`(.



## Step table (log line → code line → what can go wrong(

| # | Log line | Code | Failure mode |
|---|---|---|---|
| 1 | `Starting with Dhizuku (Device Owner)...` | 303-306 | — |
| 2 | `Initializing Dhizuku...` | `Dhizuku.init(ctx)` 311 | app not installed / not DO → ✗ init failed |
| 3 | `Checking Dhizuku permission...` | `isPermissionGranted()` 320 | ✗ permission denied (request dialog( |
| 4 | `Binding Dhizuku user service...` | `Dhizuku.bindUserService(args, conn)` 338-357 | 10s timeout → ✗ binding failed/timed out |
| 5 | `✓ Dhizuku service connected` | 365 | — |
| 6 | `Executing Shevery starter directly via Dhizuku Device Owner...` | `service.runCommand(Starter.internalCommand)` 370 | silent — async, no exit code |
| 7 | `✓ Starter command sent to Dhizuku shell.` | 373 | (does **not** mean the server started( |
| 8 | `Waiting for Shevery service to initialize...` | `waitForShizukuBinder()` (≈ `ShizukuStateMachine.awaitRunning(10_000`( 375 | timeout → **fork** (below( |
| 9a | `✓ Shevery binder verified.` | 376-377 | ✅ success → `postResult()` |
| 9b | `Direct Dhizuku execution did not publish binder, attempting ADB TCP 5555 activation via Dhizuku...` | 379 | ← **#153 fork**: enters the ADB fallback |

### The 9b fallback branch (380-417(

```kotlin
dhizukuService.enableAdb()
val bound = dhizukuService.bindAdbTcp(AdbStarter.TCP_MODE_PORT)  // 5555
if (bound) { AdbStarter.start(host="127.0.0.1", port=5555, ...); waitForShizukuBinder() }
else { "✗ Failed to bind ADB to port 5555 via Dhizuku." }
if (!adbSuccess) { "✗ Starter command completed,but Shevery service did not become available." ; postResult(DhizukuException(...) }
```

- `enableAdb()` (DPM( may succeed (see `01-api.md`( — flips global `ADB_ENABLED`; harmless but does not open 5555.
- `bindAdbTcp(5555)` runs `setprop ... ctl.restart adbd` as app uid → **almost certainly fails** on 14+/16 → `bound=false` → straight to the ✗ line. Three doomed steps before the real error surfaces. **This is the UX defect in #153.**
- Even `bound=true` → `AdbStarter.start` connects to `127.0..1:5555` and waits  again; final `waitForShizukuBinder()` decides.



## Where the binder actually gets published

- `Starter.internalCommand` (in `Starter.kt`( = `<nativeLibDir>/libshizuku.so --apk=<apkPath>` — nightly native server binary.
- In ADB mode this runs as: `adb shell <binary>` (**shell uid** — reference working path(.
- In root mode: `Shell.cmd(internalCommand)` (**root**(.
- In Dhizuku mode: `sh -c <binary>` **as the app/DO uid** — the binary's usual prerequisites (binder privileges, socket/daemon ownership, `/dev` access( are absent → **binder publish commonly fails** — the 9b fork is therefore the *likely* case, not an edge. See `04-issue-153.md` for what to do about it.



## Failure semantics

- Any ✗ → `postResult(DhizukuException(msg(, exception(`, which StarterActivity maps to its failure UI (icon, retry(.
- `runCommand` failures are **silent** (fire-and-forget( — the only visibility is "binder not published" (step 9b( or a later `DhizukuException`(.
- The `finally` block always `unbindUserService` (421-424( — service is per-flow, no leak.



## Related consumers

- `HomeActivity.bindTcp5555()` — same `bindAdbTcp` call (as step  ​1 of the settings action( — inherits the same dead-end. If #153's fix removes/repairs the Dhizuku ADB-TCP fallback here too, it must be mirrored there.
- `WatchdogManager` — `runCommand(Starter.internalCommand)` restart path (≈ step  ​6( — affected by the same app-uid-server-start question; no ADB fallback there(.