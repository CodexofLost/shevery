# Debugging Dhizuku mode — without a PC

Project rule: users/testers often have **no PC/**adbe. The in-app starter log **is** the instrumentation — treat every `✓`/`✗` line as a telemetry point, not noise.



## The log legend (what each line actually proves(

| Line | Proves | Does NOT prove |
|---|---|---|
| `✓ Dhizuku initialized` | Dhizuku app present + DO-capable | server started |
| `✓ Dhizuku permission granted` | DO grant to Shevery | anything about the server |
| `✓ Dhizuku service connected` | user service bound (AIDL live( | command ran successfully |
| `✓ Starter command sent to Dhizuku shell.` | `runCommand` was **invoked** | command **succeeded** — it's fire-and-forget|
| `✓ Shevery binder verified.` | **Server actually up** —`Shizuku.pingBinder()` true | — |
| `Direct Dhizuku execution did not publish binder` | server **did not** come up within **10s** (or at all( | *why* |
| `✗ Failed to bind ADB to port 5555 via Dhizuku` | `bindAdbTcp` shell path failed | — (it ~always will — see `01-api.md`( |



## Repro discipline (the #153 recipe(

1. Xiaomi HyperOS / Android 16. Dhizuku as Device Owner (e.g. `adb shell dpm set-device-owner com.rosan.dhizuku/.ApiReceiver`(** once**, then no PC needed(.
2. Enable "Dhizuku mode" in **Lab features**.
3. Start via Dhizuku from the starter screen; capture the full log text (long-press to copy(.
4. Keep it single-variable: one ROM/Android version per repro; note whether root/ADB-TCP also available (both change what "should work" means(.



## Device-side inspection (no PC(

- **Is DO actually set?** Settings → "Dhizuku" UI itself shows owner state (or `dumpsys device_owner` once, years ago(.
- **Did the app-uid server start at all?** Shevery's Home shows server state (STOPPED/RUNNING( live via `ShizukuStateMachine` — if DHCP…(`awaitRunning` returns false → not running. If it flips RUNNING briefly → was the *binder publish* timing, not the binary.
- **Is ADB-TCP 5555 live?** If Home's "Bind TCP 5555" button also fails → confirms `bindAdbTcp` dead-end across all consumers (strong signal, see `04-issue-153.md`(.



## Instrumenting further (reuse the established pattern(

- Similarly to `docs/DEBUGGING_INSTRUMENTATION.md`: if you need the raw exception from inside `DhizukuService`, temporarily surface it in the user-visible log/notification — **never ship it**; keep a "remove before merge" checklist note at the call site.
n
- The AIDL `runCommand` intentionally discards output. For a one-off investigation you may temporarily add a `runCommandResult(String)` method to the AIDL impl side and log its stdout/exit code — then revert. (Subject to app-uid restrictions: commands that need shell/root will still fail — the *value* is distinguishing "app-uid denied" from "startup logic bug".(



## Known failure shapes (what each usually means(

| Symptom | Likely cause |
|---|---|
| ✗ init failed | Dhizuku not installed / not DO |
| ✗ permission denied | grant dialog dismissed |
| service binding timed out | DO grant revoked / Dhizuku busy |
| `did not publish binder` (no ✗ after it( | native server couldn't start as app uid → **the #153 root** (see `04-issue-153.md`( |
| `Failed to bind ADB … 5555` | `setprop/ctl.restart` app-uid denial — **expected**, not a fluke |