package moe.shizuku.manager.about

import java.text.DateFormat
import java.util.Date
import java.util.GregorianCalendar

internal data class AlsoTryApp(
    val name: String,
    val summary: String,
    val url: String
)

internal object AlsoTry {

    const val DAILY_PICK_COUNT = 3

    val featured: List<AlsoTryApp> = listOf(
        AlsoTryApp(
            name = "BLE-Droid",
            summary = "Advanced Bluetooth LE app built with Material 3 Expressive",
            url = "https://github.com/HmnDev-Tech/BLE-Droid"
        ),
        AlsoTryApp(
            name = "KArchiver",
            summary = "File manager and archiver built with Kotlin and Material 3 Expressive",
            url = "https://github.com/sysrv64/KArchiver"
        ),
        AlsoTryApp(
            name = "DSU Extended",
            summary = "DSU Sideloader fork with Material 3 Expressive and new features",
            url = "https://github.com/kerneldroid/Dsu-Extended"
        )
    )

    val githubDaily: List<AlsoTryApp> = listOf(
        AlsoTryApp(
            name = "Seal",
            summary = "Video and audio downloader built on yt-dlp",
            url = "https://github.com/JunkFood02/Seal"
        ),
        AlsoTryApp(
            name = "Mihon",
            summary = "Free and open source manga reader",
            url = "https://github.com/mihonapp/mihon"
        ),
        AlsoTryApp(
            name = "ReVanced Manager",
            summary = "Applies ReVanced patches to your Android apps",
            url = "https://github.com/ReVanced/revanced-manager"
        ),
        AlsoTryApp(
            name = "Komi Store",
            summary = "Open source app store for releases hosted on GitHub",
            url = "https://github.com/komi-store/komi-store"
        ),
        AlsoTryApp(
            name = "Image Toolbox",
            summary = "Image editor with cropping, filters, OCR and more",
            url = "https://github.com/T8RIN/ImageToolbox"
        ),
        AlsoTryApp(
            name = "LibreTube",
            summary = "Privacy-friendly alternative frontend for YouTube",
            url = "https://github.com/libre-tube/LibreTube"
        ),
        AlsoTryApp(
            name = "Metrolist",
            summary = "YouTube Music client for Android",
            url = "https://github.com/MetrolistGroup/Metrolist"
        ),
        AlsoTryApp(
            name = "SimpMusic",
            summary = "Music app that streams through YouTube Music",
            url = "https://github.com/maxrave-dev/SimpMusic"
        ),
        AlsoTryApp(
            name = "Thunderbird",
            summary = "Open source email app for Android",
            url = "https://github.com/thunderbird/thunderbird-android"
        ),
        AlsoTryApp(
            name = "GKD",
            summary = "Taps through splash screens using subscription rules",
            url = "https://github.com/gkd-kit/gkd"
        ),
        AlsoTryApp(
            name = "SmsForwarder",
            summary = "Forwards SMS, calls and app notifications onward",
            url = "https://github.com/pppscn/SmsForwarder"
        ),
        AlsoTryApp(
            name = "LibrePods",
            summary = "Brings AirPods features over to Android",
            url = "https://github.com/librepods-org/librepods"
        ),
        AlsoTryApp(
            name = "Book's Story",
            summary = "Material You eBook reader built with Jetpack Compose",
            url = "https://github.com/Acclorite/book-story"
        ),
        AlsoTryApp(
            name = "Myne",
            summary = "Downloads and reads ebooks from Project Gutenberg",
            url = "https://github.com/Pool-Of-Tears/Myne"
        ),
        AlsoTryApp(
            name = "Minimalist Weather",
            summary = "Weather app with air quality and 7 day forecasts",
            url = "https://github.com/ByronNote/MinimalistWeather"
        ),
        AlsoTryApp(
            name = "Flow",
            summary = "YouTube and YouTube Music client with local recommendations",
            url = "https://github.com/A-EDev/Flow"
        ),
        AlsoTryApp(
            name = "ReFra",
            summary = "Media gallery built with Jetpack Compose",
            url = "https://github.com/IacobIonut01/ReFra"
        ),
        AlsoTryApp(
            name = "ZenConverter",
            summary = "Converts files on device with no ads or uploads",
            url = "https://github.com/Jasonzhu1207/ZenConverter"
        )
    )

