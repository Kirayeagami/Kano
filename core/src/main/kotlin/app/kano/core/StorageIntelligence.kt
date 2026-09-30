package app.kano.core

import java.util.Locale

/** Metadata only. Unknown size/date and unconfirmed file identity are deliberately preserved. */
data class StorageMetadata(
    val id: String,
    val name: String,
    val canonicalId: String = id,
    val mime: String? = null,
    val location: String? = null,
    val sizeBytes: Long? = null,
    val addedAt: Long? = null,
    val modifiedAt: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
    val revision: String? = null,
    val currentAccess: Boolean = true,
    val distinctFileIdentityConfirmed: Boolean = false,
)

data class StorageHashEvidence(
    val id: String,
    val sha256: String?,
    val state: String,
    val revision: String?,
)

data class StorageCandidateKey(
    val sizeBytes: Long,
    val mime: String,
    val width: Int? = null,
    val height: Int? = null,
    val durationMillis: Long? = null,
)

data class StorageCandidateGroup(val key: StorageCandidateKey, val items: List<StorageMetadata>)

/** knownBytes is a subtotal, never a claim that unknown sizes are zero. Null means overflow. */
data class StorageByteTotal(val knownBytes: Long?, val knownItems: Int, val unknownItems: Int)

data class StorageDuplicateGroup(
    val sha256: String,
    val items: List<StorageMetadata>,
    val totalBytes: Long?,
    val potentiallyRecoverableBytes: Long?,
    val confidence: String = "VERIFIED_SHA256_AT_SCAN",
) {
    val copies: Int get() = items.size
    val additionalCopies: Int get() = (copies - 1).coerceAtLeast(0)
}

data class StorageRecommendation(
    val id: String,
    val what: String,
    val why: String,
    val evidence: List<String>,
    val sizeBytes: Long?,
    val confidence: String,
    val risk: String,
    val action: String = "REVIEW",
)

data class StorageVolume(val totalBytes: Long?, val availableBytes: Long?)

/** Local deterministic rules. This object never reads, opens, hashes, modifies or deletes a file. */
object StorageRules {
    const val LARGE_BYTES: Long = 100L * 1024 * 1024
    const val VERY_LARGE_BYTES: Long = 500L * 1024 * 1024
    const val GIGABYTE_BYTES: Long = 1024L * 1024 * 1024

    private val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif", "heic", "heif", "avif", "bmp", "dng", "tif", "tiff")
    private val videoExtensions = setOf("mp4", "mkv", "webm", "mov", "avi", "m4v", "3gp", "mpeg", "mpg")
    private val audioExtensions = setOf("mp3", "m4a", "aac", "wav", "flac", "ogg", "opus", "wma", "amr")
    private val documentExtensions = setOf("pdf", "txt", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt", "ods", "odp", "rtf", "csv", "epub")
    private val archiveExtensions = setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "tgz")
    private val archiveMimes = setOf("application/zip", "application/x-zip-compressed", "application/x-rar-compressed", "application/vnd.rar", "application/x-7z-compressed", "application/x-tar", "application/gzip", "application/x-gzip", "application/x-bzip2", "application/x-xz")
    private val documentMimes = setOf("application/pdf", "application/rtf", "application/msword", "application/vnd.ms-excel", "application/vnd.ms-powerpoint", "application/epub+zip", "application/postscript")
    private val sha256Pattern = Regex("[a-fA-F0-9]{64}")

    /** Exclusive categories. SCREENSHOTS / SCREEN_RECORDINGS mean path evidence, not vision proof. */
    fun category(name: String, mime: String?, location: String?): String {
        val type = normalizedMime(mime)
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        val fallback = type == "unknown" || type == "application/octet-stream"
        val base = when {
            type.startsWith("image/") -> "PHOTOS"
            type.startsWith("video/") -> "VIDEOS"
            type.startsWith("audio/") -> "AUDIO"
            type == "application/vnd.android.package-archive" -> "APKS"
            type in archiveMimes -> "ARCHIVES"
            type.startsWith("text/") || type in documentMimes ||
                type.startsWith("application/vnd.openxmlformats-officedocument.") ||
                type.startsWith("application/vnd.oasis.opendocument.") -> "DOCUMENTS"
            !fallback -> "OTHER"
            extension in imageExtensions -> "PHOTOS"
            extension in videoExtensions -> "VIDEOS"
            extension in audioExtensions -> "AUDIO"
            extension == "apk" -> "APKS"
            extension in archiveExtensions -> "ARCHIVES"
            extension in documentExtensions -> "DOCUMENTS"
            extension in setOf("exe", "msi", "dmg", "iso") -> "OTHER"
            else -> "UNKNOWN"
        }
        val folders = folders(location)
        return when {
            base == "PHOTOS" && folders.any { it == "screenshots" || it == "screenshot" } -> "SCREENSHOTS"
            base == "VIDEOS" && folders.any { it in setOf("screenrecord", "screenrecords", "screenrecorder", "screen recordings", "screenrecordings", "screen recording") } -> "SCREEN_RECORDINGS"
            else -> base
        }
    }

