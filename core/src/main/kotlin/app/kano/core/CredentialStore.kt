package app.kano.core

enum class CredentialSlot { OPENAI, GEMINI, PERPLEXITY }
enum class CredentialWrite { SAVED, INVALID_INPUT, UNAVAILABLE }
enum class CredentialRemoval { REMOVED, UNAVAILABLE }

sealed interface CredentialRead {
    /** Caller must clear bytes after use; this type deliberately has no data-class logging. */
    class Found(val bytes: ByteArray) : CredentialRead {
        override fun toString() = "CredentialRead.Found([redacted])"
    }
    data object Missing : CredentialRead
    data object Corrupt : CredentialRead
    data object Unavailable : CredentialRead
}

interface CredentialStore {
    suspend fun save(slot: CredentialSlot, secret: ByteArray): CredentialWrite
    suspend fun read(slot: CredentialSlot): CredentialRead
    suspend fun remove(slot: CredentialSlot): CredentialRemoval
}
