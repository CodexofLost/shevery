export type Locale = "ru" | "en"

export interface ModuleItem {
  id: string
  name: string
  bannerTitle: string
  bannerAccent: string
  version: string
  author: string
  size: string
  trust: string
  hasWebUi: boolean
  hasAction: boolean
  hasService: boolean
  enabledByDefault: boolean
  description: {
    ru: string
    en: string
  }
}

export interface GuideSection {
  id: string
  badge: string
  title: { ru: string; en: string }
  subtitle: { ru: string; en: string }
  highlights: { ru: string[]; en: string[] }
  codeTitle: string
  code: string
  secondaryCodeTitle?: string
  secondaryCode?: string
}

export interface ContributorItem {
  name: string
  handle: string
  url: string
  role: { ru: string; en: string }
}

export const SHEVERY_MODULES: ModuleItem[] = [
  {
    id: "findcve",
    name: "FindCVE",
    bannerTitle: "FindCVE [BETA]",
    bannerAccent: "from-[#0c211f] via-[#11171a] to-[#0d1117] text-[#9ef0e0]",
    version: "2.1.0",
    author: "kerneldroid",
    size: "167.9 KB",
    trust: "Full Trust",
    hasWebUi: true,
    hasAction: true,
    hasService: false,
    enabledByDefault: true,
    description: {
      ru: "Сканер CVE-уязвимостей Android. Проверяет патч безопасности, версию ядра и статус Bluetooth по базе известных уязвимостей. Включает интерактивную WebUI-панель.",
      en: "Android CVE scanner. Checks security patch level, kernel version, and Bluetooth status against known vulnerabilities. Includes WebUI dashboard.",
    },
  },
  {
    id: "shizuconfigs",
    name: "ShizuConfigs",
    bannerTitle: "ShizuConfig",
    bannerAccent: "from-[#21152b] via-[#171222] to-[#0f1117] text-[#decff8]",
    version: "1.4.2",
    author: "HmnDev-Tech",
    size: "242.0 KB",
    trust: "Full Trust",
    hasWebUi: true,
    hasAction: true,
    hasService: true,
    enabledByDefault: true,
    description: {
      ru: "Системный твикер с WebUI. Управление пакетами, системными таблицами Settings (System/Secure/Global), частотой экрана, производительностью, сетью и кастомными скриптами.",
      en: "System tweaker with WebUI. Manage packages, settings tables, display refresh rate, performance profiles, network, and custom commands.",
    },
  },
  {
    id: " phantom-guard",
    name: "PhantomProcessKiller-Off",
    bannerTitle: "PhantomFree",
    bannerAccent: "from-[#142238] via-[#121926] to-[#0e1117] text-[#b8ccf0]",
    version: "1.1.0",
    author: "CodexofLost",
    size: "48.4 KB",
    trust: "Verified",
    hasWebUi: false,
    hasAction: true,
    hasService: true,
    enabledByDefault: false,
    description: {
      ru: "Автоматически снимает лимит max_phantom_processes (Android 12–17) при каждом запуске сервера, предотвращая закрытие фоновых сессий Termux и контейнеров.",
      en: "Automatically disables max_phantom_processes limits on Android 12–17 at boot, preventing background kills of Termux and Linux containers.",
    },
  },
]

