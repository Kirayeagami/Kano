# Product map

The seven product groups below are stable product boundaries, not seven mandatory
Gradle modules. The architecture diagram's orchestration layer serves features that
need reasoning; it does not route every local operation through a model or web service.

| Group | Feature inventory | Current implementation / next dependency |
|---|---|---|
| 01 Foundation | Android architecture, design system, navigation, database, permissions, security, Privacy Firewall, AI abstraction, background jobs | Initial native implementation and domain policies. Secure credential store, sensitive vault encryption, full UI/runtime verification remain gates. |
| 02 Device | Storage, Apps, Performance, Device health | Storage-volume, available system memory, battery, device/OS facts. App inventory, usage-based evidence, thermal state and detailed health review follow capability validation. |
| 03 Media | Photos, Screenshots, Videos, OCR, URLs, QR, Movies, Books, Music, Study, Extract-before-delete | Selected image/video metadata and hashes. OCR/QR → reviewed entities → vault → extraction receipt → explicit cleanup flow; screenshot detection is not assumed from extension. |
| 04 Personal Intelligence | Knowledge Vault, Universal Search, Notifications, Preferences, Daily Brief | Filename search only. Vault/provenance, source adapters, encrypted retention, user-editable preferences and authorized notifications precede cross-source search/briefs. |
| 05 AI | Local, OpenAI, Gemini, Perplexity, AI Router | Router interfaces and fail-closed text policy tested. No inference engine or remote adapter installed. Providers require credentials/model assets, capability checks and provider contract tests. |
| 06 Life | Money, Shopping, Style, Wardrobe, Study, Skills, Daily Care | Planned. Explicit records and authorized provider sources; depends on vault, privacy boundary, preferences and per-domain evidence. No transactions or invented commitments. |
| 07 Advanced | Cross-domain reasoning, Automation, Advanced integrations, Long-term personalization | Planned. Depends on stable source permissions, provenance, reversible actions, auditable confirmations, data export/forget and migration support. |

## Feature specification template
Every promoted roadmap item must specify: purpose; user outcome; data inputs and output;
permissions; source provenance; privacy and security impact; Android limitations;
dependencies; data model/migration; implementation complexity; loading/empty/denied/error/
offline states; cancellation/recovery; retention/revocation; tests; rollout/deprecation.

## Current vertical slice
Outcome: understand current device capacity and inspect a local index of selected media.
Inputs: OS readings and explicit document URIs. Outputs: factual readings, filenames,
sizes, processing states, local fingerprints. Access: persisted read grants only.
Complexity: moderate, highest risk at third-party provider I/O and cancellation boundaries.
Unavailable signals remain unavailable; no inferred meaningful categories are stored.
Tests: policy, content bounds, filename escaping, DAO idempotency, UI navigation, plus
the manual device protocol. Search is literal filename matching, not universal/AI search.

## Update contracts
App version 0.1.0-dev / code 1; database schema 1; feature/provider versions are introduced
with versioned external contracts, not arbitrary counters. The bundled roadmap records
planned releases, not available updates. A future update adapter must provide verified
current/available versions, date, notes, security changes, migration requirements,
deprecations and limitations; it must preserve user data and offer supported store flows.
Remote flags may disable integrations but may never grant permission or cloud consent.
