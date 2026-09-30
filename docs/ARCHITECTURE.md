# Architecture

Native Kotlin, Compose, Navigation Compose, Room 3 and WorkManager; Android-free :core
policy/contracts + :app data/platform/presentation. Compile/target35, min26. Existing
schemas 1,2,3 and 1→2→3 migrations are preserved. No destructive database fallback.

MainActivity owns lifecycle, themes, compact navigation and centralized backgrounds.
KanoViewModel adapts repository/platform state, foreground device sampling, gallery
pagination, reviewed Vision saving and confirmed local inventory operations.

GalleryRepository queries MediaStore in bounded 60-item pages. CancellationSignal
cancels supporting providers; every page completion rechecks access. An 8MiB thumbnail
cache with three concurrent decoders invalidates generations on changes/revocation.
Original image decoding is excluded from the cache. Local OCR has a 20MiB encoded limit
and 1600px sampled dimensions with EXIF orientation.

MediaIntelligenceProcessor is one bundled ML Kit OCR/QR engine shared by gallery,
CameraX, Style and Care. It returns review candidates, source URI and exact-input digest;
it never automatically saves. A chosen candidate triggers a fresh source scan, digest
comparison and conservative persistence checks before idempotent Room storage.
KnowledgeRepository permits at most1000 records and blocks sensitive-pattern text and
unsafe web URLs. Legacy sensitive records remain in the database but are hidden in UI.

CameraX binds preview/capture to the actual lifecycle owner. A capture stays private
until explicit Use; Android29+ then publishes to Pictures/Kano through IS_PENDING.
Retake/discard remove only the pending private capture. Older Android retains it private.

The SAF document index remains an optional bounded WorkManager utility. Hash equality
does not authorize deletion; forgetRecord removes an index row and grant only.

DeviceReader samples public Android metrics every 3 seconds in the foreground. It
gathers 100% genuine OS telemetry across 10 subsystems: Identity (Build/Version/Fingerprint),
Performance (SoC model, /proc/cpuinfo architecture, cores, ABIs, OpenGL ES), Memory
(ActivityManager MemoryInfo, used/available, low-memory pressure and thresholds),
Storage (StatFs data partition, Kano app-private breakdown across files, cache, code cache,
and SQLite database), Battery (BatteryManager charging status, plug type, health, temperature,
voltage, live current draw, average current, charge counter, gauge capacity), Thermal
(PowerManager thermal status and 30s headroom forecast), Connectivity (NetworkCapabilities
Wi-Fi/Cellular transports, unmetered status, upstream/downstream bandwidth, airplane mode),
Peripherals (Display resolution, density, physical diagonal, refresh rate, HDR, Wide Color P3;
CameraManager lens counts, hardware levels, flash; Audio output/input routes; Vibrator
haptic amplitude control; NfcAdapter; Location master switch), Sensors (SensorManager TYPE_ALL
inventory and power draw), and Subsystem Health & Diagnostics. Kano strictly prohibits
fabricated scores: Benchmark / Health scores are explicitly marked 'Unavailable'.
Memory maintenance only clears Kano thumbnail-cache references and reports real samples.
onTrimMemory also clears this cache; no force-stop of other apps or forced GC.

KeystoreCredentialStore and request-bound PrivacyFirewall/AiRouter contracts are
preserved. No provider is installed; INTERNET is removed by manifest merger. Normal
ACCESS_NETWORK_STATE supports local connection type without network requests.

VisualSystem/Theme/MotionComponents/ScreenComponents define one native design system.
Eight styles, theme modes, glass modes and reduced motion persist through ThemeManager.
Public Android reference: https://developer.android.com/training/data-storage/shared/media
Partial-access reference: https://developer.android.com/about/versions/14/changes/partial-photo-video-access
Camera compatibility: https://developer.android.com/jetpack/androidx/releases/camera
