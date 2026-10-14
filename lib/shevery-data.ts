export interface FeatureItem {
  id: string
  category: "core" | "ai" | "stealth" | "enterprise" | "automation" | "lab"
  badge: string
  title: string
  description: string
  technicalDetail: string
  accent: "primary" | "secondary" | "tertiary" | "error"
  iconName: string
  span?: "wide" | "tall" | "normal"
}

export interface GuideDocItem {
  id: string
  category: string
  title: string
  summary: string
  badge: string
  codeTitle: string
  codeSnippet: string
  steps: string[]
}

export const SHEVERY_STATS = [
  { value: "Android 11–17", label: "Target SDK 36 & Ready" },
  { value: "< 1.2 ms", label: "Direct Binder IPC Latency" },
  { value: "44 Tools", label: "Comput Autonomous Agent" },
  { value: "3 Stubs", label: "Sui · Dhizuku · Launchers" },
]

export const SHEVERY_FEATURES: FeatureItem[] = [
  {
    id: "comput-agent",
    category: "ai",
    badge: "AI AGENT · 44 TOOLS",
    title: "Comput Autonomous Agent & Shell",
    description:
      "Multi-step ReAct agent right inside the privilege manager. Inspects UI trees, captures screen via MediaProjection, taps, swipes, manages packages, and runs privileged shell commands.",
    technicalDetail:
      "Supports OpenAI, Anthropic, Google Gemini (API & OAuth), Groq, OpenRouter, DeepSeek, Cerebras, plus custom OpenAI-compatible endpoints with native tool-calling.",
    accent: "primary",
    iconName: "Bot",
    span: "wide",
  },
  {
    id: "adb-modules",
    category: "core",
    badge: "MAGISK-LIKE · ROOTLESS",
    title: "ADB Modules & GitHub Catalog",
    description:
      "Install declarative ZIP modules without root. Automatic rollback if a boot crash is detected, plus a built-in GitHub repository store with verified badges.",
    technicalDetail:
      "Includes 15+ helper APIs in api.sh: set_prop, apply_settings, pkg_disable, cmd_appops, and WebUI bridge window.shizuku.exec().",
    accent: "tertiary",
    iconName: "PackageCheck",
    span: "normal",
  },
  {
    id: "stealth-edition",
    category: "stealth",
    badge: "ANTI-DETECTION",
    title: "Stealth Edition & On-Device APK Morphing",
    description:
      "Evade aggressive banking and anti-cheat package scanners by generating a randomized package name and custom label directly on the device.",
    technicalDetail:
      "Patches binary AndroidManifest.xml string pools in UTF-8/UTF-16LE, resigns with an auto-generated V1/V2/V3 certificate, and migrates your config.",
    accent: "secondary",
    iconName: "ShieldAlert",
    span: "normal",
  },
  {
    id: "usb-debugging-hide",
    category: "stealth",
    badge: "SPOOFING GUARD",
    title: "USB Debugging & Dev Options Spoofing",
    description:
      "Automatically sets adb_enabled=0 and development_settings_enabled=0 in Settings.Global the instant a banking or game app enters the foreground.",
    technicalDetail:
      "Monitors foreground top-package every 1.5s without killing your active wireless ADB daemon or interrupting running Shizuku binders.",
    accent: "primary",
    iconName: "EyeOff",
    span: "normal",
  },
  {
    id: "device-owner",
    category: "enterprise",
    badge: "DPM · DHIZUKU BRIDGE",
    title: "Device Owner & Dhizuku Compatibility",
    description:
      "Activate Device Owner or Profile Owner in one tap via Shizuku, delegate DPM scopes to apps, transfer ownership, or act as a drop-in Dhizuku provider.",
    technicalDetail:
      "Implements DhizukuBinderWrapper, remote transact, and delegated scopes (CERT_INSTALL, PERMISSION_GRANT, APP_RESTRICTIONS, BLOCK_UNINSTALL).",
    accent: "tertiary",
    iconName: "KeyRound",
    span: "wide",
  },
  {
    id: "network-firewall",
    category: "lab",
    badge: "CONNECTIVITY CHAIN",
    title: "Zero-VPN UID Network Firewall",
    description:
      "Block internet access for selected user or system apps at the kernel/framework level without occupying the Android VPN slot.",
    technicalDetail:
      "Uses cmd connectivity set-package-networking-enabled false <pkg> with automatic fallback to iptables -A OUTPUT -m owner --uid-owner.",
    accent: "error",
    iconName: "Flame",
    span: "normal",
  },
  {
    id: "compat-stubs",
    category: "stealth",
    badge: "BRIDGES & STUBS",
    title: "Compatibility Stubs (Shizuku · Sui · Dhizuku)",
    description:
      "Generate lightweight stub APKs when running under a hidden Stealth package name so legacy apps still discover Shizuku, Sui, or Dhizuku providers.",
    technicalDetail:
      "Also creates Shortcut Launcher APKs for hidden system activities without cluttering your launcher.",
    accent: "secondary",
    iconName: "Layers",
    span: "normal",
  },
  {
    id: "watchdog-boot",
    category: "automation",
    badge: "RESILIENCE · 5S WATCHDOG",
    title: "Server Watchdog, Boot Start & Tasker Plugin",
    description:
      "Dual-layer process & native daemon watchdog polls server health every 5 seconds, keeps the Shizuku server alive, and notifies on crashes. Automate via Tasker, MacroDroid, or ADB intents.",
    technicalDetail:
      "5-second health check interval with Wireless ADB auto-start on boot, Wakelock keep-alive, and token-authenticated broadcast actions.",
    accent: "primary",
    iconName: "Activity",
    span: "normal",
  },
]

