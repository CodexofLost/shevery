# Tasker Plugin

Shevery ships a built-in **Tasker / Locale plugin** (`:tasker` module) with 4 actions and 1 state condition. No root needed inside Tasker — the plugin forwards the command to Shevery, which starts/stops the server itself.

Wiki mirror: https://github.com/HmnDev-Tech/shevery/wiki/Tasker-Plugin

## Install

The plugin is bundled in the Shevery APK. Just install Shevery, then open Tasker — `Shevery Tasker` appears under plugins.

## Task actions (Task → Plugin → Shevery Tasker)

| Command | What it does |
|---|---|
| **Start server** | Sends `moe.shizuku.manager.action.START_SERVER` to `SheveryControlReceiver` (clears user-stop flag, enqueues `AdbStartWorker` for ADB mode, watchdog restarts the server) |
| **Stop server** | Sends `moe.shizuku.manager.action.STOP_SERVER` (marks user-initiated stop so the watchdog stays off) |
| **Restart server** | Stop → wait for binder death (max 10 s) → start. If the server is already down, just starts it |
| **Toggle server** | `Shizuku.pingBinder()` → stop if running, start if not |

Configuration UI is `EditActivity`: a radio list for actions, a single fixed row for the condition. The choice is stored as JSON in the Locale bundle extra.

## Profile condition (Profiles → + → State → Plugin → Shevery Tasker)

Single condition: **Server is running**.

Returns `RESULT_CONDITION_SATISFIED` (16) when `Shizuku.pingBinder()` is true, otherwise `RESULT_CONDITION_UNSATISFIED` (17).

## Bundle format (Locale API)

Actions used: `com.twofortyfouram.locale.intent.action.EDIT_SETTING`, `EDIT_CONDITION`, `FIRE_SETTING`, `QUERY_CONDITION`.

Extras:

- `com.twofortyfouram.locale.intent.extra.BLURB` — display string (`Shevery: start/stop/restart/toggle server`, `Shevery: server is running`)
- `com.twofortyfouram.locale.intent.extra.BUNDLE` → Bundle with `com.twofortyfouram.locale.extra.STRING` = JSON:
  - action: `{"command":"start|stop|restart|toggle"}`
  - condition: `{"condition":"running"}`

The `FIRE_SETTING` / `QUERY_CONDITION` broadcast must be **explicit** to `com.hamondev.shevery / com.hamondev.shevery.tasker.PluginReceiver`, otherwise it is ignored.

## Example: auto-start on charger

1. Profile: State → Power → AC.
2. Enter task → Plugin → Shevery Tasker → **Start server**.
3. Exit task → Plugin → Shevery Tasker → **Stop server**.

## Notes & limits

- Receiver: `com.hamondev.shevery.tasker.PluginReceiver` (`exported=true`, filters `FIRE_SETTING`, `QUERY_CONDITION`), config activity `.EditActivity` (`EDIT_SETTING`, `EDIT_CONDITION`).
- Strings are localized (EN + RU): `tasker_plugin_name`, `tasker_command_*`, `tasker_blurb_*`, `tasker_condition_running`.
- Restart relies on `Shizuku.OnBinderDeadListener` + 10 s fallback timer.
- Direct control broadcasts (`SheveryControlReceiver`) are `exported=false` — third-party apps must go through this plugin, not send them directly. Raw intent reference: [intents.md](intents.md).

Sources: `tasker/src/main/java/com/hamondev/shevery/tasker/PluginContract.kt`, `PluginReceiver.kt`, `EditActivity.kt`, `tasker/src/main/AndroidManifest.xml`.
