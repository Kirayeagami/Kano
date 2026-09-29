# Implementation plan and feature dependencies

## NOW — foundation checkpoint
1. Reconnaissance, architecture, constitution, risk register, privacy matrix.
2. Reproducible native Gradle project and Android-free core policy tests.
3. Shared Compose theme, navigation, lifecycle state and truthful availability.
4. Real local device snapshot with metric scope and unavailable states.
5. Room metadata index + persisted document grants + bounded WorkManager hashing.
6. Index search/paging, refresh, cancellation, revocation recovery, forget confirmation.
7. Compile, unit tests, lint, instrumented tests if a device is available; record limits.
8. Add a local credential-storage adapter: AES-GCM with a non-exportable Android Keystore
   key; provider-bound authenticated data; atomic no-backup files; bounded reads; explicit
   missing/corrupt/unavailable outcomes; synthetic round-trip, tamper and deletion tests.
   Do not expose credential entry or cloud integration until the provider/consent UI exists.

## Release roadmap
| Release / status | Purpose and benefit | Data / access | Dependencies | Privacy, complexity, platform limits / tests |
|---|---|---|---|---|
| 1.0 / NOW | Understand Device + Media and review cleanup | Selected media, later scoped library; optional usage access | Foundation → metadata → OCR/QR/URLs → vault → duplicates → extract-before-delete | High; revocation, bounded parsing, extraction durability, OS delete confirmation, OEM/API/large-library tests |
| 1.1 / NEXT | Gmail + notification intelligence | OAuth Gmail scopes; explicit notification special access | Retention, encrypted sensitive storage, classifier, digest | High; no private DB scraping; token revocation, OTP filtering, account isolation, OAuth review |
| 1.2 / PLANNED | Style Lab and wardrobe | User-chosen clothing photos and preferences | Image pipeline + vault + consent | High; no identity/beauty inference; image bias, removal, offline tests |
| 1.3 / PLANNED | Shopping comparison | Explicit product queries, budget, region | Provider contracts, evidence/freshness model | High; authorized APIs only; stock/price timestamps, malformed data, rate-limit tests; no purchase automation |
| 1.4 / PLANNED | Perplexity research | Reviewed query and citations | Cloud boundary, secure credentials, request consent | Medium/high; API access separate from subscriptions; citation and outage tests |
| 2.0 / PLANNED | Knowledge graph across authorized sources | Explicit saved entities and relationships | Vault schema, provenance, search, export | High; no unsupported sensitive inference; graph migration/deletion/isolation tests |
| 2.1 / PLANNED | Add supported Android capabilities | Capability-specific grants | API availability checks + capability review | Varies; OS/OEM gates and denial tests; no sandbox bypass |
| 3.0 / EXPERIMENTAL | Better local inference | Authorized local input, explicitly downloaded models | Model lifecycle, resource budgets, device support | High; accuracy, cancellation, thermal/memory/device matrix, model provenance |

Life's Money, Study, Skills, Daily Care and Advanced automation remain planned under
these gates; versions are not fixed until a coherent user outcome is validated.
No deprecated features yet. No remote configuration or real update feed yet.

## Dependency map
Foundation → permissions + storage + security + jobs + design/navigation.
Device → platform adapters → measurable facts → evidence-based recommendations.
Media → grants → metadata → OCR/QR → structured extraction → knowledge persistence →
search → cleanup review → fresh user confirmation → supported OS action.
Personal intelligence → vault + provenance + consent + retention → briefs/preferences.
AI → capability interfaces + privacy firewall + credential storage + provider adapter.
Life → personal intelligence + authorized data providers. Advanced → all prior gates.

## NEXT after checkpoint
On-device API 26/35+ validation, 375 dp and 200% fonts/TalkBack; migrate device readings
into a feature module only when necessary. Then bundled on-device OCR and QR recognition
with malformed-image tests, a provenance-aware encrypted knowledge vault and URL validation.
Do not expose deletion until extraction persistence and confirmation are verified end to end.
