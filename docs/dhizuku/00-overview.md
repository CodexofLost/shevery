# Dhizuku support — developer overview

Dhizuku is an experimental Device-Owner (DO( startup path for Shevery meteored as  "Dhizuku mode" under **Lab features** (`LabFeaturesActivity` → `ModuleSettings.isDhizukuEnabled()`(. It lets Shevery try to start its server without root or ADB TCP — via the Dhizuku app's Device Owner APIs, binding a user service that runs privileged DPM operations.

> **Status: experimental.** Labs-gated, not part of the main startup flow. See `docs/dhizuku/04-issue-153.md` for the known broken edge.



## Component map

| Piece | Where | Role |
|---|---|---|
| Dhizuku app APIs | `com.rosan.dhizuku.api.Dhizuku` (3rd party( | init / permission / bindUserService |
| User service AIDL | `manager/src/main/aidl/.../IDhizukuService.aidl` | 4-method contract the manager's user service exposes |
| User service impl | `manager/src/main/java/.../dhizuku/DhizukuService.kt` | Runs **in the app's own user-service process**, executes DPM ops + shell |
| Starter path | `StarterActivity.startDhizuku()` (lines ~302-433( | The "start via Dhizuku" flow users see in the log |
| Home bind branch | `HomeActivity.bindTcp5555()` (~652-( | "Bind TCP 5555" button's Dhizuku attempt |
| Watchdog recovery | `WatchdogManager` (~400-455( | Watchdog's Dhizuku-based server restart path |
| Lab gate | `LabFeaturesActivity` / `ModuleSettings.isDhizukuEnabled()` | Feature flag behind "Dhizuku mode" switch |



## The three consumers of `DhizukuService`

All three bind the same user service and call the same AIDL, but with different goals:

1. **`StarterActivity.startDhizuku()`** — start the server directly (run native `libshizuku.so` via `runCommand`(, wait for binder, **then fall back to ADB TCP** if not published. ← **#153 lives here** (see `04-issue-153.md`(.
2. **`HomeActivity.bindTcp5555()`** — the "Bind TCP 5555" settings action tries Dhizuku **before** root to bind ADB TCP; requires `bindAdbTcp` to actually work.
3. **`WatchdogManager`** — server recovery also tries a Dhizuku `runCommand` restart when enabled.



## Key mental model

- A Dhizuku user service is **not** root or shell. It runs as **the app's own uid** in a process the Dhizuku framework binds for you. Its superpower is `DevicePolicyManager` (set global settings( — **not** arbitrary shell authority.
- `runCommand()` / `bindAdbTcp()` execute **plain `sh -c` as app uid** — everything they do must be legal for an ordinary app process (plus whatever SELinux grants the DO app(.
- The native server (`libshizuku.so`, see `Starter.internalCommand`) normally runs **as adb shell uid** (`adb shell <binary>(` or root (`Shell.cmd(`. Whether an app-uid `sh -c <binary>` can publish the binder is device/ROM dependent — often fails (**this is the crux of #153**(.



## Dev setup

1. Enable "Dhizuku mode" in Lab features.
2. Install Dhizuku app, set it as Device Owner (e.g. `adb shell dpm set-device-owner com.rosan.dhizuku/.ApiReceiver`), grant Shevery.
3. Start via Dhizuku from the starter screen. Everything else (AIDL changes, re-debug( follows normal manager module workflow.
4. On the device, the in-app starter log is the primary instrument — see `03-debugging.md`.