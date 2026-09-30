package app.kano.core

import org.junit.Assert.*
import org.junit.Test

class VisionPolicyTest {
    @Test fun secretsSuppressPersistenceRatherThanOnlyReplacingLabels() {
        listOf("password: hunter2", "OTP 123456", "WIFI:S:network;P:pass123;;", "api_key abc").forEach {
            assertTrue(VisionPolicy.hasSensitiveSignal(it))
            assertTrue(VisionPolicy.webUrls(it).isEmpty())
        }
    }
    @Test fun webLinksRejectExecutableSchemesAndCredentials() {
        listOf("javascript:alert(1)", "https://user:pass@example.com", "https://example.com?token=value",
            "https://example.com/#private").forEach { assertNull(VisionPolicy.publicWebUrl(it)) }
        assertEquals("https://developer.android.com", VisionPolicy.publicWebUrl("https://developer.android.com"))
    }
    @Test fun candidatesAreBoundedAndDeduplicated() {
        assertEquals(listOf("https://example.com/page"),
            VisionPolicy.webUrls("Notes https://example.com/page. Again https://example.com/page"))
    }
    @Test fun credentialsInsideTextExcerptsUseTheSamePersistenceGate() {
        assertTrue(VisionPolicy.hasSensitiveSignal("Reset at https://example.com/reset?code=AbCdEfGh"))
        assertTrue(VisionPolicy.hasSensitiveSignal("Read https://example.com/#private"))
        assertTrue(VisionPolicy.hasSensitiveSignal("Read https://name:pass@example.com"))
        assertFalse(VisionPolicy.hasSensitiveSignal("Read https://example.com/docs."))
    }
}