    fun isDownload(location: String?): Boolean = folders(location).any { it == "download" || it == "downloads" }

    /** No date, size, filename or age rule can label personal content disposable. */
    fun classification(
        sensitivity: String = "UNKNOWN",
        exactDuplicate: Boolean = false,
        reconstructibleKanoTemporary: Boolean = false,
        explicitlyUseful: Boolean = false,
        reviewSuggested: Boolean = false,
    ): String = when {
        sensitivity.uppercase(Locale.ROOT) in setOf("PERSONAL", "FINANCIAL", "SECRET", "SENSITIVE") -> "SENSITIVE"
        exactDuplicate -> "DUPLICATE"
        reconstructibleKanoTemporary -> "TEMPORARY"
        explicitlyUseful -> "USEFUL"
        reviewSuggested -> "REVIEW"
        else -> "UNKNOWN"
    }

    /** Phase 2 has no destructive disposition, including for duplicate or temporary evidence. */
    fun disposition(classification: String): String = when (classification) {
        "REVIEW", "LOW_VALUE", "DUPLICATE", "TEMPORARY" -> "REVIEW"
        else -> "KEEP"
    }

    /** O(n) grouping; incomplete dimensions must not exclude an otherwise possible exact match. */
    fun candidateGroups(items: List<StorageMetadata>): List<StorageCandidateGroup> {
        val valid = canonicalItems(items).filter { it.currentAccess && it.sizeBytes != null && it.sizeBytes > 0 }
        return valid.groupBy { StorageCandidateKey(it.sizeBytes!!, normalizedMime(it.mime)) }
            .flatMap { (coarse, rows) ->
                if (rows.size < 2) emptyList()
                else if (rows.any { !hasCompleteShape(it) }) listOf(StorageCandidateGroup(coarse, rows))
                else rows.groupBy { shapeKey(coarse, it) }.filterValues { it.size > 1 }
                    .map { (key, matches) -> StorageCandidateGroup(key, matches) }
            }
    }

    /** A cached hash counts only when the source is accessible and its nonempty revision matches. */
    fun exactDuplicates(items: List<StorageMetadata>, hashes: List<StorageHashEvidence>): List<StorageDuplicateGroup> {
        val evidence = hashes.groupBy { it.id }
        val verified = canonicalItems(items).mapNotNull { item ->
            if (!item.currentAccess || item.sizeBytes == null || item.sizeBytes <= 0 || item.revision.isNullOrBlank()) return@mapNotNull null
            val matching = evidence[item.id].orEmpty().filter {
                it.state == "VERIFIED" && it.revision == item.revision && it.sha256?.matches(sha256Pattern) == true
            }.map { it.sha256!!.lowercase(Locale.ROOT) }.distinct()
            if (matching.size != 1) null else item to matching.single()
        }
        return verified.groupBy { it.second to it.first.sizeBytes!! }.filterValues { it.size > 1 }
            .map { (key, matches) ->
                val rows = matches.map { it.first }
                StorageDuplicateGroup(key.first, rows,
                    multiplyBytes(key.second, rows.size.toLong()),
                    if (rows.all { it.distinctFileIdentityConfirmed }) multiplyBytes(key.second, rows.size.toLong() - 1) else null)
            }
    }

    fun totals(items: List<StorageMetadata>): StorageByteTotal {
        val rows = canonicalItems(items).filter { it.currentAccess }
        val known = rows.mapNotNull { it.sizeBytes?.takeIf { bytes -> bytes >= 0 } }
        return StorageByteTotal(sumBytes(known), known.size, rows.size - known.size)
    }

    fun largeFiles(items: List<StorageMetadata>, thresholdBytes: Long = LARGE_BYTES): List<StorageMetadata> {
        require(thresholdBytes >= 0)
        return canonicalItems(items).filter { it.currentAccess && (it.sizeBytes ?: -1) >= thresholdBytes }
            .sortedWith(compareByDescending<StorageMetadata> { it.sizeBytes }.thenBy { it.id })
    }

