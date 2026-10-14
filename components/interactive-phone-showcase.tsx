"use client"

import React, { useState } from "react"
import {
  Check,
  Terminal,
  Wifi,
  Send,
  LayoutGrid,
  Settings,
  Home,
  Sparkles,
  RefreshCw,
  Code2,
  Play,
  ExternalLink,
  Trash2,
  Search,
  History,
  SlidersHorizontal,
  Copy,
  X,
  Upload,
  Smartphone,
  Image as ImageIcon,
  ShieldCheck,
} from "lucide-react"
import {
  ExpressiveSwitch,
  ExpressiveFloatingToolbar,
} from "./m3-expressive-primitives"
import { Locale, SHEVERY_MODULES } from "@/lib/shevery-data"

interface InteractivePhoneShowcaseProps {
  locale: Locale
}

type ScreenTab = "home" | "modules" | "comput" | "settings"

export function InteractivePhoneShowcase({
  locale,
}: InteractivePhoneShowcaseProps) {
  const [activeTab, setActiveTab] = useState<ScreenTab>("home")
  const [viewMode, setViewMode] = useState<"interactive" | "screenshot">(
    "interactive"
  )

  // Home screen state
  const [serviceRunning, setServiceRunning] = useState(true)
  const [serviceMode, setServiceMode] = useState<"adb" | "root">("adb")
  const [authorizedCount, setAuthorizedCount] = useState(4)

  // Modules state
  const [moduleStates, setModuleStates] = useState<Record<string, boolean>>({
    findcve: true,
    shizuconfigs: true,
    " phantom-guard": false,
  })
  const [moduleToast, setModuleToast] = useState<string | null>(null)

  // Comput console state
  const [shellMode, setShellMode] = useState<"sh" | "adb" | "root">("adb")
  const [commandInput, setCommandInput] = useState("pm list packages -3")
  const [consoleOutput, setConsoleOutput] = useState<string[]>([
    "Console initialized. Privileged Shevery service connected.",
    "Ready for commands.",
  ])
  const [aiThinking, setAiThinking] = useState(false)

  // Settings state
  const [bootWireless, setBootWireless] = useState(true)
  const [watchdogEnabled, setWatchdogEnabled] = useState(true)
  const [reassertWireless, setReassertWireless] = useState(true)
  const [compatStubs, setCompatStubs] = useState(true)

  const runQuickCommand = (cmd: string) => {
    setCommandInput(cmd)
    let output: string[] = []
    if (cmd.includes("pm list packages")) {
      output = [
        `$ [${shellMode}] ${cmd}`,
        "package:com.hamondev.shevery",
        "package:com.termux",
        "package:com.aistra.hail",
        "package:com.rosan.dhizuku.stub",
        "→ Exit code: 0 (4 packages)",
      ]
    } else if (cmd.includes("dumpsys battery")) {
      output = [
        `$ [${shellMode}] ${cmd}`,
        "Current Battery Service state:",
        "  AC powered: false | USB powered: false | Wireless: false",
        "  status: 2 | health: 2 | level: 92 | temperature: 294 (29.4°C)",
        "→ Exit code: 0",
      ]
    } else if (cmd.includes("security_patch")) {
      output = [
        `$ [${shellMode}] ${cmd}`,
        "ro.build.version.security_patch = 2026-10-05",
        "ro.build.version.release = 16 (Baklava / Android 17 Preview ready)",
        "SELinux status: Enforcing",
      ]
    } else {
      output = [
        `$ [${shellMode}] ${cmd}`,
        `Executed via Shevery IShizukuService (${shellMode.toUpperCase()} UID ${
          shellMode === "root" ? "0" : "2000"
        })`,
        "→ Operation completed successfully (exit 0).",
      ]
    }
    setConsoleOutput(output)
  }

  const runComputAiAgent = () => {
    setAiThinking(true)
    setConsoleOutput([
      "✦ Comput AI Agent (Step 1/2) — Analyzing system state...",
      "<cmd>getprop ro.build.version.security_patch && dumpsys deviceidle | grep mState</cmd>",
    ])
    setTimeout(() => {
      setAiThinking(false)
      setConsoleOutput([
        "✦ Comput AI Agent (Step 1/2):",
        "$ getprop ro.build.version.security_patch && dumpsys deviceidle | grep mState",
        "2026-10-05 | mState=ACTIVE",
        "",
        "✦ Agent Summary:",
        locale === "ru"
          ? "Сервер Shevery v14.1 работает стабильно (UID 2000 ADB). Патч безопасности актуальный (2026-10-05), Watchdog активен."
          : "Shevery v14.1 server is healthy (UID 2000 ADB). Security patch is current (2026-10-05), Watchdog is active.",
      ])
    }, 650)
  }

  const triggerModuleAction = (msg: string) => {
    setModuleToast(msg)
    setTimeout(() => setModuleToast(null), 2600)
  }

  const screenshotMap: Record<ScreenTab, { src: string; alt: string }> = {
    home: {
      src: "/screenshots/main.png",
      alt: "Shevery Home Screen Material 3 Expressive",
    },
    modules: {
      src: "/screenshots/modules.png",
      alt: "Shevery ADB Modules Screen",
    },
    comput: {
      src: "/screenshots/comput.png",
      alt: "Shevery Comput Console Screen",
    },
    settings: {
      src: "/screenshots/settings.png",
      alt: "Shevery Settings Screen",
    },
  }

  const navTabs = [
    {
      id: "home",
      label: "Shevery",
      icon: <Home className="h-4 w-4" />,
    },
    {
      id: "modules",
      label: "ADB Modules",
      icon: <LayoutGrid className="h-4 w-4" />,
    },
    {
      id: "comput",
      label: "Comput",
      icon: <Terminal className="h-4 w-4" />,
    },
    {
      id: "settings",
      label: "Settings",
      icon: <Settings className="h-4 w-4" />,
    },
  ]

  return (
    <div className="flex flex-col items-center">
      {/* Mode Switcher Pill */}
      <div className="mb-4 inline-flex items-center gap-1.5 rounded-full bg-[#181E29] p-1.5 border border-[#2C374B]">
        <button
          type="button"
          onClick={() => setViewMode("interactive")}
          className={`flex items-center gap-2 rounded-full px-4 py-1.5 text-xs font-semibold m3-spring cursor-pointer ${
            viewMode === "interactive"
              ? "bg-[#B8CCF0] text-[#132238] shadow-sm"
              : "text-[#9BA7BE] hover:text-white"
          }`}
        >
          <Smartphone className="h-3.5 w-3.5" />
          {locale === "ru" ? "Интерактивное демо" : "Interactive Live UI"}
        </button>
        <button
          type="button"
          onClick={() => setViewMode("screenshot")}
          className={`flex items-center gap-2 rounded-full px-4 py-1.5 text-xs font-semibold m3-spring cursor-pointer ${
            viewMode === "screenshot"
              ? "bg-[#B8CCF0] text-[#132238] shadow-sm"
              : "text-[#9BA7BE] hover:text-white"
          }`}
        >
          <ImageIcon className="h-3.5 w-3.5" />
          {locale === "ru" ? "Оригинальные скриншоты" : "Real App Screenshots"}
        </button>
      </div>

      {/* Android Device Frame */}
      <div className="relative w-full max-w-[380px] rounded-[44px] border-[6px] border-[#252E3F] bg-[#0E1117] shadow-[0_28px_90px_rgba(0,0,0,0.75),0_0_60px_rgba(184,204,240,0.09)] overflow-hidden">
        {/* Android Status Bar */}
        <div className="flex items-center justify-between px-6 pt-3 pb-1 text-[11px] font-medium text-[#9BA7BE] bg-[#0E1117] select-none">
          <span>14:00</span>
          <div className="h-2.5 w-2.5 rounded-full bg-[#1C2331] border border-[#2E394E]" />
          <div className="flex items-center gap-1.5">
            <span>5G</span>
            <span>92%</span>
          </div>
        </div>

        {viewMode === "screenshot" ? (
          <div className="relative h-[650px] w-full bg-[#0E1117] flex flex-col justify-between">
            <img
              src={screenshotMap[activeTab].src}
              alt={screenshotMap[activeTab].alt}
              className="h-full w-full object-cover object-top transition-opacity duration-300"
            />
            {/* Interactive overlay switcher at bottom of screenshot */}
            <div className="absolute inset-x-0 bottom-4 flex justify-center z-20 px-2">
              <ExpressiveFloatingToolbar
                tabs={navTabs}
                activeTab={activeTab}
                onSelect={(id) => setActiveTab(id as ScreenTab)}
              />
            </div>
          </div>
        ) : (
          <div className="relative h-[650px] flex flex-col justify-between bg-[#0E1117] text-[#E5EAF5]">
            {/* Scrollable Screen Content */}
            <div className="flex-1 overflow-y-auto px-4 pt-2 pb-24 space-y-3.5">
              {/* TAB 1: HOME SCREEN (matching main.png) */}
              {activeTab === "home" && (
                <div className="space-y-3.5 animate-in fade-in duration-200">
                  <div className="flex items-center justify-between pt-1 pb-1">
                    <h3 className="text-[22px] font-semibold tracking-tight text-white">
                      Shevery
                    </h3>
                    <button
                      type="button"
                      onClick={() =>
                        setServiceMode(serviceMode === "adb" ? "root" : "adb")
                      }
                      className="rounded-full bg-[#1C2331] px-2.5 py-1 text-[11px] font-medium text-[#B8CCF0] border border-[#2E3A50] cursor-pointer hover:bg-[#252F42]"
                    >
                      Mode: {serviceMode.toUpperCase()}
                    </button>
                  </div>

                  {/* Hero Status Expressive Card */}
                  <div
                    className={`rounded-[28px] p-5 m3-spring ${
                      serviceRunning
                        ? "bg-[#3A4A63] text-white"
                        : "bg-[#3B262C] text-[#FFD9DF]"
                    }`}
                  >
                    <div className="flex items-center gap-4">
                      <div
                        className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-full m3-spring ${
                          serviceRunning
                            ? "bg-[#B8CCF0] text-[#1A2942]"
                            : "bg-[#FFB2BE] text-[#471520]"
                        }`}
                      >
                        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-[#243248] text-[#B8CCF0]">
                          {serviceRunning ? (
                            <Check className="h-3.5 w-3.5 stroke-[3]" />
                          ) : (
                            <X className="h-3.5 w-3.5 stroke-[3]" />
                          )}
                        </div>
                      </div>
                      <div>
                        <div className="text-lg font-semibold leading-snug">
                          {serviceRunning
                            ? locale === "ru"
                              ? "Shevery запущен"
                              : "Shevery is running"
                            : locale === "ru"
                              ? "Сервер остановлен"
                              : "Shevery is stopped"}
                        </div>
                        <div className="text-xs text-[#C7D6F0]/80 mt-0.5">
                          Version 14.1, {serviceMode} (API 36 ready)
                        </div>
                      </div>
                    </div>

                    <div className="mt-4 flex flex-wrap gap-2.5">
                      <button
                        type="button"
                        onClick={() =>
                          setAuthorizedCount((c) => (c >= 8 ? 4 : c + 1))
                        }
                        className="inline-flex items-center gap-2 rounded-full bg-[#B8CCF0] px-4 py-2.5 text-xs font-bold text-[#14233C] m3-spring hover:scale-[1.02] active:scale-95 cursor-pointer"
                      >
                        <LayoutGrid className="h-3.5 w-3.5" />
                        {locale === "ru" ? "Приложения" : "Manage apps"} (
                        {authorizedCount})
                      </button>

                      <button
                        type="button"
                        onClick={() => setServiceRunning(!serviceRunning)}
                        className="inline-flex items-center gap-2 rounded-full bg-[#293447] px-4 py-2.5 text-xs font-semibold text-[#DCE7FF] m3-spring hover:bg-[#222B3B] active:scale-95 cursor-pointer"
                      >
                        {serviceRunning ? (
                          <>
                            <X className="h-3.5 w-3.5" />
                            {locale === "ru" ? "Остановить" : "Stop Shevery"}
                          </>
                        ) : (
                          <>
                            <Play className="h-3.5 w-3.5" />
                            {locale === "ru" ? "Запустить" : "Start Shevery"}
                          </>
                        )}
                      </button>
                    </div>
                  </div>

                  {/* Expressive Action Pills Row */}
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => setActiveTab("comput")}
                      className="inline-flex items-center gap-2 rounded-full bg-[#263040] px-4 py-2.5 text-xs font-semibold text-[#DCE7FF] hover:bg-[#313E52] m3-spring cursor-pointer"
                    >
                      <Terminal className="h-3.5 w-3.5 text-[#B8CCF0]" />
                      Terminal
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setServiceRunning(true)
                        setServiceMode("adb")
                      }}
                      className="inline-flex items-center gap-2 rounded-full bg-[#263040] px-4 py-2.5 text-xs font-semibold text-[#DCE7FF] hover:bg-[#313E52] m3-spring cursor-pointer"
                    >
                      <Wifi className="h-3.5 w-3.5 text-[#B8CCF0]" />
                      Wireless ADB
                    </button>
                    <button
                      type="button"
                      onClick={() => setActiveTab("settings")}
                      className="inline-flex items-center gap-2 rounded-full bg-[#263040] px-4 py-2.5 text-xs font-semibold text-[#DCE7FF] hover:bg-[#313E52] m3-spring cursor-pointer"
                    >
                      <Send className="h-3.5 w-3.5 text-[#B8CCF0]" />
                      Pairing
                    </button>
                  </div>

                  <div className="pt-1 text-xs font-bold text-[#9BA7BE] px-1">
                    {locale === "ru"
                      ? "Пошаговое руководство"
                      : "Step-by-step guide"}
                  </div>

                  {/* Authorized Applications Card */}
                  <div
                    onClick={() =>
                      setAuthorizedCount((c) => (c >= 8 ? 4 : c + 1))
                    }
                    className="flex items-center gap-3.5 rounded-[24px] bg-[#161B24] p-4 border border-[#232B3A] hover:border-[#38465E] m3-spring cursor-pointer"
                  >
                    <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#38465E] text-[#DCE7FF]">
                      <LayoutGrid className="h-4 w-4" />
                    </div>
                    <div>
                      <div className="text-sm font-bold text-white">
                        {locale === "ru"
                          ? `Авторизовано приложений: ${authorizedCount}`
                          : `Authorized ${authorizedCount} applications`}
                      </div>
                      <div className="text-xs text-[#9BA7BE]">
                        {locale === "ru"
                          ? "Нажмите для управления правами Shizuku и Dhizuku"
                          : "Tap to manage authorized Shizuku & Dhizuku apps"}
                      </div>
                    </div>
                  </div>

                  {/* Start via Computer / Wireless Card */}
                  <div className="rounded-[24px] bg-[#161B24] p-4 border border-[#232B3A] space-y-3">
                    <div className="flex items-start gap-3.5">
                      <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#38465E] text-[#DCE7FF]">
                        <Code2 className="h-4 w-4" />
                      </div>
                      <div>
                        <div className="text-sm font-bold text-white">
                          {locale === "ru"
                            ? "Запуск через Wireless ADB или ПК"
                            : "Start via Wireless ADB or Computer"}
                        </div>
                        <p className="text-xs text-[#9BA7BE] mt-1 leading-relaxed">
                          {locale === "ru"
                            ? "Для устройств без Root используйте беспроводную отладку (Android 11+) с автозапуском при загрузке или скрипт start.sh."
                            : "For non-root devices, start via Wireless debugging (with boot autostart) or adb start.sh."}
                        </p>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => setActiveTab("comput")}
                      className="inline-flex items-center gap-1.5 rounded-full bg-[#B8CCF0] px-3.5 py-1.5 text-xs font-bold text-[#14233C] cursor-pointer"
                    >
                      <Code2 className="h-3.5 w-3.5" />
                      {locale === "ru" ? "Открыть консоль" : "View command"}
                    </button>
                  </div>
                </div>
              )}

              {/* TAB 2: ADB MODULES SCREEN (matching modules.png) */}
              {activeTab === "modules" && (
                <div className="space-y-3.5 animate-in fade-in duration-200">
                  <div className="flex items-center justify-between pt-1 pb-1">
                    <h3 className="text-[19px] font-semibold text-white truncate">
                      ADB Modules
                    </h3>
                    <div className="flex items-center gap-1.5">
                      <div className="inline-flex items-center rounded-full bg-[#1C222E] p-1 border border-[#2B3548]">
                        <span className="flex h-6 w-7 items-center justify-center rounded-full bg-[#38465E] text-[#DCE7FF]">
                          <Check className="h-3.5 w-3.5" />
                        </span>
                        <span className="flex h-6 w-7 items-center justify-center text-[#9BA7BE]">
                          <LayoutGrid className="h-3.5 w-3.5" />
                        </span>
                      </div>
                      <button
                        type="button"
                        onClick={() =>
                          triggerModuleAction(
                            locale === "ru"
                              ? "Готов к установке ZIP-модуля (проверка SHA-256 активна)"
                              : "Ready to install module ZIP (SHA-256 verification active)"
                          )
                        }
                        className="inline-flex items-center gap-1 rounded-full bg-[#2A3447] px-3 py-1.5 text-[11px] font-semibold text-[#DCE7FF] cursor-pointer hover:bg-[#38465E]"
                      >
                        <Upload className="h-3 w-3" />
                        Install ZIP
                      </button>
                    </div>
                  </div>

                  {moduleToast && (
                    <div className="rounded-2xl bg-[#243248] border border-[#B8CCF0]/40 px-3.5 py-2 text-xs font-medium text-[#DCE7FF] flex items-center justify-between">
                      <span>{moduleToast}</span>
                      <ShieldCheck className="h-4 w-4 text-[#B8CCF0] shrink-0" />
                    </div>
                  )}

                  {SHEVERY_MODULES.map((mod) => {
                    const enabled = moduleStates[mod.id] ?? mod.enabledByDefault
                    return (
                      <div
                        key={mod.id}
                        className="rounded-[26px] overflow-hidden bg-[#DECFF8] text-[#231638] shadow-md transition-transform duration-300"
                      >
                        {/* Module Dark Header Banner */}
                        <div
                          className={`bg-gradient-to-b ${mod.bannerAccent} px-4 py-4 text-center border-b border-black/15`}
                        >
                          <div className="font-mono text-lg font-bold tracking-wider">
                            {mod.bannerTitle}
                          </div>
                        </div>

                        {/* Lavender Body */}
                        <div className="p-4 space-y-3">
                          <div className="flex items-start justify-between gap-2">
                            <div>
                              <div className="text-lg font-bold text-[#24163A]">
                                {mod.name}
                              </div>
                              <div className="text-xs text-[#594A73] font-medium">
                                Version: {mod.version} • Author: {mod.author}
                              </div>
                            </div>
                            <ExpressiveSwitch
                              checked={enabled}
                              onCheckedChange={(val) =>
                                setModuleStates((s) => ({ ...s, [mod.id]: val }))
                              }
                              label={`Toggle ${mod.name}`}
                              darkOnLight
                            />
                          </div>

                          <p className="text-xs leading-relaxed text-[#4A3B63]">
                            {mod.description[locale]}
                          </p>

                          {/* Metadata Chips */}
                          <div className="flex flex-wrap gap-1.5">
                            <span className="rounded-lg border border-[#6E5D8A]/50 px-2.5 py-1 text-[11px] font-semibold text-[#3C2D54]">
                              {mod.id.trim()}
                            </span>
                            <span className="rounded-lg border border-[#6E5D8A]/50 px-2.5 py-1 text-[11px] font-semibold text-[#3C2D54]">
                              {mod.size}
                            </span>
                            <span className="rounded-lg border border-[#6E5D8A]/50 px-2.5 py-1 text-[11px] font-semibold text-[#3C2D54]">
                              {mod.trust}
                            </span>
                          </div>

                          {/* Module Action Buttons */}
                          <div className="flex flex-wrap gap-2 pt-1">
                            <button
                              type="button"
                              onClick={() =>
                                triggerModuleAction(
                                  `Executed ${mod.id.trim()}/action.sh (Exit 0)`
                                )
                              }
                              className="inline-flex items-center gap-1.5 rounded-full bg-[#B4CBEF] px-3.5 py-1.5 text-xs font-bold text-[#17263D] cursor-pointer hover:brightness-95"
                            >
                              <Play className="h-3 w-3 fill-current" />
                              Action
                            </button>
                            {mod.hasWebUi && (
                              <button
                                type="button"
                                onClick={() =>
                                  triggerModuleAction(
                                    `Opened WebUI (window.shizuku bridge active)`
                                  )
                                }
                                className="inline-flex items-center gap-1.5 rounded-full border border-[#6E5D8A]/60 px-3.5 py-1.5 text-xs font-semibold text-[#35264D] cursor-pointer hover:bg-[#D1BFF2]"
                              >
                                <ExternalLink className="h-3 w-3" />
                                WebUI
                              </button>
                            )}
                            <button
                              type="button"
                              onClick={() =>
                                triggerModuleAction(
                                  `Service ${mod.id.trim()}/service.sh restarted`
                                )
                              }
                              className="inline-flex items-center gap-1.5 rounded-full border border-[#6E5D8A]/40 px-3.5 py-1.5 text-xs font-semibold text-[#4D3E66] cursor-pointer hover:bg-[#D1BFF2]"
                            >
                              <Terminal className="h-3 w-3" />
                              Service
                            </button>
                            <span className="inline-flex items-center gap-1 rounded-full border border-[#6E5D8A]/40 px-3 py-1.5 text-xs font-semibold text-[#5E4E77]">
                              <Trash2 className="h-3 w-3" />
                              Delete
                            </span>
                          </div>
                        </div>
                      </div>
                    )
                  })}
                </div>
              )}

              {/* TAB 3: COMPUT CONSOLE & AI AGENT (matching comput.png) */}
              {activeTab === "comput" && (
                <div className="space-y-3 animate-in fade-in duration-200">
                  <h3 className="text-[22px] font-semibold text-white pt-1">
                    Comput
                  </h3>

                  {/* Circular Expressive Action Buttons */}
                  <div className="flex items-center gap-2 overflow-x-auto pb-1">
                    {[
                      { icon: <Search className="h-4 w-4" />, label: "Search" },
                      {
                        icon: <History className="h-4 w-4" />,
                        label: "History",
                      },
                      {
                        icon: <SlidersHorizontal className="h-4 w-4" />,
                        label: "Env",
                      },
                      {
                        icon: <Sparkles className="h-4 w-4" />,
                        label: "AI Agent",
                        onClick: runComputAiAgent,
                        highlight: true,
                      },
                      {
                        icon: <LayoutGrid className="h-4 w-4" />,
                        label: "Templates",
                      },
                    ].map((btn, i) => (
                      <button
                        key={i}
                        type="button"
                        onClick={btn.onClick}
                        title={btn.label}
                        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full m3-spring cursor-pointer ${
                          btn.highlight
                            ? "bg-[#38465E] text-[#B8CCF0] ring-1 ring-[#B8CCF0]/50"
                            : "bg-[#283347] text-[#DCE7FF] hover:bg-[#38465E]"
                        }`}
                      >
                        {btn.icon}
                      </button>
                    ))}
                  </div>

                  {/* Mode Selector Pill */}
                  <div className="flex items-center gap-2">
                    {(["sh", "adb", "root"] as const).map((m) => (
                      <button
                        key={m}
                        type="button"
                        onClick={() => setShellMode(m)}
                        className={`inline-flex items-center gap-1.5 rounded-full px-3.5 py-1.5 text-xs font-bold m3-spring cursor-pointer ${
                          shellMode === m
                            ? "bg-[#38465E] text-[#DCE7FF] ring-1 ring-[#B8CCF0]/40"
                            : "bg-[#181E29] text-[#9BA7BE] hover:text-white"
                        }`}
                      >
                        <Terminal className="h-3.5 w-3.5" />
                        {m} Mode
                      </button>
                    ))}
                  </div>

                  {/* Command Input Box */}
                  <div className="flex items-center gap-2">
                    <div className="relative flex-1 rounded-2xl border border-[#5C6B85] bg-[#0E1117] px-3.5 py-2.5">
                      <span className="absolute -top-2 left-3 bg-[#0E1117] px-1 text-[10px] font-medium text-[#9BA7BE]">
                        Command
                      </span>
                      <div className="flex items-center gap-2 font-mono text-xs text-white">
                        <span className="text-[#B8CCF0] font-bold">$</span>
                        <input
                          type="text"
                          value={commandInput}
                          onChange={(e) => setCommandInput(e.target.value)}
                          onKeyDown={(e) => {
                            if (
                              e.key === "Enter" &&
                              !e.nativeEvent.isComposing &&
                              e.keyCode !== 229
                            ) {
                              runQuickCommand(commandInput)
                            }
                          }}
                          className="w-full bg-transparent text-white focus:outline-none"
                          aria-label="Shell command"
                        />
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => runQuickCommand(commandInput)}
                      className="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-[#38465E] text-[#DCE7FF] hover:bg-[#495B7A] m3-spring cursor-pointer"
                      aria-label="Run command"
                    >
                      <Send className="h-4 w-4" />
                    </button>
                  </div>

                  {/* Quick Preset Chips */}
                  <div className="flex gap-1.5 overflow-x-auto pb-1">
                    {[
                      "pm list packages -3",
                      "dumpsys battery",
                      "getprop ro.build.version.security_patch",
                    ].map((preset) => (
                      <button
                        key={preset}
                        type="button"
                        onClick={() => runQuickCommand(preset)}
                        className="shrink-0 rounded-full bg-[#283347] px-3 py-1 text-[11px] font-mono text-[#DCE7FF] hover:bg-[#38465E] cursor-pointer"
                      >
                        {preset}
                      </button>
                    ))}
                  </div>

                  {/* Console Output Card */}
                  <div className="rounded-[22px] bg-[#141820] border border-[#222A38] p-3.5 space-y-2.5">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-white">
                        Output
                      </span>
                      <div className="flex items-center gap-2">
                        <button
                          type="button"
                          onClick={runComputAiAgent}
                          className="inline-flex items-center gap-1.5 rounded-full border border-[#465570] bg-[#1B2230] px-3 py-1 text-[11px] font-semibold text-[#DCE7FF] hover:bg-[#2C374D] cursor-pointer"
                        >
                          <Sparkles className="h-3 w-3 text-[#B8CCF0]" />
                          {aiThinking ? "Agent thinking..." : "Ask AI"}
                        </button>
                        <button
                          type="button"
                          onClick={() =>
                            navigator.clipboard?.writeText(
                              consoleOutput.join("\n")
                            )
                          }
                          className="p-1 text-[#9BA7BE] hover:text-white cursor-pointer"
                          aria-label="Copy output"
                        >
                          <Copy className="h-3.5 w-3.5" />
                        </button>
                        <button
                          type="button"
                          onClick={() => setConsoleOutput(["Ready."])}
                          className="p-1 text-[#9BA7BE] hover:text-white cursor-pointer"
                          aria-label="Clear output"
                        >
                          <X className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    </div>

                    <div className="min-h-[175px] rounded-xl bg-black p-3 font-mono text-[11px] leading-relaxed text-[#E5EAF5] whitespace-pre-wrap">
                      {consoleOutput.join("\n")}
                    </div>

                    <div className="text-[10px] font-mono text-[#9BA7BE]">
                      Lines: {consoleOutput.length} | Mode:{" "}
                      {shellMode.toUpperCase()} | Providers: 10 ready
                    </div>
                  </div>
                </div>
              )}

              {/* TAB 4: SETTINGS SCREEN (matching settings.png) */}
              {activeTab === "settings" && (
                <div className="space-y-3 animate-in fade-in duration-200">
                  <h3 className="text-[22px] font-semibold text-white pt-1">
                    Settings
                  </h3>
                  <div className="text-xs font-bold text-[#B8CCF0] px-1">
                    Application
                  </div>

                  <div className="rounded-[26px] bg-[#151922] border border-[#222A38] divide-y divide-[#222A38] overflow-hidden">
                    {/* Startup Section */}
                    <div className="p-4 space-y-3">
                      <div className="text-xs font-bold text-[#B8CCF0]">
                        Startup
                      </div>
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <Wifi className="h-4 w-4 text-[#9BA7BE] mt-1 shrink-0" />
                          <div>
                            <div className="text-sm font-bold text-white">
                              Start on boot via Wireless debugging
                            </div>
                            <p className="text-[11px] text-[#9BA7BE] mt-1 leading-relaxed">
                              On boot, briefly enable Wireless debugging and
                              start Shevery over ADB (TCP port 5555). No root
                              required.
                            </p>
                          </div>
                        </div>
                        <ExpressiveSwitch
                          checked={bootWireless}
                          onCheckedChange={setBootWireless}
                          label="Start on boot via Wireless debugging"
                        />
                      </div>
                    </div>

                    {/* Service Maintenance */}
                    <div className="p-4 space-y-3">
                      <div className="text-xs font-bold text-[#B8CCF0]">
                        Service Maintenance
                      </div>
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <RefreshCw className="h-4 w-4 text-[#9BA7BE] mt-1 shrink-0" />
                          <div>
                            <div className="text-sm font-bold text-white">
                              Watchdog
                            </div>
                            <p className="text-[11px] text-[#9BA7BE] mt-1 leading-relaxed">
                              Check process health and transactional errors
                              every 10 seconds. Automatically restarts Shevery
                              if issues are found.
                            </p>
                          </div>
                        </div>
                        <ExpressiveSwitch
                          checked={watchdogEnabled}
                          onCheckedChange={setWatchdogEnabled}
                          label="Watchdog"
                        />
                      </div>
                    </div>

                    {/* Re-assert wireless debugging */}
                    <div className="p-4">
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <Code2 className="h-4 w-4 text-[#9BA7BE] mt-1 shrink-0" />
                          <div>
                            <div className="text-sm font-bold text-white">
                              Re-assert wireless debugging
                            </div>
                            <p className="text-[11px] text-[#9BA7BE] mt-1 leading-relaxed">
                              Restores the wireless debugging flag if the ROM
                              cleared it (at ADB start and Wi-Fi reconnect).
                            </p>
                          </div>
                        </div>
                        <ExpressiveSwitch
                          checked={reassertWireless}
                          onCheckedChange={setReassertWireless}
                          label="Re-assert wireless debugging"
                        />
                      </div>
                    </div>

                    {/* Compatibility Stubs */}
                    <div className="p-4">
                      <div className="flex items-center justify-between gap-3">
                        <div className="flex items-start gap-3">
                          <ShieldCheck className="h-4 w-4 text-[#9BA7BE] mt-1 shrink-0" />
                          <div>
                            <div className="text-sm font-bold text-white">
                              Legacy Shizuku & Dhizuku Stubs
                            </div>
                            <p className="text-[11px] text-[#9BA7BE] mt-1 leading-relaxed">
                              Satisfy apps that hard-check for original
                              moe.shizuku.privileged.api or com.rosan.dhizuku
                              packages.
                            </p>
                          </div>
                        </div>
                        <ExpressiveSwitch
                          checked={compatStubs}
                          onCheckedChange={setCompatStubs}
                          label="Legacy Compatibility Stubs"
                        />
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Expressive Floating Bottom Navigation Bar */}
            <div className="absolute inset-x-0 bottom-4 flex justify-center z-20 px-2">
              <ExpressiveFloatingToolbar
                tabs={navTabs}
                activeTab={activeTab}
                onSelect={(id) => setActiveTab(id as ScreenTab)}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
