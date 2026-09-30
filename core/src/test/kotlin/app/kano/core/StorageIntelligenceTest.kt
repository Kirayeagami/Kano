package app.kano.core

import java.io.ByteArrayInputStream
import java.util.concurrent.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageIntelligenceTest {
    private fun item(id: String, bytes: Long? = 3, revision: String? = "generation-1") = StorageMetadata(
        id = id, name = "$id.jpg", mime = "image/jpeg", sizeBytes = bytes,
        width = 100, height = 100, revision = revision, distinctFileIdentityConfirmed = true,
    )
    private fun hash(id: String, text: String = "abc", revision: String? = "generation-1") =
        StorageHashEvidence(id, PrivacyFirewall.digest(text), "VERIFIED", revision)

    @Test fun knownMimeOverridesMisleadingExtension() {
        assertEquals("ARCHIVES", StorageRules.category("holiday.jpg", "application/zip", "Pictures"))
        assertEquals("DOCUMENTS", StorageRules.category("photo.png", "application/pdf", null))
        assertEquals("OTHER", StorageRules.category("unfamiliar", "application/x-msdownload", "Download"))
    }

    @Test fun categoriesRecognizeMimeAndConservativeExtensionFallback() {
        mapOf("one.JPG" to "PHOTOS", "one.mp4" to "VIDEOS", "one.FLAC" to "AUDIO", "one.docx" to "DOCUMENTS",
            "one.apk" to "APKS", "one.7z" to "ARCHIVES", "one.exe" to "OTHER", "unknown.zzz" to "UNKNOWN").forEach { (name, category) ->
            assertEquals(category, StorageRules.category(name, null, null))
            assertEquals(category, StorageRules.category(name, "application/octet-stream", null))
        }
        assertEquals("DOCUMENTS", StorageRules.category("untitled", "Text/Plain; charset=utf-8", null))
        assertEquals("UNKNOWN", StorageRules.category("untitled", " ", null))
    }

    @Test fun screenshotAndRecordingCategoriesRequireMediaAndFolderEvidence() {
        assertEquals("SCREENSHOTS", StorageRules.category("one.png", "image/png", "Pictures/Screenshots/"))
        assertEquals("SCREEN_RECORDINGS", StorageRules.category("one.mp4", "video/mp4", "Movies\\ScreenRecorder\\"))
        assertEquals("PHOTOS", StorageRules.category("Screenshot-2020.png", "image/png", "DCIM/Camera"))
        assertEquals("PHOTOS", StorageRules.category("one.png", "image/png", "Pictures/MyScreenshotsBackup/"))
        assertEquals("DOCUMENTS", StorageRules.category("one.pdf", "application/pdf", "Screenshots"))
    }

    @Test fun downloadIsLocationFacetNotASecondTypeCategory() {
        assertTrue(StorageRules.isDownload("Download/subfolder/"))
        assertTrue(StorageRules.isDownload("/storage/emulated/0/Downloads/"))
        assertFalse(StorageRules.isDownload("Pictures/downloaded-photos/"))
        assertEquals("DOCUMENTS", StorageRules.category("manual.pdf", "application/pdf", "Download/"))
    }

    @Test fun unknownAndSensitiveContentAreKept() {
        assertEquals("UNKNOWN", StorageRules.classification())
        assertEquals("KEEP", StorageRules.disposition("UNKNOWN"))
        assertEquals("KEEP", StorageRules.disposition("unexpected-classification"))
        assertEquals("SENSITIVE", StorageRules.classification(sensitivity = "SECRET", exactDuplicate = true))
        assertEquals("KEEP", StorageRules.disposition("SENSITIVE"))
        assertEquals("REVIEW", StorageRules.disposition(StorageRules.classification(exactDuplicate = true)))
        assertEquals("REVIEW", StorageRules.disposition(StorageRules.classification(reconstructibleKanoTemporary = true)))
    }

    @Test fun oldOrLargeOrUnfamiliarNeverMakesContentDisposable() {
        val old = item("unfamiliar", 2 * StorageRules.GIGABYTE_BYTES).copy(addedAt = 1, modifiedAt = 1)
        assertEquals(listOf(old), StorageRules.largeFiles(listOf(old)))
        assertEquals("UNKNOWN", StorageRules.classification())
        assertEquals("KEEP", StorageRules.disposition("UNKNOWN"))
        val advice = StorageRules.recommendations(listOf(old), emptyList()).single()
        assertEquals("REVIEW", advice.action)
        assertTrue(advice.why.contains("does not mean unnecessary"))
    }

    @Test fun uniqueSizesNeedNoHashingAndUnknownSizesAreNotCandidates() {
        val rows = listOf(item("a", 1), item("b", 2), item("unknown", null), item("invalid", -1))
        assertTrue(StorageRules.candidateGroups(rows).isEmpty())
    }

    @Test fun zeroByteEntriesRemainMetadataWithoutDuplicateOrRecoverySuggestions() {
        val rows = listOf(item("empty-a", 0), item("empty-b", 0)).map { it.copy(location = "Download/") }
        val emptyHash = ContentHasher.hash(ByteArrayInputStream(byteArrayOf()), 0, 0) {} as HashResult.Complete
        val hashes = rows.map { StorageHashEvidence(it.id, emptyHash.sha256, "VERIFIED", it.revision) }
        assertEquals(StorageByteTotal(0, 2, 0), StorageRules.totals(rows))
        assertTrue(StorageRules.candidateGroups(rows).isEmpty())
        assertTrue(StorageRules.exactDuplicates(rows, hashes).isEmpty())
        // Persisted groups from an earlier scanner version must not resurrect empty-file advice.
        val oldGroup = StorageDuplicateGroup(emptyHash.sha256, rows, 0, 0)
        assertTrue(StorageRules.recommendations(rows, listOf(oldGroup)).isEmpty())
        assertEquals("UNKNOWN", StorageRules.classification())
        assertEquals("KEEP", StorageRules.disposition("UNKNOWN"))
    }

    @Test fun metadataSeparatesCandidatesWithoutPairwiseComparison() {
        val rows = listOf(item("a"), item("b"), item("c").copy(width = 200), item("pdf").copy(mime = "application/pdf"))
        assertEquals(listOf("a", "b"), StorageRules.candidateGroups(rows).single().items.map { it.id })
    }

    @Test fun missingDimensionsDoNotHidePossibleExactDuplicates() {
        val rows = listOf(item("a"), item("b").copy(width = 200), item("missing").copy(width = null))
        assertEquals(3, StorageRules.candidateGroups(rows).single().items.size)
    }

    @Test fun videoDurationParticipatesInCandidateGroupingWhenAvailable() {
        val first = item("a").copy(mime = "video/mp4", durationMillis = 1000)
        val same = first.copy(id = "b", canonicalId = "b")
        val different = first.copy(id = "c", canonicalId = "c", durationMillis = 2000)
        assertEquals(2, StorageRules.candidateGroups(listOf(first, same, different)).single().items.size)
        assertEquals(3, StorageRules.candidateGroups(listOf(first, same, different.copy(durationMillis = null))).single().items.size)
    }

    @Test fun duplicateUriOrCanonicalAliasesNeverCountAsMultipleCopies() {
        val alias = item("b").copy(canonicalId = "a")
        assertTrue(StorageRules.candidateGroups(listOf(item("a"), item("a"), alias)).isEmpty())
        assertTrue(StorageRules.candidateGroups(listOf(item("a"), item("a").copy(canonicalId = "conflicting-alias"))).isEmpty())
        assertTrue(StorageRules.exactDuplicates(listOf(item("a"), alias), listOf(hash("a"), hash("b"))).isEmpty())
        assertEquals(3L, StorageRules.totals(listOf(item("a"), alias)).knownBytes)
    }

    @Test fun exactContentGroupsUseRealStreamHashesAndOnlyPotentialRecovery() {
        val data = "abc".toByteArray()
        val digest = ContentHasher.hash(ByteArrayInputStream(data), 3, 3) {} as HashResult.Complete
        val rows = listOf(item("a"), item("b"), item("c"))
        val group = StorageRules.exactDuplicates(rows, rows.map { StorageHashEvidence(it.id, digest.sha256, "VERIFIED", it.revision) }).single()
        assertEquals(3, group.copies)
        assertEquals(2, group.additionalCopies)
        assertEquals(9L, group.totalBytes)
        assertEquals(6L, group.potentiallyRecoverableBytes)
        val recommendation = StorageRules.recommendations(rows, listOf(group)).single()
        assertEquals(6L, recommendation.sizeBytes)
        assertEquals("REVIEW", recommendation.action)
        assertTrue(recommendation.risk.contains("no file has been removed"))
    }

    @Test fun equalSizeIsNotProofOfDuplicateContent() {
        val rows = listOf(item("a"), item("b"))
        assertEquals(1, StorageRules.candidateGroups(rows).size)
        assertTrue(StorageRules.exactDuplicates(rows, listOf(hash("a", "abc"), hash("b", "xyz"))).isEmpty())
    }

    @Test fun staleMissingInvalidAndUnverifiedHashesCannotProduceExactGroups() {
        val rows = listOf(item("a"), item("b"))
        listOf(hash("b", revision = "old"), hash("b").copy(state = "ERROR"), hash("b").copy(sha256 = "fake"), hash("b").copy(revision = null)).forEach {
            assertTrue(StorageRules.exactDuplicates(rows, listOf(hash("a"), it)).isEmpty())
        }
        assertTrue(StorageRules.exactDuplicates(listOf(item("a"), item("b", revision = null)), listOf(hash("a"), hash("b"))).isEmpty())
        assertTrue(StorageRules.exactDuplicates(rows, listOf(hash("a"), hash("b"), hash("b", "xyz"))).isEmpty())
    }

    @Test fun revokedAccessExcludesEvidenceAndMetadataTotals() {
        val rows = listOf(item("a"), item("b").copy(currentAccess = false))
        assertTrue(StorageRules.exactDuplicates(rows, listOf(hash("a"), hash("b"))).isEmpty())
        assertTrue(StorageRules.candidateGroups(rows).isEmpty())
        assertEquals(3L, StorageRules.totals(rows).knownBytes)
    }

    @Test fun unresolvedPhysicalIdentityHasNoRecoverableByteClaim() {
        val rows = listOf(item("a"), item("b").copy(distinctFileIdentityConfirmed = false))
        val group = StorageRules.exactDuplicates(rows, listOf(hash("a"), hash("b"))).single()
        assertEquals(6L, group.totalBytes)
        assertNull(group.potentiallyRecoverableBytes)
    }

    @Test fun recommendationDropsPreviouslyVerifiedGroupAfterRevocationOrChange() {
        val original = listOf(item("a"), item("b"))
        val group = StorageRules.exactDuplicates(original, listOf(hash("a"), hash("b"))).single()
        assertTrue(StorageRules.recommendations(listOf(item("a"), item("b").copy(currentAccess = false)), listOf(group)).isEmpty())
        assertTrue(StorageRules.recommendations(listOf(item("a"), item("b", revision = "new")), listOf(group)).isEmpty())
    }

    @Test fun unknownSizesHaveExplicitCountsAndOverflowNeverWraps() {
        val total = StorageRules.totals(listOf(item("known", 0), item("missing", null), item("invalid", -1)))
        assertEquals(0L, total.knownBytes)
        assertEquals(1, total.knownItems)
        assertEquals(2, total.unknownItems)
        assertNull(StorageRules.totals(listOf(item("a", Long.MAX_VALUE), item("b", 1))).knownBytes)
        assertNull(StorageRules.sumBytes(listOf(-1)))
        val rows = listOf(item("a", Long.MAX_VALUE), item("b", Long.MAX_VALUE), item("c", Long.MAX_VALUE))
        val group = StorageRules.exactDuplicates(rows, rows.map { hash(it.id) }).single()
        assertNull(group.totalBytes)
        assertNull(group.potentiallyRecoverableBytes)
    }

    @Test fun thresholdBoundariesIncludeExactSizesAndRemainConfigurable() {
        val rows = listOf(item("less", StorageRules.LARGE_BYTES - 1), item("100", StorageRules.LARGE_BYTES),
            item("500", StorageRules.VERY_LARGE_BYTES), item("1gb", StorageRules.GIGABYTE_BYTES), item("unknown", null))
        assertEquals(listOf("1gb", "500", "100"), StorageRules.largeFiles(rows).map { it.id })
        assertEquals(listOf("1gb", "500"), StorageRules.largeFiles(rows, StorageRules.VERY_LARGE_BYTES).map { it.id })
    }

    @Test fun recommendationsDoNotInventTemporaryDataOrVolumeReadings() {
        assertTrue(StorageRules.recommendations(emptyList(), emptyList(), StorageVolume(null, 0)).isEmpty())
        assertTrue(StorageRules.recommendations(emptyList(), emptyList(), StorageVolume(100, 101), -1).isEmpty())
        val result = StorageRules.recommendations(emptyList(), emptyList(), StorageVolume(100, 10), 20)
        assertEquals(listOf("kano-temporary", "capacity"), result.map { it.id })
        assertTrue(result.all { it.action == "REVIEW" })
        assertTrue(result.first().why.contains("excludes other apps"))
    }

    @Test fun downloadsWithUnknownSizeHaveNoFabricatedTotal() {
        val result = StorageRules.recommendations(listOf(item("a").copy(location = "Download"), item("b", null).copy(location = "Download")), emptyList()).single()
        assertNull(result.sizeBytes)
        assertTrue(result.why.contains("Age does not imply low value"))
    }

    @Test fun metadataFixturesScaleTo1001000And10000WithoutHashingEveryItem() {
        for (size in listOf(100, 1_000, 10_000)) {
            val rows = (0 until size).map { index ->
                val bytes = if (index % 100 < 2) 1_000_000L + index / 100 else index.toLong() + 1
                item("$index", bytes).copy(name = "fixture-$index.jpg")
            }
            val started = System.nanoTime()
            val groups = StorageRules.candidateGroups(rows)
            val total = StorageRules.totals(rows)
            val categories = rows.groupingBy { StorageRules.category(it.name, it.mime, it.location) }.eachCount()
            val elapsed = (System.nanoTime() - started) / 1_000_000.0
            assertEquals(size / 100, groups.size)
            assertEquals(size / 50, groups.sumOf { it.items.size })
            assertEquals(size, total.knownItems)
            assertEquals(0, total.unknownItems)
            assertEquals(size, categories["PHOTOS"])
            assertTrue(groups.all { it.items.size == 2 })
            println("Storage metadata fixture: items=$size candidateItems=${groups.sumOf { it.items.size }} elapsedMs=$elapsed")
        }
    }

    @Test(expected = CancellationException::class) fun candidateStreamHashingRemainsCancellable() {
        var checks = 0
        ContentHasher.hash(ByteArrayInputStream(ByteArray(130_000)), 130_000, 130_000) {
            if (++checks == 2) throw CancellationException("fixture cancelled between chunks")
        }
    }
}