    /** Sizes refer to indexed authorized data. Duplicate totals may overlap other review facets. */
    fun recommendations(
        items: List<StorageMetadata>,
        duplicates: List<StorageDuplicateGroup>,
        volume: StorageVolume? = null,
        measuredKanoTemporaryBytes: Long? = null,
    ): List<StorageRecommendation> = buildList {
        val rows = canonicalItems(items).filter { it.currentAccess && it.sizeBytes != 0L }
        val currentById = rows.associateBy { it.id }
        duplicates.forEach { group ->
            val currentCopies = canonicalItems(group.items.mapNotNull { original ->
                currentById[original.id]?.takeIf { it.revision == original.revision && it.sizeBytes == original.sizeBytes }
            })
            if (currentCopies.size < 2 || !group.sha256.matches(sha256Pattern)) return@forEach
            val bytes = currentCopies.first().sizeBytes?.takeIf { it > 0 } ?: return@forEach
            if (currentCopies.any { it.sizeBytes != bytes || it.revision.isNullOrBlank() }) return@forEach
            val potential = if (currentCopies.all { it.distinctFileIdentityConfirmed }) multiplyBytes(bytes, currentCopies.size.toLong() - 1) else null
            add(StorageRecommendation("duplicate-${group.sha256}", "${currentCopies.size} entries with exact matching content",
                "Distinct authorized entries had equal complete SHA-256 hashes at their recorded revisions.",
                currentCopies.map { it.id }, potential, group.confidence,
                if (potential == null) "File identity or byte total is not fully measurable. Keep sources until reviewed."
                else "Potentially recoverable only; no file has been removed. Keep at least one verified copy."))
        }
        val large = largeFiles(rows)
        if (large.isNotEmpty()) add(StorageRecommendation("large", "${large.size} large files",
            "Each indexed file is at least 100 MiB. Large does not mean unnecessary.", large.map { it.id },
            totals(large).knownBytes, "METADATA", "User content; review purpose before any future action."))
        val downloads = rows.filter { isDownload(it.location) }
        if (downloads.isNotEmpty()) add(StorageRecommendation("downloads", "${downloads.size} accessible downloads",
            "Indexed metadata places these files in a Download or Downloads folder. Age does not imply low value.",
            downloads.map { it.id }, totals(downloads).let { if (it.unknownItems == 0) it.knownBytes else null },
            "METADATA", "Download files can contain useful or sensitive information."))
        if (measuredKanoTemporaryBytes != null && measuredKanoTemporaryBytes > 0) add(StorageRecommendation("kano-temporary", "Kano temporary data",
            "Measured reconstructible Kano cache data. This excludes other apps' private caches.",
            listOf("KANO_RECONSTRUCTIBLE_CACHE"), measuredKanoTemporaryBytes, "MEASURED", "Review only in Phase 2; no cache was cleared."))
        val total = volume?.totalBytes
        val available = volume?.availableBytes
        if (total != null && available != null && total > 0 && available >= 0 && available <= total && available.toDouble() / total.toDouble() <= 0.10) {
            add(StorageRecommendation("capacity", "Storage availability is low", "The measured data volume has 10% or less available space.",
                listOf("TOTAL=$total", "AVAILABLE=$available"), available, "ANDROID_API", "A volume reading does not identify which user files are unnecessary."))
        }
    }

    fun sumBytes(values: Iterable<Long>): Long? {
        var total = 0L
        for (value in values) {
            if (value < 0) return null
            total = try { Math.addExact(total, value) } catch (_: ArithmeticException) { return null }
        }
        return total
    }

    private fun multiplyBytes(bytes: Long, copies: Long): Long? =
        try { Math.multiplyExact(bytes, copies) } catch (_: ArithmeticException) { null }

    private fun canonicalItems(items: List<StorageMetadata>): List<StorageMetadata> =
        items.filter { it.id.isNotBlank() }.distinctBy { it.id }.distinctBy { it.canonicalId.ifBlank { it.id } }

    private fun folders(location: String?): List<String> = location.orEmpty().replace('\\', '/').split('/')
        .map { it.trim().lowercase(Locale.ROOT) }.filter { it.isNotBlank() }

    private fun normalizedMime(mime: String?): String = mime?.substringBefore(';')?.trim()?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() } ?: "unknown"

    private fun hasCompleteShape(item: StorageMetadata): Boolean = when {
        normalizedMime(item.mime).startsWith("image/") -> item.width != null && item.width > 0 && item.height != null && item.height > 0
        normalizedMime(item.mime).startsWith("video/") -> item.width != null && item.width > 0 && item.height != null && item.height > 0 && item.durationMillis != null && item.durationMillis > 0
        normalizedMime(item.mime).startsWith("audio/") -> item.durationMillis != null && item.durationMillis > 0
        else -> true
    }

    private fun shapeKey(base: StorageCandidateKey, item: StorageMetadata): StorageCandidateKey = when {
        base.mime.startsWith("image/") -> base.copy(width = item.width, height = item.height)
        base.mime.startsWith("video/") -> base.copy(width = item.width, height = item.height, durationMillis = item.durationMillis)
        base.mime.startsWith("audio/") -> base.copy(durationMillis = item.durationMillis)
        else -> base
    }
}