export const SHEVERY_GUIDES: GuideSection[] = [
  {
    id: "quickstart",
    badge: "ADB / Wireless / Root",
    title: {
      ru: "Запуск и автостарт при загрузке",
      en: "Startup & Wireless Boot Autostart",
    },
    subtitle: {
      ru: "Три способа запуска привилегированного сервера + автоматическое восстановление после перезагрузки и защита от сбоев.",
      en: "Three ways to launch the privileged server plus automatic boot recovery and self-healing Watchdog.",
    },
    highlights: {
      ru: [
        "Беспроводная отладка (Android 11+): сопряжение по 6-значному коду прямо из шторки уведомлений без ПК.",
        "Автозапуск при загрузке (Start on boot via Wireless debugging): временно поднимает TCP-порт 5555 и запускает сервер без Root.",
        "Watchdog (каждые 10 сек): проверяет здоровье процесса и транзакционные сбои (DeadSystemException), автоматически перезапуская Shevery.",
        "Re-assert wireless debugging: защищает флаг беспроводной отладки, если прошивка (OEM ROM) сбрасывает его при переподключении Wi-Fi.",
      ],
      en: [
        "Wireless Debugging (Android 11+): pair with a 6-digit code straight from the notification shade—no PC required.",
        "Start on boot via Wireless debugging: briefly enables TCP port 5555 on boot and starts Shevery without root.",
        "Watchdog (every 10s): monitors server process health and DeadSystemException errors, restarting automatically.",
        "Re-assert wireless debugging: restores the wireless debugging flag if the OEM ROM clears it on Wi-Fi reconnect.",
      ],
    },
    codeTitle: "ADB Start Command (Non-Root)",
    code: `adb shell sh /sdcard/Android/data/com.hamondev.shevery/files/start.sh

# Для Stealth-сборки со случайным именем пакета:
adb shell sh /sdcard/Android/data/<stealth_package>/files/start.sh`,
    secondaryCodeTitle: "Write Secure Settings (для автостарта по Wi-Fi без Root)",
    secondaryCode: `adb shell pm grant com.hamondev.shevery android.permission.WRITE_SECURE_SETTINGS`,
  },
  {
    id: "comput-ai",
    badge: "Comput & AI Agent",
    title: {
      ru: "Консоль Comput и автономный ИИ-агент",
      en: "Comput Console & Autonomous AI Agent",
    },
    subtitle: {
      ru: "Встроенный терминал с режимами sh, adb, root и автономным ИИ-агентом, способным диагностировать систему в цикле до 5 шагов.",
      en: "Built-in terminal supporting sh, adb, and root modes with a multi-step AI agent loop and 10 provider presets.",
    },
    highlights: {
      ru: [
        "Режимы выполнения: sh (локальная оболочка), adb (через привилегированный Shizuku user service) и root (su).",
        "Поддержка 10 провайдеров: OpenAI, Anthropic Claude, Google Gemini, Groq, OpenRouter, Together AI, Mistral, DeepSeek, SiliconFlow и Custom URL.",
        "Режим ИИ-агента (до 5 итераций): модель генерирует команду в теге <cmd>...</cmd>, получает stdout/stderr и продолжает анализ до финального ответа.",
        "Режим Safe Confirm: перед исполнением каждой предложенной агентом команды выводится карточка подтверждения с кнопками Run / Cancel.",
      ],
      en: [
        "Execution modes: sh (local shell), adb (via privileged Shizuku user service), and root (su).",
        "10 built-in provider presets: OpenAI, Anthropic Claude, Google Gemini, Groq, OpenRouter, Together AI, Mistral, DeepSeek, SiliconFlow, and Custom Endpoint.",
        "Autonomous 5-step Agent loop: emits <cmd>...</cmd>, inspects live stdout/stderr, and iterates until the task is solved.",
        "Safe Confirm toggle: review every proposed command with interactive Run / Cancel controls before execution.",
      ],
    },
    codeTitle: "Comput Agent Tool Protocol",
    code: `# Запрос пользователя в Comput Agent:
"Проверь уровень патча безопасности, статус SELinux и топ-3 процесса по RAM"

# Шаг 1 — Агент генерирует команду:
<cmd>getprop ro.build.version.security_patch && getenforce && ps -A --sort=-rss | head -n 4</cmd>

# Шаг 2 — Comput исполняет команду в выбранном режиме (sh / adb / root)
# и возвращает stdout обратно в контекст модели для итогового отчёта.`,
  },
  {
    id: "adb-modules",
    badge: "ADB Modules & WebUI",
    title: {
      ru: "ADB-модули, каталог GitHub и WebUI API",
      en: "ADB Modules, GitHub Catalog & WebUI API",
    },
    subtitle: {
      ru: "Модульная система в стиле Magisk/KernelSU, работающая поверх ADB и Root с поддержкой HTML/JS панелей управления.",
      en: "Magisk/KernelSU-inspired module runtime working over ADB and Root with a JavaScript WebUI bridge.",
    },
    highlights: {
      ru: [
        "Жизненный цикл скриптов: post-fs-data.sh (синхронный старт), service.sh (фоновый демон), boot-completed.sh, action.sh (кнопка Action) и uninstall.sh.",
        "Встроенный WebView-мост window.shizuku: exec(), execAsync(), getProp(), setProp(), readFile(), writeFile(), listPackages(), toast().",
        "Хранилище модулей: /data/local/tmp/shizuku_modules/<id> с поддержкой изолированного KV-хранилища и логов.",
        "Каталог модулей с проверкой уровня доверия (Full Trust / Verified), SHA-256 хешей и подписи ZIP-архивов.",
      ],
      en: [
        "Lifecycle scripts: post-fs-data.sh (sync pre-start), service.sh (background daemon), boot-completed.sh, action.sh (Action button), and uninstall.sh.",
        "Built-in WebView bridge window.shizuku: exec(), execAsync(), getProp(), setProp(), readFile(), writeFile(), listPackages(), toast().",
        "Runtime directory: /data/local/tmp/shizuku_modules/<id> with isolated module KV storage and live logs.",
        "GitHub Module Catalog with trust badges (Full Trust / Verified), SHA-256 verification, and automatic updates.",
      ],
    },
    codeTitle: "WebUI JavaScript Bridge (webroot/index.html)",
    code: `// Выполнение привилегированной команды из WebUI модуля
const patch = window.shizuku.getProp("ro.build.version.security_patch");
window.shizuku.execAsync("cmd package list packages -3", "onPackagesLoaded");

window.onPackagesLoaded = function(resultJson) {
  const res = JSON.parse(resultJson); // { code, stdout, stderr }
  if (res.code === 0) {
    window.shizuku.toast("Найдено пакетов: " + res.stdout.split("\\n").length);
  }
};`,
    secondaryCodeTitle: "Структура ZIP-пакета модуля",
    secondaryCode: `my-module.zip
├── module.prop          # id, name, version, versionCode, author, description
├── post-fs-data.sh      # Синхронная инициализация
├── service.sh           # Фоновый сервис модуля
├── action.sh            # Запускается кнопкой "Action" в карточке
└── webroot/
    └── index.html       # Открывается кнопкой "WebUI"`,
  },
  {
    id: "device-owner",
    badge: "Dhizuku & DPM",
    title: {
      ru: "Device Owner, Profile Owner и совместимость с Dhizuku",
      en: "Device Owner, Profile Owner & Dhizuku Compatibility",
    },
    subtitle: {
      ru: "Полная поддержка привилегий администратора устройства и бесшовная работа с приложениями экосистемы Dhizuku.",
      en: "Native DevicePolicyManager control and built-in DhizukuBinderWrapper for existing Dhizuku client apps.",
    },
    highlights: {
      ru: [
        "Встроенный провайдер Dhizuku: клиенты (Hail, OwnDroid, Amarok, App Ops) подключаются к Shevery напрямую.",
        "Активация в 1 клик через Shizuku/Root или классической командой adb shell dpm set-device-owner.",
        "Редактор делегированных прав (Delegated Scopes): выдача DELEGATION_PACKAGE_ACCESS, BLOCK_UNINSTALL, PERMISSION_GRANT, NETWORK_LOGGING и др.",
        "Передача прав администратора (Transfer Ownership) другому приложению через API Android 9+ без сброса к заводским настройкам.",
      ],
      en: [
        "Built-in Dhizuku provider: client apps (Hail, OwnDroid, Amarok, App Ops) bind to Shevery out of the box.",
        "One-tap activation via active Shizuku/Root or via standard adb shell dpm set-device-owner.",
        "Delegated Scopes editor: grant DELEGATION_PACKAGE_ACCESS, BLOCK_UNINSTALL, PERMISSION_GRANT, NETWORK_LOGGING, and more per app.",
        "Zero-reset Ownership Transfer (Android 9+) to migrate Device Owner or Profile Owner to another admin receiver.",
      ],
    },
    codeTitle: "Device Owner & Profile Owner Commands",
    code: `# Назначение Device Owner:
adb shell dpm set-device-owner com.hamondev.shevery/moe.shizuku.manager.deviceowner.ShizukuDeviceAdminReceiver

# Назначение Profile Owner (для основного или рабочего профиля):
adb shell dpm set-profile-owner --user 0 com.hamondev.shevery/moe.shizuku.manager.deviceowner.ShizukuDeviceAdminReceiver`,
  },
  {
    id: "stealth-stubs",
    badge: "Stealth & Stubs",
    title: {
      ru: "Stealth-режим, скрытие отладки и заглушки совместимости",
      en: "Stealth Mode, USB Hide & Compatibility Stubs",
    },
    subtitle: {
      ru: "Обход жёстких проверок банковских и защищённых приложений без потери совместимости со старыми клиентами Shizuku и Dhizuku.",
      en: "Bypass aggressive app detections while preserving 100% compatibility with apps that hard-check original package names.",
    },
    highlights: {
      ru: [
        "On-demand генератор Stealth APK: прямо на устройстве перепаковывает AndroidManifest.xml и resources.arsc со случайным именем пакета com.[4].[5] и вашим названием.",
        "Заглушки совместимости (Compat Stubs): лёгкие APK с оригинальными пакетами moe.shizuku.privileged.api (v13.6.0) и com.rosan.dhizuku (v2.9.1) для приложений с жёсткой проверкой isPackageInstalled.",
        "Скрытие отладки по USB и Для разработчиков: автоматически выключает adb_enabled и development_settings_enabled при запуске выбранных банковских приложений и возвращает обратно при выходе из них.",
        "Сетевой брандмауэр (Android 13+): блокировка доступа в интернет для выбранных приложений на уровне системы через cmd connectivity set-package-networking-enabled.",
      ],
      en: [
        "On-device Stealth APK generator: rewrites binary AndroidManifest.xml & resources.arsc with a randomized com.[4].[5] package name and custom label.",
        "Compatibility Stubs: lightweight placeholder APKs for moe.shizuku.privileged.api (v13.6.0) and com.rosan.dhizuku (v2.9.1) to satisfy hardcoded package checks.",
        "USB Debugging & Developer Options Hide: automatically disables adb_enabled when selected apps enter the foreground and restores it once they close.",
        "System Network Firewall (Android 13+): per-app internet cut-off via cmd connectivity set-package-networking-enabled.",
      ],
    },
    codeTitle: "Compatibility Architecture",
    code: `# Приложения, проверяющие наличие оригинального пакета Shizuku / Dhizuku:
[Client App] ---> PackageManager.getPackageInfo("moe.shizuku.privileged.api")
                       └── Находит установленный Shizuku Compat Stub (v13.6.0, code 1086)
[Client App] ---> ContentResolver.call("moe.shizuku.privileged.api.binder")
                       └── Напрямую связывается с привилегированным сервером Shevery!`,
  },
  {
    id: "automation",
    badge: "Tasker & Connectors",
    title: {
      ru: "Автоматизация Tasker / MacroDroid и Shizuku Connectors",
      en: "Tasker / MacroDroid Automation & Shizuku Connectors",
    },
    subtitle: {
      ru: "Нативный плагин действия Tasker, защищённые токеном широковещательные Intent-команды и ContentProvider для внешних активаторов.",
      en: "Native Tasker Action plugin, token-authenticated broadcast intents, and ContentProvider for external LPE activators.",
    },
    highlights: {
      ru: [
        "Нативный плагин Tasker / MacroDroid: действия START, STOP, RESTART, EXEC_SH, EXEC_ADB и EXEC_ROOT с возвратом %err и %errmsg.",
        "Защита токеном: прямые широковещательные вызовы требуют секретный токен -e auth <token> и включённый тумблер Shevery Connectors.",
        "Уведомления безопасности: при отклонённом вызове Shevery показывает кликабельное уведомление с переходом в Settings → Automation.",
        "Shizuku Connectors API: URI content://moe.shizuku.privileged.api.connector (методы get_Start_Command и notify_Started) для внешних эксплойт-активаторов.",
      ],
      en: [
        "Native Tasker / MacroDroid plugin: START, STOP, RESTART, EXEC_SH, EXEC_ADB, and EXEC_ROOT actions returning %err and %errmsg.",
        "Token authentication: direct broadcasts require -e auth <token> and Shevery Connectors enabled in Settings → Automation.",
        "Security alerts: unauthorized broadcast attempts trigger a tap-to-configure notification linking to Settings → Automation.",
        "Shizuku Connectors API: content://moe.shizuku.privileged.api.connector (get_Start_Command & notify_Started) for external LPE activators.",
      ],
    },
    codeTitle: "ADB / Termux Broadcast Automation",
    code: `# Перезапуск сервера Shevery:
adb shell am broadcast \\
  -a com.hamondev.shevery.action.CONTROL \\
  -n com.hamondev.shevery/com.hamondev.shevery.tasker.PluginReceiver \\
  -e command RESTART \\
  -e auth "<ВАШ_ТОКЕН_АВТОМАТИЗАЦИИ>"

# Выполнение привилегированной ADB-команды с возвратом stdout:
adb shell am broadcast \\
  -a com.hamondev.shevery.action.CONTROL \\
  -n com.hamondev.shevery/com.hamondev.shevery.tasker.PluginReceiver \\
  -e command EXEC_ADB \\
  -e script "cmd package compile -m speed-profile -a" \\
  -e auth "<ВАШ_ТОКЕН_АВТОМАТИЗАЦИИ>"`,
  },
]

