# KANO — Test & Verification Status Record

Updated: 2026-09-29

---

## 1. Test Suite Summary

Total Tests: **44 Passed** (0 Failures, 0 Skipped)

### Core Unit Tests (`:core:test`)
- `PrivacyFirewallTest` (8 tests): Text firewall, sensitive patterns, consent verification.
- `AiRouterTest` (4 tests): Provider fallback and capability routing.
- `CleanupPolicyTest` (5 tests): Extract-before-delete prerequisites.
- `AvailabilityTest` (1 test): System availability state transitions.
- `ContentHasherTest` (7 tests): Streaming SHA-256 calculation and limits.

### Android Host Unit Tests (`:app:testDebugUnitTest`)
- `MediaSearchTest` (1 test): SQLite wildcard escaping for filename search.

### Instrumented Device Tests (`:app:connectedDebugAndroidTest`)
- `KnowledgeVaultTest` (2 tests): Room Schema 3 Knowledge Vault entity saving, retrieval, and deletion.
- `CareRepositoryTest` (Room Schema 1->2 migration & `CareRepository` database persistence, capacity, stale edit)
- `CareUiTest` (Personal Care UI interactions & activity recreation)
- `AppearanceTest` & `AppearanceUiTest` (Theme switching, persistence, and UI modes)
- `MediaStateUiTest` (Media index UI states & empty search UX)
- `DatabaseTest`: Room DAO idempotency and query filtering.
- `NavigationTest`: Compose bottom navigation tab switching.
- `CredentialStoreTest` (6 tests): Keystore encryption roundtrip, no plaintext on disk, redaction, tamper detection, slot binding, missing keys.

---

## 2. Automated Build Script

Script: `scripts/check.ps1`
Command: `powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1`
Validates: `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, merged manifest permissions, backup rules.
Status: **PASSING** (91 actionable tasks executed successfully, 18/18 connected instrumented tests passed, 0 lint errors).
