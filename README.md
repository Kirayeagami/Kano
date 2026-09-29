# KANO — Native Android Personal Assistant

Private, native Android personal assistant. This repository is the **0.1.0-dev development foundation**, working toward Kano 1.0 (Device + Media). It is not a production release.

Available: real storage/memory/battery snapshots, explicit image/video selection, persisted read grants, local Room metadata index, bounded SHA-256 indexing through WorkManager, filename search, stop/retry, hardware Keystore credential encryption, and confirmed index forgetting. No original file deletion, Internet permission, analytics, AI provider, or connected account.

Read [status and validation](docs/STATUS.md), [state](docs/KANO_STATE.md), [architecture](docs/ARCHITECTURE.md), [roadmap](docs/ROADMAP.md), [privacy matrix](docs/PRIVACY-MATRIX.md), and [risks](docs/RISKS.md).

## Build & Verify

JDK 17, Android SDK platform/build-tools 35, Gradle wrapper. Set `JAVA_HOME`, set SDK location using `local.properties` (not committed), then run:

```powershell
.\scripts\check.ps1
```

Or execute Gradle tasks directly:

```powershell
.\gradlew.bat :core:test :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

The connected command requires an emulator/device attached. Installable development APK: `app/build/outputs/apk/debug/app-debug.apk`. Production signing is not configured.

Dependencies are pinned in Gradle.

## Boundaries

Only user-selected image/video document URIs are indexed. A maximum of 100 selected documents and 100 MiB per hash bounds the first slice. There is no full-library scanner, semantic classification, OCR, URL extraction, knowledge vault, duplicate review UI, or cleanup executor yet. Hash equality alone is never permission to delete. Document providers may fetch content remotely themselves; Kano does not make network requests.

App-private metadata is not an additionally encrypted vault. Sensitive record storage, retention, exports, migrations, release signing, and provider integrations need their own reviewed implementations. Read [AGENTS.md](AGENTS.md) before changing the project.

An Android Keystore credential adapter is implemented and tested; no real credentials or provider integration is connected. See [KANO_WORKLOG.md](KANO_WORKLOG.md) and [docs/HANDOFF.md](docs/HANDOFF.md) for the consolidated record, evidence, and multi-agent handoff.
