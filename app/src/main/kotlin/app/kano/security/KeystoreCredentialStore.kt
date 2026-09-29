package app.kano.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import app.kano.core.CredentialRead
import app.kano.core.CredentialRemoval
import app.kano.core.CredentialSlot
import app.kano.core.CredentialStore
import app.kano.core.CredentialWrite
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** One instance per application graph. No plaintext files, logging, or network access. */
class KeystoreCredentialStore internal constructor(private val directory: File, private val keyAlias: String) : CredentialStore {
    constructor(context: Context) : this(File(context.noBackupFilesDir, "credentials"), "app.kano.credentials.v1")
    private val lock = Mutex()

    override suspend fun save(slot: CredentialSlot, secret: ByteArray): CredentialWrite {
        if (secret.isEmpty() || secret.size > MAX_SECRET_BYTES) return CredentialWrite.INVALID_INPUT
        val copy = secret.copyOf()
        return try {
            withContext(Dispatchers.IO) {
                lock.withLock {
                    try {
                        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Storage unavailable")
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        val encryptionKey = key(create = true) ?: return@withLock CredentialWrite.UNAVAILABLE
                        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)
                        if (cipher.iv.size != 12) throw GeneralSecurityException("Unsupported nonce size")
                        cipher.updateAAD(binding(slot))
                        val encrypted = cipher.doFinal(copy)
                        val encoded = ByteArrayOutputStream().also { buffer ->
                            DataOutputStream(buffer).use { output ->
                                output.writeInt(MAGIC)
                                output.writeByte(VERSION)
                                output.write(cipher.iv)
                                output.writeInt(encrypted.size)
                                output.write(encrypted)
                            }
                        }.toByteArray()
                        val file = atomicFile(slot)
                        val output = file.startWrite()
                        try {
                            output.write(encoded)
                            file.finishWrite(output)
                        } catch (failure: IOException) {
                            file.failWrite(output)
                            throw failure
                        }
                        CredentialWrite.SAVED
                    } catch (_: GeneralSecurityException) { CredentialWrite.UNAVAILABLE }
                    catch (_: ProviderException) { CredentialWrite.UNAVAILABLE }
                    catch (_: SecurityException) { CredentialWrite.UNAVAILABLE }
                    catch (_: IOException) { CredentialWrite.UNAVAILABLE }
                }
            }
        } finally { copy.fill(0) }
    }

    override suspend fun read(slot: CredentialSlot): CredentialRead = withContext(Dispatchers.IO) {
        lock.withLock {
            try {
                atomicFile(slot).openRead().use { stream ->
                    if (stream.channel.size() > MAX_SECRET_BYTES + 64) return@withLock CredentialRead.Corrupt
                    val input = DataInputStream(stream)
                    if (input.readInt() != MAGIC || input.readUnsignedByte() != VERSION) return@withLock CredentialRead.Corrupt
                    val nonce = ByteArray(12).also(input::readFully)
                    val length = input.readInt()
                    if (length !in 17..(MAX_SECRET_BYTES + 16)) return@withLock CredentialRead.Corrupt
                    val encrypted = ByteArray(length).also(input::readFully)
                    if (input.read() != -1) return@withLock CredentialRead.Corrupt
                    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                    val existingKey = key(create = false) ?: return@withLock CredentialRead.Unavailable
                    cipher.init(Cipher.DECRYPT_MODE, existingKey, GCMParameterSpec(128, nonce))
                    cipher.updateAAD(binding(slot))
                    CredentialRead.Found(cipher.doFinal(encrypted))
                }
            } catch (_: FileNotFoundException) { CredentialRead.Missing }
            catch (_: EOFException) { CredentialRead.Corrupt }
            catch (_: AEADBadTagException) { CredentialRead.Corrupt }
            catch (_: GeneralSecurityException) { CredentialRead.Unavailable }
            catch (_: ProviderException) { CredentialRead.Unavailable }
            catch (_: SecurityException) { CredentialRead.Unavailable }
            catch (_: IOException) { CredentialRead.Unavailable }
        }
    }

    override suspend fun remove(slot: CredentialSlot): CredentialRemoval = withContext(Dispatchers.IO) {
        lock.withLock {
            val file = atomicFile(slot)
            file.delete()
            if (file.baseFile.exists() || File(file.baseFile.path + ".bak").exists() || File(file.baseFile.path + ".new").exists()) {
                CredentialRemoval.UNAVAILABLE
            } else CredentialRemoval.REMOVED
        }
    }

    private fun atomicFile(slot: CredentialSlot) = AtomicFile(File(directory, "${slot.name}.bin"))
    private fun binding(slot: CredentialSlot) = "kano:credential:$VERSION:${slot.name}".toByteArray(Charsets.UTF_8)

    private fun key(create: Boolean): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = store.getKey(keyAlias, null)
        if (existing != null) return existing as? SecretKey ?: throw GeneralSecurityException("Unsupported key type")
        if (!create) return null // Do not replace a lost key while reading old data.
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(256).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
        }.generateKey()
    }

    companion object {
        const val MAX_SECRET_BYTES = 16 * 1024
        private const val MAGIC = 0x4B414E4F
        private const val VERSION = 1
    }
}
