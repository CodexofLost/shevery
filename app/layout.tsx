import type { Metadata, Viewport } from "next"
import { Plus_Jakarta_Sans, JetBrains_Mono } from "next/font/google"
import "./globals.css"

const jakarta = Plus_Jakarta_Sans({
  subsets: ["latin", "cyrillic-ext"],
  weight: ["400", "500", "600", "700", "800"],
  variable: "--font-jakarta",
})

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin", "cyrillic"],
  weight: ["400", "500", "600"],
  variable: "--font-jetbrains",
})

export const metadata: Metadata = {
  title: "Shevery — Material 3 Expressive Privileged API, ADB Modules & AI Console for Android",
  description:
    "Next-generation Shizuku & Dhizuku evolution for Android 8.0–17. Featuring Material 3 Expressive UI, Comput AI Agent Console, ADB Modules with WebUI, Wireless Boot Autostart, Stealth Edition, and Tasker automation.",
  icons: {
    icon: "/icon.webp",
    apple: "/icon.webp",
  },
}

export const viewport: Viewport = {
  themeColor: "#0E1117",
  width: "device-width",
  initialScale: 1,
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ru" className={`${jakarta.variable} ${jetbrainsMono.variable} dark`}>
      <body className="font-sans antialiased bg-[#0E1117] text-[#E5EAF5] selection:bg-[#B8CCF0] selection:text-[#132238]">
        {children}
      </body>
    </html>
  )
}
