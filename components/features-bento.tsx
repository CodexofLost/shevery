"use client"

import React from "react"
import {
  Sparkles,
  Boxes,
  Shield,
  Wifi,
  EyeOff,
  Flame,
  Lock,
  Workflow,
  Download,
  ExternalLink,
  CheckCircle2,
  Cpu,
  Users,
} from "lucide-react"
import { CONTRIBUTORS, Locale } from "@/lib/shevery-data"

interface FeaturesBentoProps {
  locale: Locale
}

export function FeaturesBento({ locale }: FeaturesBentoProps) {
  const features = [
    {
      icon: <Sparkles className="h-6 w-6" />,
      badge: "AI + Shell",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-7",
      title:
        locale === "ru"
          ? "Консоль Comput и автономный ИИ-агент"
          : "Comput Console & Autonomous AI Agent",
      desc:
        locale === "ru"
          ? "Выполняйте команды в режимах sh, adb и root с подсветкой истории, шаблонами и встроенным ИИ-агентом. Поддерживаются 10 провайдеров (OpenAI, Claude, Gemini, Groq, OpenRouter, DeepSeek, Mistral, Together, SiliconFlow, Custom URL) и безопасный режим подтверждения команд."
          : "Execute commands in sh, adb, and root modes with templates, command history, and an autonomous 5-step AI agent loop. Supports 10 AI providers and Safe Confirm command cards.",
      chips: [
        "sh / adb / root",
        "10 AI Providers",
        "5-Step Agent Loop",
        "Safe Confirm",
      ],
    },
    {
      icon: <Boxes className="h-6 w-6" />,
      badge: "Magisk-like Runtime",
      accent: "bg-[#DECFF8] text-[#24163A]",
      cardBg: "bg-[#1B1828] border-[#3D315B] lg:col-span-5",
      title:
        locale === "ru"
          ? "ADB-модули и WebUI-панели"
          : "ADB Modules & WebUI Dashboards",
      desc:
        locale === "ru"
          ? "Устанавливайте ZIP-модули из каталога GitHub или локально. Поддержка post-fs-data.sh, service.sh, action.sh и полноценных HTML/JS интерфейсов через мост window.shizuku."
          : "Install ZIP modules from the GitHub Catalog or storage. Supports post-fs-data.sh, service.sh, action.sh, and full HTML/JS dashboards powered by window.shizuku.",
      chips: ["window.shizuku JS API", "GitHub Catalog", "SHA-256 Verified"],
    },
    {
      icon: <Wifi className="h-6 w-6" />,
      badge: "Zero-PC Autostart",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-4",
      title:
        locale === "ru"
          ? "Автозапуск по Wi-Fi и Watchdog"
          : "Wireless Boot Autostart & Watchdog",
      desc:
        locale === "ru"
          ? "Автоматический старт при загрузке через Wireless Debugging (TCP 5555) без Root. Watchdog проверяет здоровье сервера каждые 10 секунд и перезапускает при сбоях."
          : "Automatically starts on boot via Wireless Debugging (TCP 5555) without root. Watchdog checks server health every 10 seconds and self-heals on crashes.",
      chips: ["TCP 5555 Boot", "10s Health Check", "ROM Flag Guard"],
    },
    {
      icon: <Shield className="h-6 w-6" />,
      badge: "Dhizuku Inside",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-4",
      title:
        locale === "ru"
          ? "Device Owner и совместимость с Dhizuku"
          : "Device Owner & Dhizuku Compatibility",
      desc:
        locale === "ru"
          ? "Встроенный DhizukuProvider позволяет приложениям Hail, OwnDroid и Amarok работать напрямую через Shevery. Включает редактор Delegated Scopes и передачу прав администратора."
          : "Built-in DhizukuProvider lets Hail, OwnDroid, and Amarok bind directly to Shevery. Includes a Delegated Scopes editor and zero-reset Admin Ownership Transfer.",
      chips: ["DhizukuProvider", "Delegated Scopes", "Admin Transfer"],
    },
    {
      icon: <EyeOff className="h-6 w-6" />,
      badge: "Anti-Detection",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-4",
      title:
        locale === "ru"
          ? "Stealth-сборка и скрытие отладки USB"
          : "Stealth APK & USB Debugging Hide",
      desc:
        locale === "ru"
          ? "Генерация скрытого APK со случайным пакетом com.[4].[5] прямо на телефоне, заглушки совместимости (Compat Stubs) и авто-скрытие отладки по USB при открытии банковских приложений."
          : "On-device Stealth APK generator with randomized com.[4].[5] package ID, Compatibility Stubs, and automatic USB debugging hide when banking apps open.",
      chips: ["Dynamic Package ID", "Compat Stubs", "Auto USB Hide"],
    },
    {
      icon: <Flame className="h-6 w-6" />,
      badge: "Lab & Security",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-6",
      title:
        locale === "ru"
          ? "Сетевой экран, Спец. возможности и Шифрованный бэкап"
          : "Network Firewall, Accessibility Manager & Encrypted Backup",
      desc:
        locale === "ru"
          ? "Системный брандмауэр для блокировки интернета отдельным приложениям (Android 13+), менеджер служб специальных возможностей, биометрическая блокировка входа и бэкап настроек AES-256-GCM (.shizuku)."
          : "System-level per-app network firewall (Android 13+), Accessibility Services manager, biometric app lock, and AES-256-GCM encrypted .shizuku backups.",
      chips: [
        "cmd connectivity Firewall",
        "Accessibility Manager",
        "AES-256-GCM Backup",
      ],
    },
    {
      icon: <Workflow className="h-6 w-6" />,
      badge: "Automation",
      accent: "bg-[#38465E] text-[#B8CCF0]",
      cardBg: "bg-[#151A24] border-[#283347] lg:col-span-6",
      title:
        locale === "ru"
          ? "Плагин Tasker / MacroDroid и Shizuku Connectors"
          : "Tasker / MacroDroid Plugin & Shizuku Connectors",
      desc:
        locale === "ru"
          ? "Нативный плагин действий (START, STOP, RESTART, EXEC_SH, EXEC_ADB, EXEC_ROOT), защищённые токеном широковещательные Intent-вызовы и ContentProvider для внешних активаторов."
          : "Native Tasker Action plugin (START, STOP, RESTART, EXEC_SH, EXEC_ADB, EXEC_ROOT), token-protected broadcasts, and ContentProvider for external activators.",
      chips: ["Tasker Action Plugin", "Token Auth", "LPE Connector API"],
    },
  ]

  return (
    <div className="space-y-24">
      {/* BENTO FEATURES SECTION */}
      <section
        id="features"
        className="pt-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto"
      >
        <div className="text-center max-w-3xl mx-auto mb-14">
          <span className="inline-flex items-center gap-2 rounded-full bg-[#293447] px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-[#B8CCF0]">
            Material 3 Expressive Architecture
          </span>
          <h2 className="mt-4 text-3xl sm:text-5xl font-extrabold tracking-tight text-white">
            {locale === "ru"
              ? "Больше чем Shizuku. Полный контроль над Android."
              : "Beyond Shizuku. Complete Android Control."}
          </h2>
          <p className="mt-4 text-base text-[#9BA7BE] leading-relaxed">
            {locale === "ru"
              ? "Shevery объединяет привилегированный Binder-сервер Shizuku, возможности Device Owner из Dhizuku, модульную среду выполнения и ИИ-консоль в едином интерфейсе Material 3 Expressive."
              : "Shevery unifies the Shizuku privileged binder server, Dhizuku Device Owner APIs, a Magisk-inspired ADB module runtime, and an AI terminal in one Material 3 Expressive experience."}
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-5">
          {features.map((feat, idx) => (
            <div
              key={idx}
              className={`group rounded-[32px] border p-6 sm:p-7 flex flex-col justify-between m3-spring hover:-translate-y-1 hover:border-[#4E607E] ${feat.cardBg}`}
            >
              <div className="space-y-4">
                <div className="flex items-center justify-between gap-3">
                  <div
                    className={`flex h-12 w-12 items-center justify-center rounded-2xl ${feat.accent} m3-spring group-hover:scale-110`}
                  >
                    {feat.icon}
                  </div>
                  <span className="rounded-full bg-[#252E3F] px-3.5 py-1 text-xs font-bold text-[#DCE7FF]">
                    {feat.badge}
                  </span>
                </div>
                <h3 className="text-xl sm:text-2xl font-bold text-white">
                  {feat.title}
                </h3>
                <p className="text-sm text-[#9BA7BE] leading-relaxed">
                  {feat.desc}
                </p>
              </div>

              <div className="mt-6 flex flex-wrap gap-2 pt-2">
                {feat.chips.map((chip) => (
                  <span
                    key={chip}
                    className="rounded-full bg-[#222B3B] border border-[#323F57] px-3 py-1 text-xs font-semibold text-[#B8CCF0]"
                  >
                    {chip}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ANDROID 17 & RELEASE EDITIONS SECTION */}
      <section
        id="downloads"
        className="px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto"
      >
        <div className="rounded-[40px] bg-gradient-to-br from-[#1A2232] via-[#141923] to-[#10141D] border border-[#2E3B52] p-6 sm:p-10 lg:p-12">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            <div className="lg:col-span-5 space-y-5">
              <div className="inline-flex items-center gap-2 rounded-full bg-[#B8CCF0] px-4 py-1.5 text-xs font-extrabold text-[#132238]">
                <Cpu className="h-3.5 w-3.5" />
                Android 8.0 (API 26) — Android 17 (API 36)
              </div>
              <h2 className="text-3xl sm:text-4xl font-extrabold text-white tracking-tight">
                {locale === "ru"
                  ? "Три редакции под любой сценарий"
                  : "Three Editions for Every Workflow"}
              </h2>
              <p className="text-sm sm:text-base text-[#9BA7BE] leading-relaxed">
                {locale === "ru"
                  ? "Начиная с ветки 14.x, Shevery использует чистую схему версий (14.0, 14.1, 14.2…) и проверяет SHA-256 хеш каждого обновления перед установкой."
                  : "Starting with 14.x, Shevery uses a clean versioning scheme (14.0, 14.1, 14.2…) and verifies SHA-256 digests on every in-app update."}
              </p>
              <div className="flex flex-wrap gap-3 pt-2">
                <a
                  href="https://github.com/HmnDev-Tech/shevery/releases/latest"
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2.5 rounded-full bg-[#B8CCF0] px-6 py-3.5 text-sm font-extrabold text-[#132238] m3-spring hover:scale-[1.03] active:scale-95"
                >
                  <Download className="h-4 w-4" />
                  {locale === "ru"
                    ? "Скачать с GitHub Releases"
                    : "Download on GitHub Releases"}
                </a>
                <a
                  href="https://t.me/hmndevtech"
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2 rounded-full bg-[#283347] px-5 py-3.5 text-sm font-bold text-[#DCE7FF] hover:bg-[#38465E] m3-spring"
                >
                  Telegram @hmndevtech
                  <ExternalLink className="h-3.5 w-3.5" />
                </a>
              </div>
            </div>

            <div className="lg:col-span-7 grid grid-cols-1 sm:grid-cols-3 gap-4">
              {/* Edition 1 */}
              <div className="rounded-[28px] bg-[#1D2536] border border-[#34425B] p-5 flex flex-col justify-between space-y-4">
                <div className="space-y-2.5">
                  <span className="inline-block rounded-full bg-[#B8CCF0] px-3 py-0.5 text-[11px] font-extrabold text-[#132238]">
                    Recommended
                  </span>
                  <h3 className="text-lg font-bold text-white">Standard APK</h3>
                  <p className="font-mono text-[11px] text-[#B8CCF0]">
                    com.hamondev.shevery
                  </p>
                  <p className="text-xs text-[#9BA7BE] leading-relaxed">
                    {locale === "ru"
                      ? "Основная сборка с автообновлением, поддержкой всех API Shizuku/Dhizuku, ADB-модулей и консоли Comput."
                      : "Main release with in-app updates, full Shizuku/Dhizuku API support, ADB Modules, and Comput console."}
                  </p>
                </div>
                <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-300">
                  <CheckCircle2 className="h-4 w-4" />
                  SHA-256 Verified
                </div>
              </div>

              {/* Edition 2 */}
              <div className="rounded-[28px] bg-[#1D2536] border border-[#34425B] p-5 flex flex-col justify-between space-y-4">
                <div className="space-y-2.5">
                  <span className="inline-block rounded-full bg-[#DECFF8] px-3 py-0.5 text-[11px] font-extrabold text-[#24163A]">
                    Anti-Detect
                  </span>
                  <h3 className="text-lg font-bold text-white">
                    Stealth Edition
                  </h3>
                  <p className="font-mono text-[11px] text-[#DECFF8]">
                    com.[rand4].[rand5]
                  </p>
                  <p className="text-xs text-[#9BA7BE] leading-relaxed">
                    {locale === "ru"
                      ? "Случайное имя пакета в CI или генерация прямо на устройстве в Настройках для обхода детекта банковских приложений."
                      : "Randomized package ID generated in CI or directly on-device in Settings to evade root/Shizuku detectors."}
                  </p>
                </div>
                <div className="flex items-center gap-1.5 text-xs font-semibold text-[#DECFF8]">
                  <Lock className="h-4 w-4" />
                  On-Device Builder
                </div>
              </div>

              {/* Edition 3 */}
              <div className="rounded-[28px] bg-[#1D2536] border border-[#34425B] p-5 flex flex-col justify-between space-y-4">
                <div className="space-y-2.5">
                  <span className="inline-block rounded-full bg-[#38465E] px-3 py-0.5 text-[11px] font-extrabold text-[#DCE7FF]">
                    Legacy Bridge
                  </span>
                  <h3 className="text-lg font-bold text-white">Compat Stubs</h3>
                  <p className="font-mono text-[11px] text-[#9BA7BE]">
                    moe.shizuku.privileged.api
                  </p>
                  <p className="text-xs text-[#9BA7BE] leading-relaxed">
                    {locale === "ru"
                      ? "Устанавливаемые в 1 клик заглушки Shizuku (v13.6.0) и Dhizuku (v2.9.1) для старых клиентов с жёсткой проверкой пакета."
                      : "One-tap installable Shizuku (v13.6.0) & Dhizuku (v2.9.1) stubs for legacy apps with hardcoded package checks."}
                  </p>
                </div>
                <div className="flex items-center gap-1.5 text-xs font-semibold text-[#B8CCF0]">
                  <CheckCircle2 className="h-4 w-4" />
                  100% Back-Compat
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CONTRIBUTORS & CREDITS SECTION */}
      <section
        id="contributors"
        className="px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto pb-12"
      >
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-10">
          <div>
            <span className="inline-flex items-center gap-2 rounded-full bg-[#293447] px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-[#B8CCF0]">
              <Users className="h-3.5 w-3.5" />
              Open Source Community
            </span>
            <h2 className="mt-3 text-3xl sm:text-4xl font-extrabold text-white">
              {locale === "ru"
                ? "Разработчики и контрибьюторы"
                : "Contributors & Maintainers"}
            </h2>
          </div>
          <a
            href="https://github.com/HmnDev-Tech/shevery/graphs/contributors"
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-2 text-sm font-bold text-[#B8CCF0] hover:underline"
          >
            {locale === "ru" ? "Все участники на GitHub" : "All on GitHub"}
            <ExternalLink className="h-4 w-4" />
          </a>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {CONTRIBUTORS.map((person) => (
            <a
              key={person.name}
              href={person.url}
              target="_blank"
              rel="noreferrer"
              className="group rounded-[26px] bg-[#151A24] border border-[#263042] p-5 hover:border-[#B8CCF0]/60 hover:bg-[#1A212E] m3-spring flex flex-col justify-between gap-3"
            >
              <div>
                <div className="flex items-center justify-between gap-2">
                  <span className="text-base font-bold text-white group-hover:text-[#B8CCF0] transition-colors">
                    {person.name}
                  </span>
                  <ExternalLink className="h-3.5 w-3.5 text-[#9BA7BE] group-hover:text-[#B8CCF0]" />
                </div>
                <div className="text-xs font-mono text-[#B8CCF0]/80 mt-0.5">
                  {person.handle}
                </div>
                <p className="text-xs text-[#9BA7BE] mt-2.5 leading-relaxed">
                  {person.role[locale]}
                </p>
              </div>
            </a>
          ))}
        </div>
      </section>
    </div>
  )
}
