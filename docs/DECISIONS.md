# KANO — Architectural Decision Log (ADR)

---

## ADR-001: Two-Module Gradle Architecture
- **Date**: 2026-09-29
- **Decision**: Split codebase into `:core` (pure Kotlin, Android-free) and `:app` (Android composition, UI, platform adapters).
- **Reason**: Guarantees core policy, privacy rules, and AI contracts can be tested on host JVM without Android dependencies.
- **Alternatives**: Single module (messy boundaries), or multi-module per feature (overkill for initial slice).
- **Consequences**: Strict separation enforced by Gradle build dependencies.

---

## ADR-002: Android Keystore for Credential Storage
- **Date**: 2026-09-29
- **Decision**: Use `KeystoreCredentialStore` with AES-256-GCM and `noBackupFilesDir`.
- **Reason**: Prevents raw credentials from persisting in plaintext or leaking via system backups.
- **Alternatives**: EncryptedSharedPreferences (deprecated/bugs), plain Room DB (unencrypted).

---

## ADR-003: Selected Media via SAF with Persisted Read Grants
- **Date**: 2026-09-29
- **Decision**: Use `ACTION_OPEN_DOCUMENT` and `takePersistableUriPermission`.
- **Reason**: Avoids requesting broad `READ_EXTERNAL_STORAGE` or `READ_MEDIA_*` permissions, aligning with privacy-first principle.
- **Consequences**: User explicitly selects files; max 100 documents initially.

## Recovery decisions
Prioritize truthful behavior over decorative completion claims. Implement bounded manual Care storage because the exposed inventory was fabricated. Defer Style analysis and cloud integrations until working adapters exist. Use migration 1→2, never destructive fallback. Match IDE/CLI debug signing via the current user's standard debug keystore; no keystore is committed. Theme colors are scoped to composition; opaque dark cards and reduced ambient motion take precedence over decoration.
