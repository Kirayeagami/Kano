package app.kano.core

enum class ExtractionState { NOT_REVIEWED, NOT_NEEDED, REQUIRED, SAVED }
data class CleanupCandidate(
    val id: String,
    val fingerprint: String,
    val extraction: ExtractionState,
    val savedKnowledgeId: String? = null,
    val isAccessible: Boolean,
)
data class CleanupConfirmation(val id: String, val fingerprint: String)

sealed interface CleanupDecision {
    data object MayRequestSystemConfirmation : CleanupDecision
    data class Blocked(val reason: String) : CleanupDecision
}

/** Pure prerequisite policy; never deletes files and never replaces OS confirmation. */
object CleanupPolicy {
    fun evaluate(item: CleanupCandidate, confirmation: CleanupConfirmation?): CleanupDecision = when {
        !item.isAccessible -> CleanupDecision.Blocked("Source access was revoked.")
        item.fingerprint.isBlank() -> CleanupDecision.Blocked("Recheck the original file before cleanup.")
        item.extraction == ExtractionState.NOT_REVIEWED -> CleanupDecision.Blocked("Review useful information first.")
        item.extraction == ExtractionState.REQUIRED -> CleanupDecision.Blocked("Save useful information first.")
        item.extraction == ExtractionState.SAVED && item.savedKnowledgeId.isNullOrBlank() ->
            CleanupDecision.Blocked("Verify the saved knowledge record first.")
        confirmation?.id != item.id || confirmation.fingerprint != item.fingerprint ->
            CleanupDecision.Blocked("Confirm this exact file before cleanup.")
        else -> CleanupDecision.MayRequestSystemConfirmation
    }
}
