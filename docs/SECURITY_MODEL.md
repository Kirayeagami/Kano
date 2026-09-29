# Security model

No INTERNET permission, broad storage permission, notification listener or accessibility
service. The selected-document picker supplies only user-granted URI access.
Backup/transfer exclusions remain; they are not a promise against every device threat.

Room metadata and manual Care inventory use the app-private database directory.
They are NOT stored in noBackupFilesDir and are NOT separately database-encrypted.
Credential ciphertext alone uses noBackupFilesDir; encryption keys remain in Android
Keystore. AES-GCM authenticates the provider slot/version, with bounded atomic records,
random IVs and typed failures. Hardware backing depends on the device; no guarantee.

Text firewall heuristics reject unknown/sensitive data and require bound consent for
cloud use. They are not complete DLP. No live provider exists and no private content is
sent in this build. No OAuth tokens or personal credentials were accessed during recovery.
