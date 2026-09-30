package app.kano.core

import java.net.URI

/** Conservative local persistence gate; heuristics do not prove content is non-sensitive. */
object VisionPolicy {
    private val sensitive = Regex(
        "(?i)\\b(password|passwd|otp|api[_ -]?key|access[_ -]?token|bearer|secret)\\b|sk-[A-Za-z0-9_-]{12,}|-----BEGIN .*PRIVATE KEY|\\b(?:[0-9][ -]?){9,19}\\b|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|WIFI:",
    )
    private val links = Regex("(?i)https?://[^\\s<>\\\"']+")

    fun hasSensitiveSignal(text: String): Boolean = sensitive.containsMatchIn(text) || links.findAll(text).any { match ->
        // Apply the URL credential policy to excerpts too, not just the URL field.
        runCatching {
            val uri = URI(match.value.trimEnd('.', ',', ')', ']', ';'))
            uri.rawUserInfo != null || uri.rawQuery != null || uri.rawFragment != null
        }.getOrDefault(true)
    }

    /** No automatic opening; reject credentials, query strings and non-web schemes. */
    fun publicWebUrl(text: String): String? = runCatching {
        val input = text.trim().trimEnd('.', ',', ')', ']', ';')
        if (input.length > 500 || hasSensitiveSignal(input)) return null
        val uri = URI(input)
        if (uri.scheme?.lowercase() !in setOf("https", "http") || uri.host.isNullOrBlank() ||
            uri.rawUserInfo != null || uri.rawQuery != null || uri.rawFragment != null) return null
        uri.toASCIIString()
    }.getOrNull()

    fun webUrls(text: String): List<String> =
        if (hasSensitiveSignal(text)) emptyList()
        else links.findAll(text).mapNotNull { publicWebUrl(it.value) }.distinct().take(20).toList()
}
