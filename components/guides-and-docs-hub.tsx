"use client"

import React, { useState } from "react"
import {
  Check,
  Copy,
  Terminal,
  Sparkles,
  Boxes,
  Shield,
  EyeOff,
  Workflow,
  ChevronRight,
} from "lucide-react"
import { Locale, SHEVERY_GUIDES } from "@/lib/shevery-data"

interface GuidesAndDocsHubProps {
  locale: Locale
}

export function GuidesAndDocsHub({ locale }: GuidesAndDocsHubProps) {
  const [selectedGuideId, setSelectedGuideId] = useState(SHEVERY_GUIDES[0].id)
  const [copiedKey, setCopiedKey] = useState<string | null>(null)

  const currentGuide =
    SHEVERY_GUIDES.find((g) => g.id === selectedGuideId) ?? SHEVERY_GUIDES[0]

  const copyCode = (text: string, key: string) => {
    navigator.clipboard?.writeText(text)
    setCopiedKey(key)
    setTimeout(() => setCopiedKey(null), 2000)
  }

  const guideIcons: Record<string, React.ReactNode> = {
    quickstart: <Terminal className="h-4 w-4" />,
    "comput-ai": <Sparkles className="h-4 w-4" />,
    "adb-modules": <Boxes className="h-4 w-4" />,
    "device-owner": <Shield className="h-4 w-4" />,
    "stealth-stubs": <EyeOff className="h-4 w-4" />,
    automation: <Workflow className="h-4 w-4" />,
  }

  return (
    <section id="guides" className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-10">
        <div>
          <span className="inline-flex items-center gap-2 rounded-full bg-[#293447] px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-[#B8CCF0]">
            {locale === "ru"
              ? "База знаний и документация"
              : "Knowledge Base & Guides"}
          </span>
          <h2 className="mt-4 text-3xl sm:text-4xl font-extrabold tracking-tight text-white">
            {locale === "ru"
              ? "Полное руководство по экосистеме Shevery"
              : "Complete Shevery Ecosystem Guides"}
          </h2>
        </div>
        <p className="max-w-md text-sm text-[#9BA7BE] leading-relaxed">
          {locale === "ru"
            ? "Актуальная техническая документация по всем подсистемам репозитория: от автозапуска по Wireless ADB до разработки WebUI-модулей и интеграции с Tasker."
            : "Up-to-date technical guides covering every subsystem in the repository: from Wireless ADB boot autostart to WebUI modules and Tasker automation."}
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left Navigation Pills */}
        <div className="lg:col-span-4 flex flex-col gap-2.5 rounded-[32px] bg-[#141923] p-3.5 border border-[#242D3E]">
          {SHEVERY_GUIDES.map((guide) => {
            const active = guide.id === selectedGuideId
            return (
              <button
                key={guide.id}
                type="button"
                onClick={() => setSelectedGuideId(guide.id)}
                className={`group flex items-center justify-between gap-3 rounded-[22px] p-4 text-left m3-spring cursor-pointer ${
                  active
                    ? "bg-[#B8CCF0] text-[#132238] shadow-md scale-[1.01]"
                    : "bg-[#1A212E]/70 text-[#DCE7FF] hover:bg-[#232C3D]"
                }`}
              >
                <div className="flex items-center gap-3.5 min-w-0">
                  <div
                    className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-2xl m3-spring ${
                      active
                        ? "bg-[#132238] text-[#B8CCF0]"
                        : "bg-[#293447] text-[#B8CCF0]"
                    }`}
                  >
                    {guideIcons[guide.id]}
                  </div>
                  <div className="min-w-0">
                    <div
                      className={`text-[11px] font-bold uppercase tracking-wider ${
                        active ? "text-[#132238]/75" : "text-[#9BA7BE]"
                      }`}
                    >
                      {guide.badge}
                    </div>
                    <div className="text-sm font-bold truncate mt-0.5">
                      {guide.title[locale]}
                    </div>
                  </div>
                </div>
                <ChevronRight
                  className={`h-4 w-4 shrink-0 transition-transform ${
                    active
                      ? "translate-x-0.5 text-[#132238]"
                      : "text-[#9BA7BE] group-hover:translate-x-0.5"
                  }`}
                />
              </button>
            )
          })}
        </div>

        {/* Right Guide Content Panel */}
        <div className="lg:col-span-8 rounded-[36px] bg-[#151A24] border border-[#263042] p-6 sm:p-8 space-y-6 shadow-xl">
          <div className="space-y-2">
            <span className="inline-flex items-center rounded-full bg-[#38465E] px-3.5 py-1 text-xs font-bold text-[#DCE7FF]">
              {currentGuide.badge}
            </span>
            <h3 className="text-2xl sm:text-3xl font-extrabold text-white">
              {currentGuide.title[locale]}
            </h3>
            <p className="text-sm sm:text-base text-[#9BA7BE] leading-relaxed">
              {currentGuide.subtitle[locale]}
            </p>
          </div>

          {/* Highlights List */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
            {currentGuide.highlights[locale].map((point, idx) => (
              <div
                key={idx}
                className="flex items-start gap-3 rounded-[22px] bg-[#1B2230] p-4 border border-[#2A3447]"
              >
                <div className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[#B8CCF0] text-[#132238] text-xs font-extrabold mt-0.5">
                  {idx + 1}
                </div>
                <p className="text-xs sm:text-sm text-[#DCE7FF]/90 leading-relaxed">
                  {point}
                </p>
              </div>
            ))}
          </div>

          {/* Primary Code Block */}
          <div className="rounded-[24px] bg-[#0B0E14] border border-[#252F42] overflow-hidden">
            <div className="flex items-center justify-between px-4 py-3 bg-[#121722] border-b border-[#222B3C]">
              <span className="font-mono text-xs font-semibold text-[#B8CCF0]">
                {currentGuide.codeTitle}
              </span>
              <button
                type="button"
                onClick={() => copyCode(currentGuide.code, "primary")}
                className="inline-flex items-center gap-1.5 rounded-full bg-[#263042] px-3 py-1 text-xs font-semibold text-[#DCE7FF] hover:bg-[#38465E] cursor-pointer"
              >
                {copiedKey === "primary" ? (
                  <>
                    <Check className="h-3.5 w-3.5 text-emerald-400" />
                    {locale === "ru" ? "Скопировано" : "Copied"}
                  </>
                ) : (
                  <>
                    <Copy className="h-3.5 w-3.5" />
                    {locale === "ru" ? "Копировать" : "Copy"}
                  </>
                )}
              </button>
            </div>
            <pre className="p-4 font-mono text-xs sm:text-sm text-[#E5EAF5] overflow-x-auto leading-relaxed">
              <code>{currentGuide.code}</code>
            </pre>
          </div>

          {/* Optional Secondary Code Block */}
          {currentGuide.secondaryCode && (
            <div className="rounded-[24px] bg-[#0B0E14] border border-[#252F42] overflow-hidden">
              <div className="flex items-center justify-between px-4 py-3 bg-[#121722] border-b border-[#222B3C]">
                <span className="font-mono text-xs font-semibold text-[#DECFF8]">
                  {currentGuide.secondaryCodeTitle}
                </span>
                <button
                  type="button"
                  onClick={() =>
                    copyCode(currentGuide.secondaryCode!, "secondary")
                  }
                  className="inline-flex items-center gap-1.5 rounded-full bg-[#263042] px-3 py-1 text-xs font-semibold text-[#DCE7FF] hover:bg-[#38465E] cursor-pointer"
                >
                  {copiedKey === "secondary" ? (
                    <>
                      <Check className="h-3.5 w-3.5 text-emerald-400" />
                      {locale === "ru" ? "Скопировано" : "Copied"}
                    </>
                  ) : (
                    <>
                      <Copy className="h-3.5 w-3.5" />
                      {locale === "ru" ? "Копировать" : "Copy"}
                    </>
                  )}
                </button>
              </div>
              <pre className="p-4 font-mono text-xs sm:text-sm text-[#E5EAF5] overflow-x-auto leading-relaxed">
                <code>{currentGuide.secondaryCode}</code>
              </pre>
            </div>
          )}
        </div>
      </div>
    </section>
  )
}
