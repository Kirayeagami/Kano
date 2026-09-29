package app.kano.core

import java.security.MessageDigest
import java.time.Instant

enum class Sensitivity { PUBLIC, PERSONAL, FINANCIAL, SECRET, UNKNOWN }
enum class AiCapability { REASON, ANALYZE_IMAGE, SUMMARIZE, CLASSIFY, RESEARCH, EXTRACT }

data class AiRequest(
    val text: String,
    val sensitivity: Sensitivity,
    val capability: AiCapability,
    val purpose: String,
)

/** Consent must originate from a visible review of this exact payload and provider. */
data class CloudConsent(
    val payloadDigest: String,
    val providerId: String,
    val purpose: String,
    val capability: AiCapability,
    val expiresAt: Instant,
)

sealed interface EgressDecision {
    data object Allowed : EgressDecision
    data class Denied(val reason: Reason) : EgressDecision
    enum class Reason { INVALID_REQUEST, SENSITIVE, SECRET_DETECTED, CONSENT_REQUIRED, CONSENT_MISMATCH, EXPIRED }
}

/** Conservative text-only boundary, not a comprehensive PII or image classifier. */
class PrivacyFirewall {
    private val secretSignals = listOf(
        Regex("(?i)\\b(password|passwd|otp|api[_ -]?key|access[_ -]?token|bearer)\\b"),
        Regex("-----BEGIN [A-Z ]*PRIVATE KEY-----"),
        Regex("\\bsk-[A-Za-z0-9_-]{12,}\\b"),
        Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"),
        Regex("\\b(?:[0-9][ -]?){9,19}\\b"),
    )

    fun evaluate(request: AiRequest, providerId: String, consent: CloudConsent?, now: Instant): EgressDecision {
        if (request.text.isBlank() || request.text.length > 32_000 || request.purpose.isBlank() || providerId.isBlank()) {
            return deny(EgressDecision.Reason.INVALID_REQUEST)
        }
        if (secretSignals.any { it.containsMatchIn(request.text) }) return deny(EgressDecision.Reason.SECRET_DETECTED)
        // Public classification is required; no consent overrides sensitive/unknown content in v0.1.
        if (request.sensitivity != Sensitivity.PUBLIC) return deny(EgressDecision.Reason.SENSITIVE)
        if (consent == null) return deny(EgressDecision.Reason.CONSENT_REQUIRED)
        if (!now.isBefore(consent.expiresAt)) return deny(EgressDecision.Reason.EXPIRED)
        if (consent.payloadDigest != digest(request.text) || consent.providerId != providerId ||
            consent.purpose != request.purpose || consent.capability != request.capability) {
            return deny(EgressDecision.Reason.CONSENT_MISMATCH)
        }
        return EgressDecision.Allowed
    }

    private fun deny(reason: EgressDecision.Reason) = EgressDecision.Denied(reason)

    companion object {
        fun digest(text: String): String = MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
