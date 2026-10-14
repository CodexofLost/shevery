import React from "react"
import {
  Sparkles,
  Download,
  GitBranch,
  ShieldCheck,
  Bot,
  PackageCheck,
  ArrowUpRight,
  Cpu,
  KeyRound,
  EyeOff,
  BookOpen,
  Zap,
} from "lucide-react"
import { FeaturesBento } from "@/components/features-bento"
import { GuidesAndDocsHub } from "@/components/guides-and-docs-hub"
import { SHEVERY_STATS } from "@/lib/shevery-data"
import { ExpressiveMorphBadge, ExpressiveStatusBlob } from "@/components/m3-expressive-primitives"

export default function SheveryHomePage() {
  return (
    <div className="min-h-screen bg-[#121318] text-[#E3E1E9] selection:bg-[#BAC3FF] selection:text-[#17237A]">
      {/* Ambient Material 3 Expressive Background Mesh */}
      <div
        aria-hidden="true"
        className="pointer-events-none fixed inset-0 -z-10 overflow-hidden"
      >
        <div className="m3-blob absolute -top-36 left-1/4 h-[460px] w-[460px] bg-[#354496]/25 blur-[130px]" />
        <div className="m3-blob-delayed absolute top-1/3 -right-24 h-[420px] w-[420px] bg-[#71295E]/20 blur-[130px]" />
        <div className="m3-blob absolute -bottom-32 left-10 h-[380px] w-[380px] bg-[#00513B]/20 blur-[130px]" />
      </div>

      {/* Sticky Material 3 Expressive Navigation Bar (No Logo) */}
      <header className="sticky top-0 z-50 border-b border-[#444654]/40 bg-[#121318]/80 backdrop-blur-xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 sm:px-6 lg:px-8 h-16">
          <a href="#" className="flex items-center gap-2.5 group">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-display text-lg font-extrabold tracking-tight text-white">
                  Shevery
                </span>
                <span className="hidden sm:inline-flex rounded-full bg-[#354496] px-2.5 py-0.5 text-[10px] font-bold text-[#DEE1FF]">
                  M3 Expressive
                </span>
              </div>
              <p className="text-[10px] text-[#8F909F] hidden sm:block">
                Next-Gen Shizuku Privilege Manager
              </p>
            </div>
          </a>

          <nav className="hidden md:flex items-center gap-1 rounded-full border border-[#444654]/60 bg-[#1B1C22]/90 p-1">
            {[
              { href: "#features", label: "Capabilities" },
              { href: "#architecture", label: "Architecture" },
              { href: "#guides", label: "Guides & API" },
            ].map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="rounded-full px-3.5 py-1.5 text-xs font-semibold text-[#C5C5D6] hover:bg-[#282932] hover:text-white transition-colors"
              >
                {link.label}
              </a>
            ))}
          </nav>

          <div className="flex items-center gap-2.5">
            <a
              href="https://github.com/HmnDev-Tech/shevery"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-full border border-[#444654] bg-[#1F2026] px-3.5 py-2 text-xs font-semibold text-[#E3E1E9] hover:border-[#BAC3FF] transition-colors"
            >
              <GitBranch className="h-4 w-4" />
              <span className="hidden sm:inline">GitHub</span>
            </a>
            <a
              href="https://github.com/HmnDev-Tech/shevery/releases/latest"
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-2 rounded-full bg-[#BAC3FF] px-4 py-2 text-xs font-bold text-[#17237A] hover:bg-[#DEE1FF] transition-all shadow-sm"
            >
              <Download className="h-3.5 w-3.5" />
              <span>Download APK</span>
            </a>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 pt-10 sm:pt-16 pb-24 space-y-28">
        {/* EXPRESSIVE HERO SECTION */}
        <section className="relative overflow-hidden rounded-[40px] border border-[#444654]/60 bg-gradient-to-br from-[#1B1C22] via-[#16171E] to-[#121318] p-6 sm:p-12 lg:p-14 shadow-2xl">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
            {/* Left Column: Hero Pitch */}
            <div className="lg:col-span-7 space-y-6">
              <div className="flex flex-wrap items-center gap-2">
                <ExpressiveMorphBadge variant="primary">
                  <Sparkles className="h-3.5 w-3.5 text-[#BAC3FF]" />
                  Material 3 Expressive · Android 11–17
                </ExpressiveMorphBadge>
                <ExpressiveMorphBadge variant="tertiary">
                  <ShieldCheck className="h-3.5 w-3.5 text-[#66DFB2]" />
                  Root & Wireless ADB
                </ExpressiveMorphBadge>
              </div>

              <h1 className="font-display text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-white leading-[1.08]">
                System-Level Power.{" "}
                <span className="bg-gradient-to-r from-[#BAC3FF] via-[#FFADE1] to-[#66DFB2] bg-clip-text text-transparent">
                  Zero Root Required.
                </span>
              </h1>

              <p className="max-w-2xl text-base sm:text-lg leading-relaxed text-[#C5C5D6]">
                <strong className="text-white font-semibold">Shevery</strong> is a deeply engineered evolution of Shizuku built with Material 3 Expressive design. It pairs direct Android binder IPC with the{" "}
                <span className="text-[#FFADE1] font-medium">Comput 44-tool AI Agent</span>,{" "}
                <span className="text-[#66DFB2] font-medium">rootless Magisk-like ADB Modules</span>,{" "}
                <span className="text-[#BAC3FF] font-medium">Device Owner & Dhizuku bridging</span>, and{" "}
                <span className="text-white font-medium">anti-detection package disguise</span>.
              </p>

              {/* Primary CTAs */}
              <div className="flex flex-wrap items-center gap-3 pt-2">
                <a
                  href="https://github.com/HmnDev-Tech/shevery/releases/latest"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="m3-spring inline-flex items-center gap-2.5 rounded-full bg-[#BAC3FF] px-6 py-3.5 text-sm font-bold text-[#17237A] hover:bg-[#DEE1FF] shadow-lg shadow-[#354496]/30"
                >
                  <Download className="h-4 w-4" />
                  Download Latest Release
                </a>
                <a
                  href="#guides"
                  className="m3-spring inline-flex items-center gap-2 rounded-full border border-[#444654] bg-[#1F2026] px-6 py-3.5 text-sm font-bold text-[#E3E1E9] hover:border-[#BAC3FF] hover:bg-[#282932]"
                >
                  <BookOpen className="h-4 w-4 text-[#BAC3FF]" />
                  Explore Guides & Docs
                </a>
                <a
                  href="https://github.com/HmnDev-Tech/shevery"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="m3-spring inline-flex items-center gap-2 rounded-full border border-[#444654]/70 bg-[#121318] px-5 py-3.5 text-sm font-semibold text-[#C5C5D6] hover:text-white"
                >
                  <GitBranch className="h-4 w-4" />
                  Source Code
                  <ArrowUpRight className="h-4 w-4" />
                </a>
              </div>

              {/* Key Metrics Strip */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-4">
                {SHEVERY_STATS.map((stat) => (
                  <div
                    key={stat.label}
                    className="rounded-[24px] border border-[#444654]/50 bg-[#121318]/85 p-4"
                  >
                    <div className="font-display text-xl sm:text-2xl font-extrabold text-white">
                      {stat.value}
                    </div>
                    <div className="mt-0.5 text-xs text-[#8F909F]">{stat.label}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right Column: Material 3 Expressive System Overview Cards */}
            <div className="lg:col-span-5 space-y-3.5">
              {/* Expressive Active Binder Card */}
              <div className="m3-spring rounded-[28px] bg-gradient-to-br from-[#354496] via-[#2B377B] to-[#1E2759] p-5 text-[#DEE1FF] shadow-lg border border-[#BAC3FF]/30">
                <div className="flex items-center gap-3.5">
                  <ExpressiveStatusBlob active>
                    <Zap className="h-6 w-6 text-[#66DFB2]" />
                  </ExpressiveStatusBlob>
                  <div>
                    <div className="inline-flex items-center gap-1.5 rounded-full bg-black/25 px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider text-[#66DFB2]">
                      Direct Binder IPC · API v13
                    </div>
                    <h2 className="mt-1 font-display text-lg font-extrabold text-white">
                      UID 2000 (ADB) & UID 0 (Root)
                    </h2>
                    <p className="text-xs text-[#DEE1FF]/80">
                      5-second Watchdog health check · Auto-Start on Boot
                    </p>
                  </div>
                </div>
              </div>

              {/* Expressive Pillar Mini-Cards */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="m3-spring rounded-[24px] border border-[#444654]/60 bg-[#121318]/90 p-4 space-y-2 hover:border-[#FFADE1]/50">
                  <div className="flex items-center justify-between">
                    <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-[#71295E]/50 text-[#FFADE1]">
                      <Bot className="h-5 w-5" />
                    </div>
                    <span className="rounded-full bg-[#71295E] px-2.5 py-0.5 text-[10px] font-bold text-[#FFD7EE]">
                      44 Tools
                    </span>
                  </div>
                  <div className="font-display text-sm font-bold text-white">
                    Comput AI Agent
                  </div>
                  <p className="text-xs text-[#8F909F] leading-relaxed">
                    Autonomous GUI inspection, MediaProjection capture, and privileged shell execution.
                  </p>
                </div>

                <div className="m3-spring rounded-[24px] border border-[#444654]/60 bg-[#121318]/90 p-4 space-y-2 hover:border-[#66DFB2]/50">
                  <div className="flex items-center justify-between">
                    <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-[#00513B]/50 text-[#66DFB2]">
                      <PackageCheck className="h-5 w-5" />
                    </div>
                    <span className="rounded-full bg-[#00513B] px-2.5 py-0.5 text-[10px] font-bold text-[#85F8C9]">
                      api.sh + WebUI
                    </span>
                  </div>
                  <div className="font-display text-sm font-bold text-white">
                    Rootless ADB Modules
                  </div>
                  <p className="text-xs text-[#8F909F] leading-relaxed">
                    Declarative ZIP modules with crash rollback protection and GitHub catalog.
                  </p>
                </div>

                <div className="m3-spring rounded-[24px] border border-[#444654]/60 bg-[#121318]/90 p-4 space-y-2 hover:border-[#BAC3FF]/50">
                  <div className="flex items-center justify-between">
                    <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-[#354496]/50 text-[#BAC3FF]">
                      <EyeOff className="h-5 w-5" />
                    </div>
                    <span className="rounded-full bg-[#354496] px-2.5 py-0.5 text-[10px] font-bold text-[#DEE1FF]">
                      Anti-Detect
                    </span>
                  </div>
                  <div className="font-display text-sm font-bold text-white">
                    Stealth & USB Spoofing
                  </div>
                  <p className="text-xs text-[#8F909F] leading-relaxed">
                    Custom package disguise and real-time foreground USB debugging hiding.
                  </p>
                </div>

                <div className="m3-spring rounded-[24px] border border-[#444654]/60 bg-[#121318]/90 p-4 space-y-2 hover:border-[#FFB4AB]/50">
                  <div className="flex items-center justify-between">
                    <div className="flex h-9 w-9 items-center justify-center rounded-2xl bg-[#93000A]/50 text-[#FFB4AB]">
                      <KeyRound className="h-5 w-5" />
                    </div>
                    <span className="rounded-full bg-[#93000A] px-2.5 py-0.5 text-[10px] font-bold text-[#FFDAD6]">
                      DPM · Firewall
                    </span>
                  </div>
                  <div className="font-display text-sm font-bold text-white">
                    Device Owner & Lab
                  </div>
                  <p className="text-xs text-[#8F909F] leading-relaxed">
                    1-tap DPM activation, Dhizuku bridge, and Zero-VPN per-app UID network firewall.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* BENTO FEATURES SECTION */}
        <section id="features" className="scroll-mt-24">
          <FeaturesBento />
        </section>

        {/* ARCHITECTURE & HOW IT WORKS SECTION */}
        <section
          id="architecture"
          className="scroll-mt-24 rounded-[36px] border border-[#444654]/60 bg-[#1B1C22]/90 p-6 sm:p-10 space-y-8"
        >
          <div className="max-w-3xl">
            <span className="inline-flex items-center gap-1.5 rounded-full bg-[#354496] px-3 py-1 text-xs font-bold text-[#DEE1FF]">
              <Cpu className="h-3.5 w-3.5" />
              Direct Binder IPC Architecture
            </span>
            <h2 className="mt-3 font-display text-2xl sm:text-3xl font-extrabold text-white">
              How Shevery Executes Privileged System Calls
            </h2>
            <p className="mt-2 text-sm sm:text-base text-[#C5C5D6]">
              Instead of spawning slow, fragile <code className="font-mono text-[#BAC3FF]">su</code> shell pipes for every action, Shevery runs a lightweight native daemon under <code className="font-mono text-[#66DFB2]">UID 2000 (shell)</code> or <code className="font-mono text-[#FFADE1]">UID 0 (root)</code> and passes direct Android <code className="font-mono text-[#BAC3FF]">IBinder</code> references to authorized apps.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="rounded-[28px] bg-[#121318] border border-[#444654]/50 p-6 space-y-3">
              <div className="flex items-center justify-between">
                <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#354496]/50 font-display text-sm font-bold text-[#BAC3FF]">
                  01
                </span>
                <span className="font-mono text-xs text-[#8F909F]">Wireless ADB / Root</span>
              </div>
              <h3 className="font-display text-lg font-bold text-white">
                Starter & 5s Native Watchdog
              </h3>
              <p className="text-xs leading-relaxed text-[#C5C5D6]">
                Pairs over Android 11–17 Wireless Debugging or Root, launches <code className="font-mono text-[#BAC3FF]">shizuku_server</code>, and attaches a dual-layer watchdog that checks binder liveness every 5 seconds and auto-restarts on boot or Wi-Fi reconnect.
              </p>
            </div>

            <div className="rounded-[28px] bg-[#121318] border border-[#444654]/50 p-6 space-y-3">
              <div className="flex items-center justify-between">
                <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#71295E]/50 font-display text-sm font-bold text-[#FFADE1]">
                  02
                </span>
                <span className="font-mono text-xs text-[#8F909F]">Binder Wrapper</span>
              </div>
              <h3 className="font-display text-lg font-bold text-white">
                Shizuku, Sui & Dhizuku Bridges
              </h3>
              <p className="text-xs leading-relaxed text-[#C5C5D6]">
                Exposes standard <code className="font-mono text-[#FFADE1]">ShizukuBinderWrapper</code> plus a built-in <code className="font-mono text-[#FFADE1]">DhizukuProvider</code> so both Shizuku and DeviceOwner/Dhizuku apps work seamlessly side by side.
              </p>
            </div>

            <div className="rounded-[28px] bg-[#121318] border border-[#444654]/50 p-6 space-y-3">
              <div className="flex items-center justify-between">
                <span className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#00513B]/50 font-display text-sm font-bold text-[#66DFB2]">
                  03
                </span>
                <span className="font-mono text-xs text-[#8F909F]">Autonomous Execution</span>
              </div>
              <h3 className="font-display text-lg font-bold text-white">
                Comput Agent & ADB Modules
              </h3>
              <p className="text-xs leading-relaxed text-[#C5C5D6]">
                Executes declarative <code className="font-mono text-[#66DFB2]">api.sh</code> modules on startup with bootloop rollback protection, and powers the 44-tool Comput AI agent for GUI and system automation.
              </p>
            </div>
          </div>
        </section>

        {/* INTERACTIVE GUIDES & DOCUMENTATION HUB */}
        <GuidesAndDocsHub />
      </main>

      {/* Footer (No Logo) */}
      <footer className="border-t border-[#444654]/40 bg-[#0E0F14] py-12">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div>
            <div className="font-display text-sm font-bold text-white">
              Shevery · Material 3 Expressive Privilege Manager
            </div>
            <div className="text-xs text-[#8F909F]">
              Maintained by HmnDev-Tech · Open Source under Apache-2.0
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-4 text-xs font-semibold text-[#C5C5D6]">
            <a
              href="https://github.com/HmnDev-Tech/shevery"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-white transition-colors"
            >
              GitHub Repository
            </a>
            <a
              href="https://github.com/HmnDev-Tech/shevery/releases"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-white transition-colors"
            >
              Releases & APKs
            </a>
            <a
              href="https://github.com/HmnDev-Tech/shevery/issues"
              target="_blank"
              rel="noopener noreferrer"
              className="hover:text-white transition-colors"
            >
              Issue Tracker
            </a>
          </div>
        </div>
      </footer>
    </div>
  )
}
