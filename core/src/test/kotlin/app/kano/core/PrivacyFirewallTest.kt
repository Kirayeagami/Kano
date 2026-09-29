package app.kano.core

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyFirewallTest {
    private val now = Instant.parse("2026-01-01T00:00:00Z")
    private val firewall = PrivacyFirewall()
    private val request = AiRequest("Summarize the public Android documentation.", Sensitivity.PUBLIC, AiCapability.SUMMARIZE, "Public research")
    private fun consent() = CloudConsent(PrivacyFirewall.digest(request.text), "openai", request.purpose, request.capability, now.plusSeconds(60))

    @Test fun publicContentStillRequiresConsent() {
        assertEquals(EgressDecision.Denied(EgressDecision.Reason.CONSENT_REQUIRED), firewall.evaluate(request, "openai", null, now))
    }
    @Test fun exactUnexpiredConsentAllowsPublicText() {
        assertEquals(EgressDecision.Allowed, firewall.evaluate(request, "openai", consent(), now))
    }
    @Test fun consentCannotMoveToAnotherProviderPayloadPurposeOrCapability() {
        val variants = listOf(request.copy(text = "Different text"), request.copy(purpose = "Other purpose"), request.copy(capability = AiCapability.RESEARCH))
        variants.forEach { assertEquals(EgressDecision.Denied(EgressDecision.Reason.CONSENT_MISMATCH), firewall.evaluate(it, "openai", consent(), now)) }
        assertEquals(EgressDecision.Denied(EgressDecision.Reason.CONSENT_MISMATCH), firewall.evaluate(request, "gemini", consent(), now))
    }
    @Test fun expiryBoundaryIsDenied() {
        assertEquals(EgressDecision.Denied(EgressDecision.Reason.EXPIRED), firewall.evaluate(request, "openai", consent(), now.plusSeconds(60)))
    }
    @Test fun sensitiveAndUnknownNeverPassEvenWithConsent() {
        Sensitivity.entries.filter { it != Sensitivity.PUBLIC }.forEach {
            assertEquals(EgressDecision.Denied(EgressDecision.Reason.SENSITIVE), firewall.evaluate(request.copy(sensitivity = it), "openai", consent(), now))
        }
    }
    @Test fun commonSecretSignalsOverridePublicLabel() {
        listOf("OTP 123456", "Bearer abc", "my PASSWORD is public", "a@example.com", "4111 1111 1111 1111", "sk-abcdefghijklmnop", "-----BEGIN RSA PRIVATE KEY-----").forEach {
            assertEquals(EgressDecision.Denied(EgressDecision.Reason.SECRET_DETECTED), firewall.evaluate(request.copy(text = it), "openai", consent(), now))
        }
    }
    @Test fun emptyAndOversizedRequestsFailClosed() {
        listOf("", " ", "x".repeat(32_001)).forEach {
            assertEquals(EgressDecision.Denied(EgressDecision.Reason.INVALID_REQUEST), firewall.evaluate(request.copy(text = it), "openai", consent(), now))
        }
    }
    @Test fun digestUsesUtf8AndIsDeterministic() {
        assertEquals(PrivacyFirewall.digest("नमस्ते"), PrivacyFirewall.digest("नमस्ते"))
        assertTrue(PrivacyFirewall.digest("one") != PrivacyFirewall.digest("two"))
        assertEquals(64, PrivacyFirewall.digest("one").length)
    }
}