export const CONTRIBUTORS: ContributorItem[] = [
  {
    name: "HmnDev-Tech",
    handle: "@HmnDev-Tech",
    url: "https://github.com/HmnDev-Tech",
    role: {
      ru: "Руководитель проекта — релизы, CI/CD, центр настроек, Watchdog, автообновление, документация и архитектура",
      en: "Project lead — releases, CI, settings hub, watchdog, self-update, docs and project direction",
    },
  },
  {
    name: "Landon Moran",
    handle: "@LandonMoran",
    url: "https://github.com/LandonMoran",
    role: {
      ru: "Core-разработчик — ИИ-провайдеры и Commandium, Material 3 Expressive, защита ADB, миграция на Compose, i18n",
      en: "Core developer — AI providers and Commandium, Material 3 Expressive, ADB hardening, Compose migrations, i18n",
    },
  },
  {
    name: "kerneldroid",
    handle: "@kerneldroid",
    url: "https://github.com/kerneldroid",
    role: {
      ru: "Редизайн консоли Comput, UI каталога модулей, плагин Tasker, Legacy API Stub, подпись и установка модулей",
      en: "Comput console redesign, module catalog UI, Tasker plugin, legacy API stub, CI signing and module install fixes",
    },
  },
  {
    name: "OptionString",
    handle: "@superman32432432",
    url: "https://github.com/superman32432432",
    role: {
      ru: "Плавающая навигация Material 3 Expressive, редизайн Comput, иконки, архитектура настроек и релизный пайплайн",
      en: "Expressive floating navigation, Comput M3 redesign, launcher icons, settings layout and release workflow",
    },
  },
  {
    name: "CodexofLost",
    handle: "@CodexofLost",
    url: "https://github.com/CodexofLost",
    role: {
      ru: "Токены Material 3, надёжность беспроводного ADB и стартера, авторизация привилегий и исправления Watchdog",
      en: "Material 3 tokens, wireless ADB and starter reliability, permission auth, watchdog and server startup fixes",
    },
  },
  {
    name: "arysm4a",
    handle: "@tim1540",
    url: "https://github.com/tim1540",
    role: {
      ru: "Автозапуск по беспроводной отладке при загрузке, заглушки Dhizuku, биометрическая защита и Jetpack Compose UI",
      en: "Wireless-debugging boot autostart, Dhizuku compatibility stubs, biometric auth & session security, Compose UI/UX",
    },
  },
  {
    name: "Rikka",
    handle: "@RikkaApps",
    url: "https://github.com/RikkaApps",
    role: {
      ru: "Создатель оригинального проекта Shizuku — фундамента, на котором построен Shevery",
      en: "Original Shizuku project — the foundation Shevery is built on",
    },
  },
  {
    name: "iamr0s",
    handle: "@iamr0s",
    url: "https://github.com/iamr0s",
    role: {
      ru: "Создатель проекта Dhizuku — архитектура Device Owner и совместного использования DPM API",
      en: "Dhizuku project — Device Owner architecture, API sharing and management integrated into Shevery",
    },
  },
]
