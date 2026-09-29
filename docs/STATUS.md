# KANO status — 2026-09-29

## DONE — implemented foundation checkpoint
- Independent native Android project at E:\Works\Kano; unrelated Kolpo projects untouched.
- Engineering constitution, architecture/reconnaissance, risk register, permissions/privacy
  matrix, dependency map, seven-group feature catalog, and versioned 1.0–3.0 roadmap.
- Kotlin/Compose/Navigation shell, shared paper/wine theme, accessible labelled actions,
  API-26-compatible resources and adaptive launcher icon.
- Real storage-volume, memory, battery and device/OS readings with refresh and failure state.
- Room schema 1 exported; local selected-document metadata with persistent read grants.
- User-initiated WorkManager indexing; bounded streaming SHA-256; explicit unsupported,
  revoked, changed, partial and unavailable states; cancellation and explicit rescan.
- Literal filename search, paging and confirmed index forgetting/grant release. No file deletion.
- Newly acquired URI grants are released when their database insert fails.
- Android-free AI provider/router contracts, fail-closed text firewall, request-bound consent
  checks, availability policy, extract-before-delete prerequisite policy and bounded hasher.
- Full source briefs preserved in docs/briefs. Git initialized on main; no commit/push made.

## Validation evidence
- 25 core tests passed: privacy (8), router (4), cleanup (5), availability (1), hashing (7).
- 1 Android host unit test passed: literal wildcard escaping for filename search.
- 8 Android API-35 instrumented tests passed: Room idempotency/literal search, Compose navigation,
  and 6 credential-storage tests. Latest connected run used 200% text size: 34 total tests passed.
- Device testing found a Kotlin-escaped SQLite ESCAPE clause; changed it to a raw SQL
  string and reran the full host checks plus connected tests successfully.
- Debug APK and instrumentation APK built successfully.
- Android lint passed with **0 errors**. Dependency-update advisories remain for the deliberately
  pinned baseline. Launcher-icon advisories were addressed in the final resource pass.
- Reviewed merged manifest: no Internet, broad media/storage, blanket package visibility or
  accessibility service; backup disabled. WorkManager adds normal lifecycle/job permissions.
- Initial lint found an API-27 navigation-bar attribute in API-26 resources; removed and rechecked.
- Gradle 8.11.1 official SHA-256 matched the downloaded archive. Java's network bootstrap
  timed out following the distribution redirect; the verified archive seeded the local wrapper
  cache. Normal Gradle/Maven resolution and native compilation succeeded.

## Runtime and visual verification
An isolated API-35 emulator under E:\Works\tools\kano booted successfully with WHPX.
Android's acceleration diagnostic, unlike the generic Windows CPU report, correctly found
the usable backend. A previous software-only attempt did not boot and was stopped.
Actual 375 dp screens were inspected. Large-text navigation passed after correcting the
test to scroll lazy content into view. A 68-byte synthetic image was selected through
Android's document picker, indexed, and forgotten; its original remained intact and the
persisted grant was released. Final previews are in docs/validation/*-final.png.
TalkBack, API 26, physical devices, revocation and cancellation/forget races remain to
be verified. This checkpoint is not production-ready; the full definition of done is open.

## NEXT
1. Complete the remaining device/accessibility/failure matrix in VALIDATION.md.
2. Add on-device OCR/QR with bounded image decode and malformed-input tests.
3. Implement a provenance-aware knowledge vault with sensitive-storage/retention review,
   user edits, export/forget, structured URLs/entities and migration tests.
4. Add duplicate review and only then implement confirmed extract-before-delete execution.

## TECH DEBT / known limitations
- Development version 0.1.0-dev is not Kano 1.0. No cloud or local model is integrated.
- No full-library scan, OCR, visual semantics, URL/QR recognition, vault, broad universal
  search, apps/usage inventory, notification/email, Life, or Advanced features yet.
- Metadata-only database has no extra encryption layer. An Android Keystore AES-GCM
  credential adapter now has round-trip, tamper, provider-binding, missing-key, truncation,
  input/removal and random-nonce coverage. No real credentials or provider entry UI are connected.
- 100-document selection and 100 MiB hashing limits are explicit initial resource budgets.
- A document provider may block inside a read or fetch remote content; cancellation is
  cooperative between reads. Harden provider timeouts/isolation before broad library rollout.
- Same-size changes during hashing are not fully detected. Hashes are evidence only;
  no deduplication/deletion decision is made. A future cleanup executor must revalidate bytes.
- Secret matching is heuristic; unknown/sensitive content remains blocked. A future provider
  must add consent issuance/revocation/replay enforcement and a complete reviewed data path.
- English UI copy is currently source-local; extract localized strings before translation.
- Search and page state survive configuration via ViewModel, not full process recreation.
- Schema 1 needs no migration yet; every future schema change requires upgrade tests.
- Dependency/security audit, current store target-policy review, release signing and distribution
  are release prerequisites. The update screen is a bundled roadmap, not a live update feed.
