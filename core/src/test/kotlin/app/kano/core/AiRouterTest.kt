package app.kano.core

import java.time.Instant
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRouterTest {
    private class SpyProvider(override val isLocal: Boolean) : AiProvider {
        override val id = "spy"
        override val capabilities = setOf(AiCapability.SUMMARIZE)
        var calls = 0
        override suspend fun execute(request: AiRequest): AiResult { calls++; return AiResult.Answer("result", id) }
    }
    private val request = AiRequest("Private document", Sensitivity.PERSONAL, AiCapability.SUMMARIZE, "Test")
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    @Test fun cloudProviderNeverReceivesDeniedPayload() = runImmediate {
        val provider = SpyProvider(false)
        val result = AiRouter(listOf(provider), PrivacyFirewall()).execute(request, "spy", null, now)
        assertTrue(result is AiResult.Blocked)
        assertEquals(0, provider.calls)
    }
    @Test fun localProviderCanProcessPrivateContentWithoutCloudConsent() = runImmediate {
        val provider = SpyProvider(true)
        val result = AiRouter(listOf(provider), PrivacyFirewall()).execute(request, "spy", null, now)
        assertTrue(result is AiResult.Answer)
        assertEquals(1, provider.calls)
    }
    @Test fun unknownProviderAndUnsupportedCapabilityDoNotFallback() = runImmediate {
        val provider = SpyProvider(true)
        val router = AiRouter(listOf(provider), PrivacyFirewall())
        assertTrue(router.execute(request, "missing", null, now) is AiResult.Unavailable)
        assertTrue(router.execute(request.copy(capability = AiCapability.RESEARCH), "spy", null, now) is AiResult.Unavailable)
        assertEquals(0, provider.calls)
    }
    @Test(expected = IllegalArgumentException::class) fun duplicateProviderIdsAreRejected() {
        AiRouter(listOf(SpyProvider(true), SpyProvider(false)), PrivacyFirewall())
    }

    // These providers never suspend; fail if a test unexpectedly becomes asynchronous.
    private fun runImmediate(block: suspend () -> Unit) {
        var outcome: Result<Unit>? = null
        block.startCoroutine(object : Continuation<Unit> {
            override val context = EmptyCoroutineContext
            override fun resumeWith(result: Result<Unit>) { outcome = result }
        })
        checkNotNull(outcome) { "Unexpected suspension in immediate test" }.getOrThrow()
    }
}
