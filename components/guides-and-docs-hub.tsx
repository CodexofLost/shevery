"use client"

import React, { useState } from "react"
import {
  BookOpen,
  Terminal,
  Copy,
  Check,
  ChevronRight,
  Sparkles,
  ExternalLink,
  Code2,
  CheckCircle2,
} from "lucide-react"
import { GUIDE_DOCS } from "@/lib/shevery-data"

export function GuidesAndDocsHub() {
  const [activeGuideId, setActiveGuideId] = useState<string>(GUIDE_DOCS[0].id)
  const [copied, setCopied] = useState(false)

  const currentGuide =
    GUIDE_DOCS.find((g) => g.id === activeGuideId) || GUIDE_DOCS[0]

  const handleCopyCode = () => {
    navigator.clipboard.writeText(currentGuide.codeSnippet)
    setCopied(true)
    setTimeout(() => setCopied(false), 1800)
  }

  return (
    <section id="guides" className="scroll-mt-24 space-y-8">
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-2 rounded-full bg-[#00513B]/60 border border-[#66DFB2]/30 px-3.5 py-1 text-xs font-semibold text-[#85F8C9]">
            <BookOpen className="h-3.5 w-3.5 text-[#66DFB2]" />
            Interactive Documentation & Guides
          </div>
          <h2 className="mt-3 font-display text-3xl sm:text-4xl font-extrabold tracking-tight text-[#E3E1E9]">
            Complete Shevery Knowledge Base
          </h2>
          <p className="mt-2 max-w-2xl text-sm sm:text-base text-[#C5C5D6]">
            Step-by-step setup guides, ADB module API references, Device Owner commands, and Tasker broadcast specifications synced directly with the repository.
          </p>
        </div>

        <a
          href="https://github.com/HmnDev-Tech/shevery/tree/main/docs"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 self-start rounded-full border border-[#444654] bg-[#1F2026] px-4 py-2 text-xs font-semibold text-[#E3E1E9] hover:border-[#BAC3FF] hover:text-[#BAC3FF] transition-colors"
        >
          Browse /docs on GitHub
          <ExternalLink className="h-3.5 w-3.5" />
        </a>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Navigation Column */}
        <div className="lg:col-span-4 space-y-2">
          {GUIDE_DOCS.map((guide) => {
            const isSelected = guide.id === activeGuideId
            return (
              <button
                key={guide.id}
                type="button"
                onClick={() => setActiveGuideId(guide.id)}
                className={`w-full text-left rounded-[24px] p-4 transition-all duration-300 border ${
                  isSelected
                    ? "bg-[#354496] border-[#BAC3FF]/60 text-[#DEE1FF] shadow-md scale-[1.01]"
                    : "bg-[#1B1C22]/90 border-[#444654]/50 text-[#C5C5D6] hover:bg-[#282932] hover:text-white"
                }`}
              >
                <div className="flex items-center justify-between">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold uppercase tracking-wider ${
                      isSelected
                        ? "bg-black/25 text-[#66DFB2]"
                        : "bg-[#282932] text-[#BAC3FF]"
                    }`}
                  >
                    {guide.category}
                  </span>
                  <span className="text-[11px] font-mono opacity-80">{guide.badge}</span>
                </div>
                <div className="mt-2 flex items-center justify-between gap-2">
                  <span className="font-display text-sm font-bold">{guide.title}</span>
                  <ChevronRight
                    className={`h-4 w-4 shrink-0 transition-transform ${
                      isSelected ? "translate-x-0.5 text-white" : "text-[#8F909F]"
                    }`}
                  />
                </div>
              </button>
            )
          })}
        </div>

        {/* Right Guide Detail & Code Panel */}
        <div className="lg:col-span-8 rounded-[32px] border border-[#444654]/70 bg-[#1B1C22] p-6 sm:p-8 flex flex-col justify-between space-y-6">
          <div className="space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#444654]/50 pb-4">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-[#66DFB2]">
                  {currentGuide.category}
                </span>
                <h3 className="mt-1 font-display text-2xl font-extrabold text-white">
                  {currentGuide.title}
                </h3>
              </div>
              <span className="rounded-full bg-[#354496]/60 border border-[#BAC3FF]/30 px-3.5 py-1 text-xs font-semibold text-[#DEE1FF]">
                {currentGuide.badge}
              </span>
            </div>

            <p className="text-sm sm:text-base leading-relaxed text-[#C5C5D6]">
              {currentGuide.summary}
            </p>

            {/* Step-by-step checklist */}
            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-[#BAC3FF]">
                Step-by-Step Workflow
              </h4>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {currentGuide.steps.map((step, index) => (
                  <div
                    key={index}
                    className="flex items-start gap-3 rounded-2xl bg-[#121318]/80 border border-[#444654]/40 p-3.5"
                  >
                    <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-[#354496] font-display text-xs font-bold text-[#DEE1FF]">
                      {index + 1}
                    </span>
                    <span className="text-xs leading-relaxed text-[#E3E1E9]">{step}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Code / Command Block */}
          <div className="rounded-[24px] border border-[#444654]/70 bg-[#0E0F14] overflow-hidden">
            <div className="flex items-center justify-between border-b border-[#444654]/40 bg-[#171820] px-4 py-2.5">
              <div className="flex items-center gap-2 text-xs font-mono text-[#BAC3FF]">
                <Terminal className="h-3.5 w-3.5 text-[#66DFB2]" />
                <span>{currentGuide.codeTitle}</span>
              </div>
              <button
                type="button"
                onClick={handleCopyCode}
                className="inline-flex items-center gap-1.5 rounded-full bg-[#282932] px-3 py-1 text-[11px] font-semibold text-[#E3E1E9] hover:bg-[#354496] transition-colors"
              >
                {copied ? (
                  <>
                    <Check className="h-3.5 w-3.5 text-[#66DFB2]" />
                    Copied
                  </>
                ) : (
                  <>
                    <Copy className="h-3.5 w-3.5" />
                    Copy Snippet
                  </>
                )}
              </button>
            </div>
            <pre className="overflow-x-auto p-4 font-mono text-xs leading-relaxed text-[#E3E1E9]">
              <code>{currentGuide.codeSnippet}</code>
            </pre>
          </div>
        </div>
      </div>
    </section>
  )
}