    val shizukuDaily: List<AlsoTryApp> = listOf(
        AlsoTryApp(
            name = "Canta",
            summary = "Uninstall and debloat any app without root, via Shizuku",
            url = "https://github.com/samolego/Canta"
        ),
        AlsoTryApp(
            name = "ShizuTools",
            summary = "Controls the Android system through Shizuku",
            url = "https://github.com/legendsayantan/ShizuTools"
        ),
        AlsoTryApp(
            name = "Inure",
            summary = "App manager with root and Shizuku support",
            url = "https://github.com/Hamza417/Inure"
        ),
        AlsoTryApp(
            name = "InstallWithOptions",
            summary = "Installs APKs on device with advanced options through Shizuku",
            url = "https://github.com/zacharee/InstallWithOptions"
        ),
        AlsoTryApp(
            name = "ShizuCallRecorder",
            summary = "Records calls on a non-rooted device through Shizuku",
            url = "https://github.com/kitsumed/ShizuCallRecorder"
        ),
        AlsoTryApp(
            name = "Universal Installer",
            summary = "Silent split-APK installs and VirusTotal scanning via Shizuku",
            url = "https://github.com/pass-with-high-score/universal-installer"
        ),
        AlsoTryApp(
            name = "AutoTask",
            summary = "Automation assistant driven by Shizuku and accessibility",
            url = "https://github.com/xjunz/AutoTask"
        ),
        AlsoTryApp(
            name = "AutoSkip",
            summary = "Skips app splash screens using Shizuku",
            url = "https://github.com/xjunz/AutoSkip"
        ),
        AlsoTryApp(
            name = "shappky",
            summary = "Stops background apps to boost performance with Shizuku",
            url = "https://github.com/YasserNull/shappky"
        ),
        AlsoTryApp(
            name = "WiFi Password Manager",
            summary = "Manages saved WiFi passwords through Shizuku or root",
            url = "https://github.com/Khh-vu/wifi-password-manager"
        ),
        AlsoTryApp(
            name = "Android Screener",
            summary = "Changes screen resolution and refresh rate via Shizuku",
            url = "https://github.com/jiesou/Android-Screener"
        ),
        AlsoTryApp(
            name = "RebootNya",
            summary = "Reboot utility that supports both root and Shizuku",
            url = "https://github.com/daisukiKaffuChino/RebootNya"
        ),
        AlsoTryApp(
            name = "Buge App Manager",
            summary = "App and permission management that needs Shizuku or root",
            url = "https://github.com/BugeStudioTeam/Buge-App-Manager"
        ),
        AlsoTryApp(
            name = "BatStats",
            summary = "Battery monitor with per-app statistics via Shizuku",
            url = "https://github.com/mlm-games/BatStats"
        ),
        AlsoTryApp(
            name = "FrameX",
            summary = "FPS meter and thermal diagnostics powered by Shizuku",
            url = "https://github.com/MaheshSharan/FrameX-Android"
        ),
        AlsoTryApp(
            name = "DarQ Reborn",
            summary = "Per-app force dark mode for Android 10 and up, via Shizuku",
            url = "https://github.com/Arora-Sir/DarQ-Reborn"
        ),
        AlsoTryApp(
            name = "ShizuStore",
            summary = "App store that installs Shizuku apps from their sources",
            url = "https://github.com/timschneeb/ShizuStore"
        )
    )

    fun dayStamp(): String = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date())

    fun <T> dailyPick(pool: List<T>, count: Int = DAILY_PICK_COUNT): List<T> {
        if (pool.isEmpty()) return emptyList()
        val start = dayIndex() % pool.size
        return (0 until minOf(count, pool.size)).map { pool[(start + it) % pool.size] }
    }

    private fun dayIndex(): Int {
        val calendar = GregorianCalendar()
        return calendar.get(GregorianCalendar.YEAR) * 1000 + calendar.get(GregorianCalendar.DAY_OF_YEAR)
    }
}
