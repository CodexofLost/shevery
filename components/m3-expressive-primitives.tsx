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

interface ExpressiveMorphBadgeProps {
  children: React.ReactNode
  variant?: "primary" | "secondary" | "tertiary"
}

export function ExpressiveMorphBadge({
  children,
  variant = "primary",
}: ExpressiveMorphBadgeProps) {
  const styles = {
    primary: "bg-[#354496]/50 border-[#BAC3FF]/35 text-[#DEE1FF]",
    secondary: "bg-[#71295E]/50 border-[#FFADE1]/35 text-[#FFD7EE]",
    tertiary: "bg-[#00513B]/50 border-[#66DFB2]/35 text-[#85F8C9]",
  }

  return (
    <span
      className={`inline-flex items-center gap-2 rounded-full border px-3.5 py-1 text-xs font-semibold backdrop-blur-md transition-transform duration-300 hover:scale-[1.03] ${styles[variant]}`}
    >
      {children}
    </span>
  )
}

interface ExpressiveStatusBlobProps {
  active?: boolean
  children: React.ReactNode
}

export function ExpressiveStatusBlob({
  active = true,
  children,
}: ExpressiveStatusBlobProps) {
  return (
    <div
      className={`m3-blob flex h-12 w-12 shrink-0 items-center justify-center transition-colors duration-500 ${
        active
          ? "bg-[#00513B]/90 border border-[#66DFB2]/40 shadow-[0_0_25px_rgba(102,223,178,0.25)]"
          : "bg-[#93000A]/80 border border-[#FFB4AB]/40"
      }`}
    >
      {children}
    </div>
  )
}
