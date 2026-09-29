# KANO — Security & Privacy Architecture

---

## 1. Local-First Boundary

- **Manifest Boundaries**: `AndroidManifest.xml` contains **zero `INTERNET` permission**.
- **Backup & Extraction**: `android:allowBackup="false"` and data extraction rules explicitly disable cloud backup or ADB device-to-device transfers.
- **App Storage**: Database and encrypted keys reside exclusively within private app sandbox directories (`context.noBackupFilesDir`).

---

## 2. Hardware Keystore Encryption

- **Algorithm**: AES-256-GCM (`AES/GCM/NoPadding`).
- **Key Store**: Android Keystore with `KeyGenParameterSpec` (`PURPOSE_ENCRYPT` \| `PURPOSE_DECRYPT`).
- **Authenticated Additional Data (AAD)**: Each secret payload is bound to `kano:credential:1:<slot.name>` to prevent cross-slot replacement or tampering.
- **Nonce Integrity**: Random 12-byte initialization vectors generated per encryption operation.
- **File Writes**: Handled atomically using `AtomicFile` to prevent partial or corrupted writes.

---

## 3. Privacy Firewall

- **Fail-Closed Verification**: Data is assumed sensitive until evaluated by `PrivacyFirewall`.
- **Sensitive Detection**: Automatically flags passwords, tokens, API keys, financial data, and personal IDs.
- **Consent Contract**: Every external AI or network request requires unexpired, request-bound user consent specifying payload hash, provider ID, purpose, and capability.
