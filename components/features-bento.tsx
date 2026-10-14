"use client"

import React, { useState } from "react"
import {
  Bot,
  PackageCheck,
  ShieldAlert,
  EyeOff,
  KeyRound,
  Flame,
  Layers,
  Activity,
  Sparkles,
  Check,
  Download,
  Shield,
  Cpu,
  Terminal,
  Lock,
  ExternalLink,
} from "lucide-react"
import { SHEVERY_FEATURES, FeatureItem } from "@/lib/shevery-data"

const ICON_MAP: Record<string, React.ComponentType<{ className?: string }>> = {
  Bot,
  PackageCheck,
  ShieldAlert,
  EyeOff,
  KeyRound,
  Flame,
  Layers,
  Activity,
}

export function FeaturesBento() {
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [selectedEdition, setSelectedEdition] = useState<"full" | "lite" | "stealth">("full")

  const categories = [
    { id: "all", label: "All Capabilities" },
    { id: "ai", label: "Comput AI" },
    { id: "stealth", label: "Stealth & Spoofing" },
    { id: "enterprise", label: "Device Owner & DPM" },
    { id: "core", label: "ADB Modules" },
    { id: "lab", label: "Firewall & Lab" },
  ]

  const filtered =
    selectedCategory === "all"
      ? SHEVERY_FEATURES
      : SHEVERY_FEATURES.filter((f) => f.category === selectedCategory)

  const accentStyles: Record<FeatureItem["accent"], { pill: string; iconBg: string; border: string }> = {
    primary: {
      pill: "bg-[#354496] text-[#DEE1FF]",
      iconBg: "bg-[#354496]/40 text-[#BAC3FF] border-[#BAC3FF]/30",
      border: "hover:border-[#BAC3FF]/50",
    },
    secondary: {
      pill: "bg-[#71295E] text-[#FFD7EE]",
      iconBg: "bg-[#71295E]/40 text-[#FFADE1] border-[#FFADE1]/30",
      border: "hover:border-[#FFADE1]/50",
    },
    tertiary: {
      pill: "bg-[#00513B] text-[#85F8C9]",
      iconBg: "bg-[#00513B]/40 text-[#66DFB2] border-[#66DFB2]/30",
      border: "hover:border-[#66DFB2]/50",
    },
    error: {
      pill: "bg-[#93000A] text-[#FFDAD6]",
      iconBg: "bg-[#93000A]/40 text-[#FFB4AB] border-[#FFB4AB]/30",
      border: "hover:border-[#FFB4AB]/50",
    },
  }

  return (
    <div className="space-y-20">
      {/* Category Filter Pills */}
      <div className="space-y-8">
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 rounded-full bg-[#354496]/50 border border-[#BAC3FF]/30 px-3.5 py-1 text-xs font-semibold text-[#DEE1FF]">
              <Sparkles className="h-3.5 w-3.5 text-[#BAC3FF]" />
              Material 3 Expressive Architecture
            </div>
            <h2 className="mt-3 font-display text-3xl sm:text-4xl font-extrabold tracking-tight text-[#E3E1E9]">
              Far Beyond Standard Shizuku
            </h2>
            <p className="mt-2 max-w-2xl text-sm sm:text-base text-[#C5C5D6]">
              Shevery unifies privileged binder IPC, an autonomous 44-tool AI agent, rootless Magisk-style ADB modules, Device Owner orchestration, and anti-detection stealth morphing in one cohesive app.
            </p>
          </div>

          <div className="flex flex-wrap gap-1.5">
            {categories.map((cat) => (
              <button
                key={cat.id}
                type="button"
                onClick={() => setSelectedCategory(cat.id)}
                className={`rounded-full px-4 py-2 text-xs font-semibold transition-all duration-300 ${
                  selectedCategory === cat.id
                    ? "bg-[#BAC3FF] text-[#17237A] shadow-sm scale-[1.02]"
                    : "bg-[#1F2026] text-[#C5C5D6] border border-[#444654]/60 hover:bg-[#282932]"
                }`}
              >
                {cat.label}
              </button>
            ))}
          </div>
        </div>

        {/* Expressive Bento Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((item) => {
            const Icon = ICON_MAP[item.iconName] || Sparkles
            const style = accentStyles[item.accent]
            return (
              <div
                key={item.id}
                className={`m3-spring group relative flex flex-col justify-between rounded-[32px] border border-[#444654]/60 bg-[#1B1C22]/90 p-6 transition-all ${
                  style.border
                } ${item.span === "wide" ? "md:col-span-2" : ""}`}
              >
                <div>
                  <div className="flex items-center justify-between gap-3">
                    <div
                      className={`flex h-12 w-12 items-center justify-center rounded-2xl border ${style.iconBg}`}
                    >
                      <Icon className="h-6 w-6" />
                    </div>
                    <span
                      className={`rounded-full px-3 py-1 text-[11px] font-bold tracking-wider uppercase ${style.pill}`}
                    >
                      {item.badge}
                    </span>
                  </div>

                  <h3 className="mt-5 font-display text-xl font-bold text-[#E3E1E9] group-hover:text-white">
                    {item.title}
                  </h3>
                  <p className="mt-2 text-sm leading-relaxed text-[#C5C5D6]">
                    {item.description}
                  </p>
                </div>

                <div className="mt-5 rounded-2xl bg-[#121318]/90 border border-[#444654]/40 p-3.5">
                  <div className="text-[11px] font-mono leading-relaxed text-[#BAC3FF]">
                    {item.technicalDetail}
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>

      {/* Build Flavors & Stealth Comparison Section */}
      <div id="editions" className="scroll-mt-24 rounded-[36px] border border-[#444654]/70 bg-gradient-to-b from-[#1F2026] to-[#17181E] p-6 sm:p-10">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 pb-8 border-b border-[#444654]/50">
          <div>
            <span className="inline-flex items-center gap-1.5 rounded-full bg-[#71295E] px-3 py-1 text-xs font-bold text-[#FFD7EE]">
              <Shield className="h-3.5 w-3.5" />
              3 Official Build Flavors
            </span>
            <h3 className="mt-3 font-display text-2xl sm:text-3xl font-extrabold text-[#E3E1E9]">
              Choose Your Shevery Edition
            </h3>
            <p className="mt-1.5 max-w-xl text-sm text-[#C5C5D6]">
              Every GitHub Release ships with Full and Lite APKs, while the built-in Stealth Generator morphs your installation on-device to evade package scanners.
            </p>
          </div>

          <div className="inline-flex rounded-full bg-[#121318] p-1.5 border border-[#444654]/70 self-start">
            {(
              [
                { id: "full", label: "Shevery Full" },
                { id: "lite", label: "Shevery Lite" },
                { id: "stealth", label: "Stealth Morphed" },
              ] as const
            ).map((ed) => (
              <button
                key={ed.id}
                type="button"
                onClick={() => setSelectedEdition(ed.id)}
                className={`rounded-full px-4 py-2 text-xs font-bold transition-all ${
                  selectedEdition === ed.id
                    ? "bg-[#BAC3FF] text-[#17237A]"
                    : "text-[#C5C5D6] hover:text-white"
                }`}
              >
                {ed.label}
              </button>
            ))}
          </div>
        </div>

        <div className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Card 1: Full */}
          <div
            onClick={() => setSelectedEdition("full")}
            className={`cursor-pointer rounded-[28px] p-6 transition-all border ${
              selectedEdition === "full"
                ? "border-[#BAC3FF] bg-[#282932]/90 shadow-lg"
                : "border-[#444654]/50 bg-[#1B1C22]/60 hover:border-[#444654]"
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="rounded-full bg-[#354496] px-3 py-1 text-xs font-bold text-[#DEE1FF]">
                Recommended
              </span>
              <span className="font-mono text-xs text-[#66DFB2]">BuildConfig.IS_FULL_VERSION = true</span>
            </div>
            <h4 className="mt-4 font-display text-xl font-bold text-white">Shevery Full</h4>
            <p className="mt-1 text-xs text-[#C5C5D6]">
              Complete privileged workstation with Comput AI Agent, ADB Modules, Device Owner, and Lab tools.
            </p>
            <ul className="mt-5 space-y-2.5 text-xs text-[#E3E1E9]">
              {[
                "Comput AI Agent (44 Tools + Vision + Shell)",
                "Rootless ADB Modules + GitHub Store + WebUI",
                "Device Owner, DPM Scopes & Dhizuku Bridge",
                "USB Debugging Hide Guard & UID Firewall",
                "On-device Stealth APK & Stub Generator",
              ].map((feat) => (
                <li key={feat} className="flex items-center gap-2">
                  <Check className="h-4 w-4 shrink-0 text-[#66DFB2]" />
                  <span>{feat}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Card 2: Lite */}
          <div
            onClick={() => setSelectedEdition("lite")}
            className={`cursor-pointer rounded-[28px] p-6 transition-all border ${
              selectedEdition === "lite"
                ? "border-[#66DFB2] bg-[#282932]/90 shadow-lg"
                : "border-[#444654]/50 bg-[#1B1C22]/60 hover:border-[#444654]"
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="rounded-full bg-[#00513B] px-3 py-1 text-xs font-bold text-[#85F8C9]">
                Minimalist Core
              </span>
              <span className="font-mono text-xs text-[#85F8C9]">liteRelease</span>
            </div>
            <h4 className="mt-4 font-display text-xl font-bold text-white">Shevery Lite</h4>
            <p className="mt-1 text-xs text-[#C5C5D6]">
              Streamlined Shizuku privilege manager with Material 3 Expressive UI, Watchdog, and zero AI/Module overhead.
            </p>
            <ul className="mt-5 space-y-2.5 text-xs text-[#E3E1E9]">
              {[
                "Core Shizuku Server (Wireless ADB & Root)",
                "Material 3 Expressive Dynamic Color UI",
                "Server Watchdog & Auto-Start on Boot",
                "Accessibility Service Manager & App Auth",
                "Tasker / MacroDroid Automation Receiver",
              ].map((feat) => (
                <li key={feat} className="flex items-center gap-2">
                  <Check className="h-4 w-4 shrink-0 text-[#66DFB2]" />
                  <span>{feat}</span>
                </li>
              ))}
            </ul>
          </div>

          {/* Card 3: Stealth */}
          <div
            onClick={() => setSelectedEdition("stealth")}
            className={`cursor-pointer rounded-[28px] p-6 transition-all border ${
              selectedEdition === "stealth"
                ? "border-[#FFADE1] bg-[#282932]/90 shadow-lg"
                : "border-[#444654]/50 bg-[#1B1C22]/60 hover:border-[#444654]"
            }`}
          >
            <div className="flex items-center justify-between">
              <span className="rounded-full bg-[#71295E] px-3 py-1 text-xs font-bold text-[#FFD7EE]">
                Anti-Detection
              </span>
              <span className="font-mono text-xs text-[#FFADE1]">StealthApkGenerator</span>
            </div>
            <h4 className="mt-4 font-display text-xl font-bold text-white">Stealth Morphed APK</h4>
            <p className="mt-1 text-xs text-[#C5C5D6]">
              Generated directly on your phone with a random package ID and custom name to bypass root/Shizuku detectors.
            </p>
            <ul className="mt-5 space-y-2.5 text-xs text-[#E3E1E9]">
              {[
                "Binary AndroidManifest.xml UTF-8/16 patching",
                "Fresh on-device V1/V2/V3 signing certificate",
                "Compatibility Stubs for Shizuku, Sui & Dhizuku",
                "Automatic settings backup & migration",
                "Optional old package self-uninstall",
              ].map((feat) => (
                <li key={feat} className="flex items-center gap-2">
                  <Check className="h-4 w-4 shrink-0 text-[#FFADE1]" />
                  <span>{feat}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </div>
    </div>
  )
}
