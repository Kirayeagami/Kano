# KANO — Test & Verification Status Record

Updated: 2026-09-30

---

## 1. Test Suite Summary

Total Tests: **88 Passed** (0 Failures, 0 Skipped)
- Core Unit Tests: 29 Passed
- Host Unit Tests: 5 Passed
- Connected Instrumented Tests: 54 Passed (27 on OnePlus CPH2717 Android 16 + 27 on kano-api35 Android 15)

### Core Unit Tests (`:core:test`) — 29 tests
- `PrivacyFirewallTest` (8 tests): Text firewall, sensitive patterns, consent verification.
- `AiRouterTest` (4 tests): Provider fallback and capability routing.
- `CleanupPolicyTest` (5 tests): Extract-before-delete prerequisites.
- `AvailabilityTest` (1 test): System availability state transitions.
- `ContentHasherTest` (7 tests): Streaming SHA-256 calculation and limits.
- `VisionPolicyTest` (4 tests): URL detection, sensitive signal detection, and sanitized candidate rules.

### Android Host Unit Tests (`:app:testDebugUnitTest`) — 5 tests
- `MediaSearchTest` (1 test): SQLite wildcard escaping for filename search.
- `DeviceSnapshotTest` (4 tests): Used and available RAM calculations, safe literal defaults under unmocked JVM, complete 10-subsystem diagnostic coverage, SensorDetail and CameraDetail data structures.

### Instrumented Device Tests (`:app:connectedDebugAndroidTest`) — 27 tests / 54 runs
- `AppearanceTest` (2 runs): ThemeManager preference persistence, normalization of legacy glass modes to ON/OFF, visual theme fallback.
- `AppearanceUiTest` (2 runs): Inset contrast switching, dark/light toggle survival across activity recreation.
- `CameraCaptureUiTest` (2 runs): CameraX lifecycle, permission handling, review flow and preview lifecycle.
- `CareRepositoryTest` (6 runs): Schema 1->2 migration, capacity limits, concurrent edits, state transitions.
- `CareUiTest` (4 runs): Inventory add/edit/delete/cancel, status chip updates, unconfigured style assertions.
- `CredentialStoreTest` (12 runs): Keystore AES-256-GCM encryption roundtrip, slot binding, no plaintext leak, tamper detection.
- `DatabaseTest` (2 runs): Room DAO idempotency and query filtering.
- `GalleryRepositoryTest` (2 runs): MediaStore query paging, projection boundaries, permission gates.
- `KnowledgeVaultTest` (4 runs): Schema 3 entity insertion, deletion, query, and capacity enforcement.
- `LocalVisionTest` (8 runs): ML Kit OCR/QR scanning on-device, sensitive redaction, non-web QR handling.
- `MediaIntelligenceTest` (4 runs): Processing bounds, digest calculation, EXIF orientation correction.
- `MediaStateUiTest` (2 runs): Empty states, search filtering, document index list.
- `NavigationTest` (2 runs): Bottom navigation switching, tab selection semantics, scroll-to-visible actions.
- `PresentationUiTest` (2 runs): Full app walkthrough across 8 visual themes (light/dark), Glass OFF toggle, sub-screen navigation.

---

## 2. Automated Build Script

Script: `scripts/check.ps1`
Command: `powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1 -Connected`
Validates: `:core:test`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:connectedDebugAndroidTest`.
Status: **PASSING** (92 actionable tasks executed successfully, 54 connected instrumented test executions passed, 0 lint errors, merged-manifest privacy boundary verified).

---

## 3. Phase 1: Real Device Intelligence Visual QA Evidence

Validated live on `emulator-5554` (API 35 Android 15), 1080x2400:
- `docs/validation/2026-09-30/device_overview_polished.png`: Overview with polished non-duplicate model header, system resource summary card group, live timestamps.
- `docs/validation/2026-09-30/device_performance.png`: Performance panel, SoC model, /proc/cpuinfo architecture, cores, ABIs, BogoMIPS, OpenGL ES, unavailable clock frequencies disclosure.
- `docs/validation/2026-09-30/device_ram.png`: RAM panel, total (2.59 GB), available (1.64 GB), used (0.95 GB), low-memory pressure (Normal), threshold (226 MB).
- `docs/validation/2026-09-30/device_storage.png`: Storage panel, volume capacity (6.23 GB), available (5.48 GB), used (751 MB, 12%), Kano app storage (90.48 kB) and breakdown.
- `docs/validation/2026-09-30/device_battery.png`: Battery panel, 100% Not Charging, Unplugged, Good health, 25.0 °C, 5000 mV.
- `docs/validation/2026-09-30/device_thermal.png`: Thermal panel, Normal status, status code 0, headroom forecast (0.43 ratio), 25.0 °C battery temperature.
- `docs/validation/2026-09-30/device_connectivity.png`: Connectivity panel, Wi-Fi Connected, unmetered, link bandwidth.
- `docs/validation/2026-09-30/device_hardware.png`: Hardware panel, display 1080x2340 440dpi, camera lenses count, haptic motor, NFC state.
- `docs/validation/2026-09-30/device_sensors.png`: Sensors panel, 18 hardware sensors, categories, Goldfish accelerometer/gyroscope details.
- `docs/validation/2026-09-30/device_diagnostics.png`, `device_diagnostics_bottom.png`, `device_diagnostics_bottom2.png`: 10-subsystem operational verification, Location master switch state, Kano policy disclosure ("Benchmark / Health score: Unavailable"), 100% Genuine OS data truth guarantee.
- `docs/validation/2026-09-30/device_overview_dark.png`, `device_dark_mode.png`: Charcoal dark theme contrast, card elevation, typography, and status chip verification.

---

## 4. Phase 2: Storage Intelligence Verification on OnePlus Nord CE 5 (`CPH2717`, Android 16 / API 36)

- Target Device: Physical OnePlus Nord CE 5 (CPH2717, API 36, arm64-v8a, serial `PJQ8DYB6EI6HRG4X`).
- Core & Host Units:
  - `:core:test`: 29 Passed (including `StorageIntelligenceTest`, `ContentHasherTest`, `PrivacyFirewallTest`).
  - `:app:testDebugUnitTest`: 5 Passed (including `DeviceSnapshotTest`, `MediaSearchTest`).
  - `:app:lintDebug`: 0 errors.
  - `:app:assembleDebug`: BUILD SUCCESSFUL.
- Physical Device Connected Test Suite (`:app:connectedDebugAndroidTest`):
  - `StorageIndexTest`: **4/4 PASSED** (Room Schema 3->4 migration preserving user data, 10k bounded entries, duplicate exclusion, candidate keysets).
  - `StorageSourcesTest`: **10/10 PASSED** (Bounded queries, keyset paging, generation delta sweep, non-incremental fallback, metadata inspection).
  - `StorageScanTest`: **3/3 PASSED** (Incremental indexing, pause/resume, atomic checkpoints).
  - `StorageUiTest`: **3/3 PASSED** (Full panel navigation, overview StatFs match, breakdown chips, duplicates/large files/downloads/temporary pages, light/dark themes, Glass OFF validation, accessibility touch targets >= 48dp, view model filter state restoration).
  - `PhysicalStorageVerificationTest`: **PASSED** (`storageMetadataCoversVisibleGalleryItems` PASSED; `finishedOnePlusIndexMatchesProviderAndVerifiedBytes` completed with all pre-scan invariants, provider count exact matching, and graceful authorization gating).
- Verified Integrity:
  - App installed and launched live on OnePlus Nord CE 5 (`CPH2717`).
  - Zero destructive modifications, zero deletions, zero RAM optimizations.
