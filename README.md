# Kano — native Android

Kano 0.1.0-dev is a local-first development app. Every existing screen now uses the
shared native Compose presentation system. The latest user request is refinement and
actual Android validation, not another architecture/UI rewrite.

Implemented: eight visual themes with Light/Dark/System, one Glass OFF switch, reduced
motion, compact floating navigation and hamburger controls; real foreground device
readings; permission-aware MediaStore gallery and actual thumbnails/details; lifecycle
CameraX capture/review; bundled Latin OCR/QR; explicitly reviewed Vault records; manual
Care inventory; selected-document WorkManager index; Room migrations1→2→3; Android
Keystore credential adapter. No live cloud provider/account or Internet permission.

Style generation, shopping prices, Gmail/notification intelligence, semantic Universal
AI Search, advanced knowledge graph and original-file cleanup are unfinished. Screens
expose truthful unavailable states and working local actions. RAM action clears only
Kano thumbnail-cache references and reports actual before/after readings without a
performance-gain claim. App-private Room storage is not database-level encryption.

Read [current status](docs/STATUS.md), [runtime evidence](docs/TEST_STATUS.md),
[UI system](docs/UI_SYSTEM.md), [architecture](docs/ARCHITECTURE.md),
[privacy](docs/PRIVACY-MATRIX.md) and [handoff](docs/HANDOFF.md).
[KANO_WORKLOG.md](KANO_WORKLOG.md) is the single consolidated record with supplied
prompts, changes/reasons, failures, evidence, APK details and continuation guidance.

## Build

JDK17, Android SDK35 and pinned Gradle dependencies. Local setup is untracked.

```powershell
.\scripts\check.ps1
```

The script runs core/host tests, lint, APK/test-APK assembly and merged-manifest guards.
Run instrumentation on an explicitly selected emulator, preserving personal-device data:

```powershell
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
adb -s emulator-5554 install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s emulator-5554 shell am start -n app.kano/.MainActivity
adb -s emulator-5554 shell am instrument -w app.kano.test/androidx.test.runner.AndroidJUnitRunner
```

Development APK: `dist/Kano-debug.apk`. Release signing is not configured.
Compose previews are in `UiPreviews.kt`; actual Android screenshots are linked from
TEST_STATUS.md. Physical-phone private imagery is intentionally outside Git.
