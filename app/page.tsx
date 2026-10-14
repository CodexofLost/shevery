"use client"

import React, { useState } from "react"
import {
  Download,
  BookOpen,
  Sparkles,
  ShieldCheck,
  Terminal,
  Boxes,
  Globe,
  ExternalLink,
  Check,
  Copy,
  ArrowUpRight,
} from "lucide-react"
import { InteractivePhoneShowcase } from "@/components/interactive-phone-showcase"
import { FeaturesBento } from "@/components/features-bento"
import { GuidesAndDocsHub } from "@/components/guides-and-docs-hub"
import { Locale } from "@/lib/shevery-data"

export default function SheveryHomePage() {
  const [locale, setLocale] = useState<Locale>("ru")
  const [copiedStartCmd, setCopiedStartCmd] = useState(false)

  const adbCommand =
    "adb shell sh /sdcard/Android/data/com.hamondev.shevery/files/start.sh"

  const handleCopyAdb = () => {
    navigator.clipboard?.writeText(adbCommand)
    setCopiedStartCmd(true)
    setTimeout(() => setCopiedStartCmd(false), 2000)
  }

  return (
    <div className="min-h-screen bg-[#0E1117] text-[#E5EAF5] flex flex-col justify-between relative overflow-hidden">
      {/* Subtle Ambient M3 Expressive Tonal Glows */}
      <div
        aria-hidden="true"
        className="pointer-events-none fixed top-[-180px] left-1/2 -translate-x-1/2 h-[520px] w-[960px] rounded-full bg-[#38465E]/25 blur-[130px] -z-10"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none fixed top-[420px] right-[-180px] h-[420px] w-[420px] rounded-full bg-[#DECFF8]/10 blur-[140px] -z-10"
      />

      {/* TOP EXPRESSIVE FLOATING HEADER */}
      <header className="sticky top-4 z-40 px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl flex items-center justify-between gap-3 rounded-full bg-[#161C27]/85 backdrop-blur-xl border border-[#2B364B] px-4 py-2.5 shadow-[0_12px_40px_rgba(0,0,0,0.45)]">
          {/* Brand Logo */}
          <a href="#" className="flex items-center gap-3 group">
            <img
              src="/icon.webp"
              alt="Shevery Logo"
              className="h-9 w-9 rounded-full ring-2 ring-[#B8CCF0]/40 m3-spring group-hover:scale-105"
            />
            <div>
              <div className="flex items-center gap-2">
                <span className="text-base font-extrabold tracking-tight text-white">
                  Shevery
                </span>
                <span className="rounded-full bg-[#38465E] px-2.5 py-0.5 text-[11px] font-bold text-[#B8CCF0]">
                  v14.1
                </span>
              </div>
              <span className="hidden sm:block text-[11px] text-[#9BA7BE]">
                Material 3 Expressive • Shizuku + Dhizuku
              </span>
            </div>
          </a>

          {/* Navigation Links */}
          <nav
            aria-label="Primary Navigation"
            className="hidden md:flex items-center gap-1 rounded-full bg-[#1E2636] p-1 border border-[#2D394F]"
          >
            {[
              {
                href: "#showcase",
                label: locale === "ru" ? "Обзор" : "Showcase",
              },
              {
                href: "#features",
                label: locale === "ru" ? "Возможности" : "Features",
              },
              {
                href: "#guides",
                label: locale === "ru" ? "Документация" : "Guides",
              },
              {
                href: "#downloads",
                label: locale === "ru" ? "Скачать" : "Downloads",
              },
              {
                href: "#contributors",
                label: locale === "ru" ? "Команда" : "Team",
              },
            ].map((item) => (
              <a
                key={item.href}
                href={item.href}
                className="rounded-full px-4 py-1.5 text-xs font-bold text-[#DCE7FF]/85 hover:bg-[#38465E] hover:text-white m3-spring"
              >
                {item.label}
              </a>
            ))}
          </nav>

          {/* Right Controls: Language Toggle + GitHub */}
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setLocale(locale === "ru" ? "en" : "ru")}
              className="inline-flex items-center gap-1.5 rounded-full bg-[#252F42] px-3.5 py-2 text-xs font-bold text-[#DCE7FF] hover:bg-[#38465E] m3-spring cursor-pointer"
              aria-label="Switch language"
            >
              <Globe className="h-3.5 w-3.5 text-[#B8CCF0]" />
              {locale === "ru" ? "RU / EN" : "EN / RU"}
            </button>

            <a
              href="https://github.com/HmnDev-Tech/shevery"
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-1.5 rounded-full bg-[#B8CCF0] px-4 py-2 text-xs font-extrabold text-[#132238] m3-spring hover:scale-[1.03] active:scale-95"
            >
              GitHub
              <ArrowUpRight className="h-3.5 w-3.5" />
            </a>
          </div>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <main className="flex-1">
        {/* HERO SECTION */}
        <section
          id="showcase"
          className="pt-12 pb-16 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto"
        >
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
            {/* Left Hero Column */}
            <div className="lg:col-span-7 space-y-6">
              <div className="inline-flex flex-wrap items-center gap-2 rounded-full bg-[#1C2433] border border-[#313E57] px-4 py-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
                <span className="text-xs font-bold text-[#B8CCF0]">
                  {locale === "ru"
                    ? "Поддержка Android 8.0 – Android 17 (API 36)"
                    : "Android 8.0 – Android 17 (API 36) Ready"}
                </span>
                <span className="text-xs text-[#9BA7BE]">•</span>
                <span className="text-xs font-semibold text-[#DECFF8]">
                  Material 3 Expressive
                </span>
              </div>

              <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-white leading-[1.08]">
                {locale === "ru" ? (
                  <>
                    Новое поколение{" "}
                    <span className="text-[#B8CCF0]">Shizuku</span> и{" "}
                    <span className="text-[#DECFF8]">Dhizuku</span> для Android
                  </>
                ) : (
                  <>
                    The Material 3 Expressive Evolution of{" "}
                    <span className="text-[#B8CCF0]">Shizuku</span> &{" "}
                    <span className="text-[#DECFF8]">Dhizuku</span>
                  </>
                )}
              </h1>

              <p className="text-base sm:text-lg text-[#9BA7BE] leading-relaxed max-w-2xl">
                {locale === "ru"
                  ? "Shevery предоставляет обычным приложениям прямой доступ к системным API через ADB или Root, добавляя автономного ИИ-агента Comput, модульную систему ADB Modules с WebUI, автозапуск по Wi-Fi при загрузке, встроенный провайдер Device Owner и защиту от обнаружения."
                  : "Shevery bridges standard Android apps with privileged system APIs over ADB or Root—supercharged with the Comput AI Agent console, ADB Modules with WebUI dashboards, Wireless ADB boot autostart, native Device Owner management, and Stealth anti-detection."}
              </p>

              {/* Hero CTAs */}
              <div className="flex flex-wrap items-center gap-3 pt-1">
                <a
                  href="https://github.com/HmnDev-Tech/shevery/releases/latest"
                  target="_blank"
                  rel="noreferrer"
                  className="inline-flex items-center gap-2.5 rounded-full bg-[#B8CCF0] px-7 py-4 text-sm font-extrabold text-[#132238] shadow-[0_8px_30px_rgba(184,204,240,0.25)] m3-spring hover:scale-[1.03] active:scale-95"
                >
                  <Download className="h-4 w-4" />
                  {locale === "ru"
                    ? "Скачать Shevery APK"
                    : "Download Shevery APK"}
                </a>

                <a
                  href="#guides"
                  className="inline-flex items-center gap-2 rounded-full bg-[#263042] border border-[#3B4A66] px-6 py-4 text-sm font-bold text-[#DCE7FF] hover:bg-[#38465E] m3-spring"
                >
                  <BookOpen className="h-4 w-4 text-[#B8CCF0]" />
                  {locale === "ru"
                    ? "Открыть гайды и документацию"
                    : "Explore Guides & Docs"}
                </a>
              </div>

              {/* Copyable Quick ADB Start Bar */}
              <div className="rounded-[24px] bg-[#141923] border border-[#263145] p-3.5 max-w-2xl">
                <div className="flex items-center justify-between gap-2 mb-1.5 px-1">
                  <span className="text-[11px] font-bold uppercase tracking-wider text-[#9BA7BE]">
                    {locale === "ru"
                      ? "Быстрый старт через ADB (или используйте Wireless Debugging в 1 клик)"
                      : "Quick Start via ADB (or use built-in Wireless Debugging)"}
                  </span>
                  <button
                    type="button"
                    onClick={handleCopyAdb}
                    className="inline-flex items-center gap-1 text-xs font-bold text-[#B8CCF0] hover:text-white cursor-pointer"
                  >
                    {copiedStartCmd ? (
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
                <div className="rounded-2xl bg-[#0B0E14] px-3.5 py-2.5 font-mono text-xs text-[#DCE7FF] overflow-x-auto">
                  <span className="text-[#B8CCF0] select-none">$ </span>
                  {adbCommand}
                </div>
              </div>

              {/* Key Metric Pills */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-2">
                {[
                  {
                    icon: <Terminal className="h-4 w-4 text-[#B8CCF0]" />,
                    value: "sh / adb / su",
                    label:
                      locale === "ru"
                        ? "Режимы Comput"
                        : "Comput Shell Modes",
                  },
                  {
                    icon: <Sparkles className="h-4 w-4 text-[#DECFF8]" />,
                    value: "10 AI APIs",
                    label:
                      locale === "ru"
                        ? "Провайдеров агента"
                        : "Built-in AI Presets",
                  },
                  {
                    icon: <Boxes className="h-4 w-4 text-[#B8CCF0]" />,
                    value: "WebUI + ZIP",
                    label:
                      locale === "ru" ? "ADB-модули" : "ADB Module Runtime",
                  },
                  {
                    icon: <ShieldCheck className="h-4 w-4 text-emerald-400" />,
                    value: "API 26 – 36",
                    label:
                      locale === "ru"
                        ? "Android 8.0 – 17"
                        : "Android 8.0 – 17",
                  },
                ].map((stat, i) => (
                  <div
                    key={i}
                    className="rounded-[22px] bg-[#151A24] border border-[#242E40] p-3.5"
                  >
                    <div className="flex items-center gap-2">
                      {stat.icon}
                      <span className="text-sm font-extrabold text-white">
                        {stat.value}
                      </span>
                    </div>
                    <div className="text-[11px] text-[#9BA7BE] mt-1">
                      {stat.label}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right Hero Column: Interactive Material 3 Expressive Phone */}
            <div className="lg:col-span-5 flex justify-center">
              <InteractivePhoneShowcase locale={locale} />
            </div>
          </div>
        </section>

        {/* BENTO FEATURES + EDITIONS + CONTRIBUTORS */}
        <FeaturesBento locale={locale} />

        {/* INTERACTIVE DOCUMENTATION & GUIDES HUB */}
        <GuidesAndDocsHub locale={locale} />
      </main>

      {/* FOOTER */}
      <footer className="border-t border-[#222B3C] bg-[#0B0E14] py-12 px-4 sm:px-6 lg:px-8">
        <div className="mx-auto max-w-7xl flex flex-col sm:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-3.5">
            <img
              src="/icon.webp"
              alt="Shevery"
              className="h-9 w-9 rounded-full ring-1 ring-[#B8CCF0]/40"
            />
            <div>
              <div className="text-sm font-bold text-white">
                Shevery • HmnDev-Tech
              </div>
              <div className="text-xs text-[#9BA7BE]">
                {locale === "ru"
                  ? "Открытый исходный код под лицензией Apache-2.0. Построено на базе Shizuku и Dhizuku."
                  : "Open-source under the Apache-2.0 License. Built on the foundations of Shizuku & Dhizuku."}
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-4 text-xs font-bold text-[#B8CCF0]">
            <a
              href="https://github.com/HmnDev-Tech/shevery"
              target="_blank"
              rel="noreferrer"
              className="hover:text-white inline-flex items-center gap-1"
            >
              GitHub Repository
              <ExternalLink className="h-3.5 w-3.5" />
            </a>
            <a
              href="https://github.com/HmnDev-Tech/shevery/releases"
              target="_blank"
              rel="noreferrer"
              className="hover:text-white inline-flex items-center gap-1"
            >
              Releases
              <ExternalLink className="h-3.5 w-3.5" />
            </a>
            <a
              href="https://t.me/hmndevtech"
              target="_blank"
              rel="noreferrer"
              className="hover:text-white inline-flex items-center gap-1"
            >
              Telegram (@hmndevtech)
              <ExternalLink className="h-3.5 w-3.5" />
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
