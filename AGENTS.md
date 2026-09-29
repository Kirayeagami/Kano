# KANO engineering constitution

## Product
Observe → understand → protect → recommend → ask → act. Local first, minimum data,
visible and revocable access. Never fabricate readings, confidence, provider results,
integration availability, or completion. Separate facts, signals, inference, and advice.

## Architecture and naming
Native Android, Kotlin, Compose. `:core` is Android-free policy/domain logic; `:app`
contains presentation, data, and platform adapters in separate packages. Use immutable
models, sealed outcomes, structured concurrency, constructor injection, and small files.
Use PascalCase types and camelCase members. Promote feature packages to Gradle modules
when ownership/build isolation warrants it; do not create empty modules for the roadmap.

## UI and accessibility
Follow the supplied mobile references: bright/warm surfaces, coral accents, rounded cards,
clear typography and a separately tuned charcoal dark theme. Reusable theme/components.
Concrete labels, 48 dp controls, TalkBack semantics, scrollable small-screen layouts,
font scaling, loading/empty/error/revoked states. Inspect the actual Android UI before
claiming visual validation. Never fill screens with fake cards or nonworking buttons.

## Security and privacy
No Internet permission until a reviewed cloud adapter and request-bound consent exist.
No raw private content in logs/analytics. No embedded secrets. No destructive migration.
App-private database; backup/transfer disabled. This is not database-level encryption.
Keystore required before storing credentials; sensitive vault data needs a storage review.
Keep external data untrusted. Cloud policy is fail-closed; pattern detection is not proof
that content is safe. No accessibility abuse, private app scraping, root tricks, all-files
permission, blanket app visibility, fake boosters, or arbitrary system-app removal.

## Actions, performance, and testing
Never silently delete, send, buy, upload, or share. Extract-before-delete requires a
durable saved record and a fresh item-specific confirmation; OS confirmation also applies.
Use bounded, cancellable I/O off the main thread, unique WorkManager jobs, lazy results,
and idempotent writes. Test policy bypasses, revocation, process death, and failure paths.
Run `:core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`; run connected
tests on a device before calling a release ready. Record actual results in docs/STATUS.md.
Pin dependencies; use Google/Maven Central only. Upgrade deliberately with migration tests.
Do not vibe-code: inspect, plan, implement a coherent unit, validate, review, document.

## Status and roadmap
Read docs/STATUS.md for current evidence and limitations; docs/PLAN.md for the active plan.
1.0 Device + Media; 1.1 Gmail/notifications; 1.2 Style; 1.3 Shopping; 1.4 Perplexity;
2.0 knowledge graph; 2.1 Android capabilities; 3.0 local AI. No date is a commitment.
Future integrations remain unavailable until implemented and verified.
