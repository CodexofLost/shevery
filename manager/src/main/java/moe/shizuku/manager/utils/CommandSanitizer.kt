package moe.shizuku.manager.utils

object CommandSanitizer {
    fun sanitize(raw: String): String {
        var t = raw.trim()
        t = REGEX_THINKING.replace(t, "")
        return REGEX_FENCE.replace(t) { m -> m.groupValues[1] }.trim()
    }

    private val REGEX_THINKING = Regex("(?is)<thinking>.*?</thinking>")
    private val REGEX_FENCE = Regex("(?is)^```[a-zA-Z0-9_-]*\\s*\\n(.*?)\\n```\\s*$")
}
