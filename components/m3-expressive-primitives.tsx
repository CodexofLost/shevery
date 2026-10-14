"use client"

import React from "react"
import { Check, X } from "lucide-react"

interface ExpressiveSwitchProps {
  checked: boolean
  onCheckedChange: (checked: boolean) => void
  label?: string
  darkOnLight?: boolean
}

export function ExpressiveSwitch({
  checked,
  onCheckedChange,
  label,
  darkOnLight = false,
}: ExpressiveSwitchProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={label}
      onClick={(e) => {
        e.stopPropagation()
        onCheckedChange(!checked)
      }}
      className={`relative inline-flex h-8 w-14 shrink-0 cursor-pointer items-center rounded-full p-1 m3-spring focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#B8CCF0] ${
        checked
          ? darkOnLight
            ? "bg-[#9BB8ED]"
            : "bg-[#B8CCF0]"
          : darkOnLight
            ? "bg-[#B9ADCE] border-2 border-[#6B5E82]"
            : "bg-[#263042] border-2 border-[#5D6B82]"
      }`}
    >
      <span
        className={`flex items-center justify-center rounded-full m3-spring ${
          checked
            ? "h-6 w-6 translate-x-6 bg-[#23324A] text-[#DCE7FF] scale-105"
            : "h-4 w-4 translate-x-0.5 bg-[#8E9BB3] text-transparent scale-95"
        }`}
      >
        {checked ? (
          <Check className="h-3.5 w-3.5 stroke-[3]" />
        ) : (
          <X className="h-2.5 w-2.5 opacity-0" />
        )}
      </span>
    </button>
  )
}

export interface FloatingNavTab {
  id: string
  label: string
  icon: React.ReactNode
}

interface ExpressiveFloatingToolbarProps {
  tabs: FloatingNavTab[]
  activeTab: string
  onSelect: (id: string) => void
  className?: string
}

export function ExpressiveFloatingToolbar({
  tabs,
  activeTab,
  onSelect,
  className = "",
}: ExpressiveFloatingToolbarProps) {
  return (
    <div
      role="tablist"
      aria-label="Material 3 Expressive Navigation"
      className={`inline-flex items-center gap-1 rounded-full bg-[#38465E]/95 p-1.5 shadow-[0_12px_32px_rgba(0,0,0,0.55)] backdrop-blur-md border border-[#4E607E]/40 ${className}`}
    >
      {tabs.map((tab) => {
        const isSelected = tab.id === activeTab
        return (
          <button
            key={tab.id}
            type="button"
            role="tab"
            aria-selected={isSelected}
            onClick={() => onSelect(tab.id)}
            className={`group relative flex h-11 items-center justify-center rounded-full px-3.5 text-sm font-bold m3-spring cursor-pointer select-none ${
              isSelected
                ? "bg-[#B8CCF0] text-[#132238] px-4 shadow-sm scale-[1.02]"
                : "bg-transparent text-[#DCE7FF]/85 hover:bg-[#495A78]/50 hover:text-white"
            }`}
          >
            <span className="flex items-center gap-2">
              <span className="shrink-0 transition-transform duration-300 group-active:scale-90">
                {tab.icon}
              </span>
              <span
                className={`overflow-hidden whitespace-nowrap transition-all duration-300 ${
                  isSelected
                    ? "max-w-[140px] opacity-100 ml-0.5"
                    : "max-w-0 opacity-0"
                }`}
              >
                {tab.label}
              </span>
            </span>
          </button>
        )
      })}
    </div>
  )
}
