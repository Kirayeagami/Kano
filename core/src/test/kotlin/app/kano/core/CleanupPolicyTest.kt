package app.kano.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupPolicyTest {
    private val candidate = CleanupCandidate("one", "hash", ExtractionState.SAVED, "knowledge-1", true)
    private val confirmation = CleanupConfirmation("one", "hash")

    @Test fun validPrerequisitesOnlyPermitRequestingSystemConfirmation() {
        assertEquals(CleanupDecision.MayRequestSystemConfirmation, CleanupPolicy.evaluate(candidate, confirmation))
    }
    @Test fun extractionMustBeReviewedAndPersisted() {
        listOf(candidate.copy(extraction = ExtractionState.REQUIRED), candidate.copy(extraction = ExtractionState.NOT_REVIEWED),
            candidate.copy(savedKnowledgeId = null), candidate.copy(savedKnowledgeId = " ")).forEach {
            assertTrue(CleanupPolicy.evaluate(it, confirmation) is CleanupDecision.Blocked)
        }
    }
    @Test fun staleMissingAndWrongItemConfirmationFailClosed() {
        listOf(null, confirmation.copy(id = "two"), confirmation.copy(fingerprint = "changed")).forEach {
            assertTrue(CleanupPolicy.evaluate(candidate, it) is CleanupDecision.Blocked)
        }
    }
    @Test fun revokedSourceAndMissingFingerprintBlockCleanup() {
        assertTrue(CleanupPolicy.evaluate(candidate.copy(isAccessible = false), confirmation) is CleanupDecision.Blocked)
        assertTrue(CleanupPolicy.evaluate(candidate.copy(fingerprint = ""), confirmation) is CleanupDecision.Blocked)
    }
    @Test fun reviewedNoExtractionNeededStillRequiresConfirmation() {
        assertEquals(CleanupDecision.MayRequestSystemConfirmation, CleanupPolicy.evaluate(candidate.copy(extraction = ExtractionState.NOT_NEEDED, savedKnowledgeId = null), confirmation))
    }
}
