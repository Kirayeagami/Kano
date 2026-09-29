# KANO — Test & UI Verification Status Record

Updated: 2026-09-29

---

## 1. Test Suite Summary

Total Tests: **34 Passed** (0 Failures, 0 Skipped)

### Core Unit Tests (`:core:test`)
- `PrivacyFirewallTest` (8 tests): Text firewall, sensitive patterns, consent verification.
- `AiRouterTest` (4 tests): Provider fallback and capability routing.
- `CleanupPolicyTest` (5 tests): Extract-before-delete prerequisites.
- `AvailabilityTest` (1 test): System availability state transitions.
- `ContentHasherTest` (7 tests): Streaming SHA-256 calculation and limits.

### Android Host Unit Tests (`:app:testDebugUnitTest`)
- `MediaSearchTest` (1 test): SQLite wildcard escaping for filename search.

### Instrumented Device Tests (`:app:connectedDebugAndroidTest`)
- `DatabaseTest` (1 test): Room DAO idempotency and query filtering.
- `NavigationTest` (1 test): Compose bottom navigation tab switching.
- `CredentialStoreTest` (6 tests): Keystore encryption roundtrip, no plaintext on disk, redaction, tamper detection, slot binding, missing keys.

---

## 2. Compose Previews & UI Verification

The following Compose Previews are implemented in `app/src/main/kotlin/app/kano/ui/UiPreviews.kt`:
- `@Preview HomeScreenPreview`: Editorial Home Dashboard preview.
- `@Preview DeviceScreenPreview`: Technical Device Dashboard preview.
- `@Preview StyleScreenPreview`: Style Studio, Outfit evaluation, Wardrobe & Shopping gaps preview.
- `@Preview PersonalCareScreenPreview`: Personal Care inventory & Anti-Overspending check preview.
- `@Preview SettingsScreenPreview`: Privacy disclosures & release roadmap preview.

---

## 3. Automated Build Script

Script: `scripts/check.ps1`
Command: `powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1`
Validates: `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, merged manifest permissions, backup rules.
Status: **PASSING** (91 actionable tasks executed successfully, 0 lint errors).
