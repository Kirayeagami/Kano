package app.kano.core

import java.time.Instant

sealed interface AiResult {
    data class Answer(val text: String, val providerId: String) : AiResult
    data class Unavailable(val reason: String) : AiResult
    data class Blocked(val reason: EgressDecision.Reason) : AiResult
}

interface AiProvider {
    val id: String
    val isLocal: Boolean
    val capabilities: Set<AiCapability>
    suspend fun execute(request: AiRequest): AiResult
}

/** No automatic provider fallback: fallback would change the data recipient. */
class AiRouter(providers: List<AiProvider>, private val firewall: PrivacyFirewall) {
    private val providersById = providers.associateBy { it.id }
    init { require(providersById.size == providers.size) { "Provider IDs must be unique" } }

    suspend fun execute(request: AiRequest, providerId: String, consent: CloudConsent?, now: Instant): AiResult {
        val provider = providersById[providerId] ?: return AiResult.Unavailable("Provider is not installed.")
        if (request.capability !in provider.capabilities) return AiResult.Unavailable("Provider does not support this task.")
        if (!provider.isLocal) {
            val decision = firewall.evaluate(request, providerId, consent, now)
            if (decision is EgressDecision.Denied) return AiResult.Blocked(decision.reason)
        }
        return provider.execute(request)
    }
}