export const GUIDE_DOCS: GuideDocItem[] = [
  {
    id: "wireless-adb",
    category: "Quick Start",
    title: "Wireless ADB & Auto-Start on Boot",
    summary:
      "Start Shevery on Android 11–17 without a computer using split-screen Wireless Debugging pairing and keep it active across reboots.",
    badge: "Android 11+ · No PC",
    codeTitle: "Manual ADB Start Fallback",
    codeSnippet: `# Start Shevery server via ADB from PC or local terminal
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/starter.sh

# Verify server status & UID
adb shell sh -c "dumpsys activity provider moe.shizuku.privileged.api"`,
    steps: [
      "Enable Developer Options and toggle on Wireless Debugging.",
      "Open Shevery → click Pairing → open Developer Options in Split-Screen or Notification mode.",
      "Tap 'Pair device with pairing code' and enter the 6-digit code in Shevery's notification.",
      "Enable 'Start on boot (Wireless ADB)' and 'Watchdog Service' in Settings so Shevery automatically reconnects whenever Wi-Fi is ready.",
    ],
  },
  {
    id: "comput-ai",
    category: "Comput AI",
    title: "Comput AI Agent & Autonomous Screen Control",
    summary:
      "Configure LLM providers (Gemini OAuth, OpenAI, Claude, Groq, OpenRouter, DeepSeek) and run autonomous multi-step device workflows.",
    badge: "44 Built-in Tools",
    codeTitle: "Comput Agent Tool Capabilities",
    codeSnippet: `# UI & Vision Tools
get_screen_elements(filter="interactive")
tap_element(index=4) / swipe(x1, y1, x2, y2, durationMs=300)
take_screenshot() # MediaProjection hardware frame or screencap

# System & Package Tools
run_shell("cmd package compile -m speed-profile -a")
manage_app_permission(pkg="com.example.app", perm="android.permission.CAMERA", grant=true)
modify_system_setting(namespace="global", key="animator_duration_scale", value="0.5")`,
    steps: [
      "Open the Comput tab in Shevery and tap the AI Provider icon in the top bar.",
      "Select a preset (Google Gemini with 1-click OAuth or API Key, OpenAI, Anthropic, Groq, OpenRouter, or Custom Endpoint).",
      "Switch between Terminal Shell mode (natural language to shell command) or Agent mode (autonomous multi-step GUI + system execution).",
      "Review real-time tool execution cards or approve sensitive commands before they run.",
    ],
  },
  {
    id: "adb-modules-dev",
    category: "ADB Modules",
    title: "Building & Installing Rootless ADB Modules",
    summary:
      "Create Magisk-style ZIP modules that run with ADB/Root permissions via Shevery's declarative api.sh helper functions and WebUI bridge.",
    badge: "api.sh + WebUI",
    codeTitle: "module.prop & service.sh Example",
    codeSnippet: `# module.prop
id=fps_unlocker_adb
name=High Refresh & Animation Tuner
version=v2.1.0
versionCode=21
author=HmnDev
description=Unlocks peak refresh rate and tunes window manager via ADB

# service.sh (api.sh is auto-sourced by Shevery)
ui_print "- Applying display & animation optimizations..."
apply_settings global window_animation_scale 0.5
apply_settings system peak_refresh_rate 120.0
cmd_appops set com.example.heavyapp RUN_IN_BACKGROUND ignore`,
    steps: [
      "Package module.prop, service.sh, post-fs-data.sh, and uninstall.sh at the root of a standard ZIP archive.",
      "Use built-in api.sh helpers (apply_settings, set_prop, pkg_disable, cmd_compile) so Shevery automatically tracks and reverts changes on uninstall.",
      "Optional: bundle a webroot/index.html to provide an interactive Material 3 WebUI powered by window.shizuku.exec().",
      "Publish on GitHub with topic 'adb-module' or 'shizuku-module' to appear automatically in Shevery's built-in Module Catalog.",
    ],
  },
  {
    id: "stealth-usb-hide",
    category: "Stealth & Anti-Detection",
    title: "Stealth Morphing, Stubs & USB Debugging Hide",
    summary:
      "Prevent banking apps, enterprise MDM, and games from detecting Shizuku, Developer Options, or active ADB debugging.",
    badge: "Zero Detection",
    codeTitle: "How USB Debugging Spoofing Works",
    codeSnippet: `# When a monitored banking/game package enters foreground:
settings put global adb_enabled 0
settings put global development_settings_enabled 0

# Your active Wireless ADB / Shizuku server binder remains ALIVE!
# When you leave the protected app, Shevery restores:
settings put global adb_enabled 1
settings put global development_settings_enabled 1`,
    steps: [
      "Go to Settings → Stealth Installation to generate a morphed APK with a random package name (e.g. com.sys.helper.a9f2) and custom app label.",
      "Open Settings → Compatibility Stubs to install lightweight Shizuku / Sui / Dhizuku bridge stubs if legacy apps check hardcoded package names.",
      "Open Settings → Lab Features → Hide USB Debugging and select the apps that check for Developer Options or USB Debugging.",
      "Enable the background guard service — Shevery spoofs the flags in real time only while those apps are in the foreground.",
    ],
  },
  {
    id: "device-owner-dhizuku",
    category: "Device Owner & DPM",
    title: "Device Owner, Delegated Scopes & Dhizuku Mode",
    summary:
      "Elevate Shevery to Device Owner or Profile Owner to unlock enterprise DevicePolicyManager controls and Dhizuku client support.",
    badge: "Enterprise DPM",
    codeTitle: "ADB Activation & Delegated Scopes",
    codeSnippet: `# Activate Shevery as Device Owner via ADB (or 1-tap inside Shevery)
dpm set-device-owner moe.shizuku.privileged.api/moe.shizuku.manager.deviceowner.ShizukuDeviceAdminReceiver

# Available DPM Delegated Scopes for client apps:
- delegation-cert-install
- delegation-permission-grant
- delegation-package-access
- delegation-block-uninstall
- delegation-network-logging`,
    steps: [
      "Navigate to Settings → Device Owner and tap 'Activate via Shizuku' (or run the dpm command over ADB).",
      "Toggle 'Dhizuku Mode' to allow Dhizuku-compatible apps (Hail, OwnDroid, AppOps) to bind directly to Shevery.",
      "Use 'Delegate Scopes' to grant fine-grained DPM capabilities to trusted apps without giving them full Device Owner.",
      "Need to switch managers? Use 'Transfer Ownership' to hand off Device Owner to another receiver without factory resetting.",
    ],
  },
  {
    id: "tasker-intents",
    category: "Automation & Intents",
    title: "Tasker, MacroDroid & Authenticated Broadcasts",
    summary:
      "Control the Shizuku server lifecycle or execute privileged actions from Tasker, MacroDroid, or scripts using token-verified intents.",
    badge: "Token Secured",
    codeTitle: "Broadcast Automation Commands",
    codeSnippet: `# Start Shevery server with your secret token
adb shell am broadcast \\
  -a com.hamondev.shevery.intent.action.START_SERVER \\
  -n moe.shizuku.privileged.api/com.hamondev.shevery.tasker.PluginReceiver \\
  -e auth "YOUR_SECRET_TOKEN"

# Stop or Restart server
-a com.hamondev.shevery.intent.action.STOP_SERVER
-a com.hamondev.shevery.intent.action.RESTART_SERVER`,
    steps: [
      "Enable 'Shevery Connectors' in Settings → Automation (Allow plugins to activate Shevery).",
      "Copy your secret authentication token from Settings → Automation or the Home screen 'View Intents' bottom sheet.",
      "In Tasker or MacroDroid, add an Action → Plugin → Shevery (Start, Stop, or Restart Server) or send a direct broadcast intent.",
      "Unauthorized broadcasts without a valid token are automatically blocked with a deep-link security alert notification.",
    ],
  },
]
