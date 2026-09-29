# KANO — Master Product Roadmap

---

## Roadmap Phases Overview

```
PHASE 1 (Foundation)  ──► PHASE 2 (Device)      ──► PHASE 3 (Media & Vault)
[Complete]                 [Complete]                 [Complete - Kano 1.0 Release]
                                                           │
                                                           ▼
PHASE 6 (Life)        ◄── PHASE 5 (AI Engine)   ◄── PHASE 4 (Personal Intel)
[Planned]                  [Planned]                  [NEXT MILESTONE]
     │
     ▼
PHASE 7 (Advanced)
[Planned]
```

---

## Phase Breakdown & Status

### PHASE 1 — FOUNDATION [STATUS: COMPLETE]
- **Architecture**: Modular `:core` (Android-free) and `:app` architecture.
- **Database**: Room Database with Schemas 1, 2, and 3 exported.
- **Security**: Hardware-backed Android Keystore AES-256-GCM credential encryption.
- **Privacy**: Local-first Privacy Firewall, zero Internet permission in manifest.
- **Design System**: Warm paper / wine palette with accessible typography, dual themes, and Compose previews.

### PHASE 2 — DEVICE INTELLIGENCE [STATUS: COMPLETE]
- **Device Dashboard**: Real-time storage, available RAM, battery level/status, and OS metrics.
- **Storage Metrics**: Storage volume calculation via `StatFs` and `StorageStatsManager`.
- **System States**: Truthful loading, error, and unavailable UI states.

### PHASE 3 — MEDIA INTELLIGENCE & KNOWLEDGE VAULT [STATUS: COMPLETE - KANO 1.0 RELEASE]
- **Selected Documents**: System document picker (`OpenMultipleDocuments`) with SAF persisted read grants [COMPLETED].
- **Background Indexer**: WorkManager worker with streaming SHA-256 calculation [COMPLETED].
- **Search & Forget**: Literal filename search with wildcard escaping; confirmed index forgetting and grant release [COMPLETED].
- **On-Device OCR & QR Extraction**: Local ML Kit Text Recognition and Barcode Scanning on image URIs [COMPLETED].
- **Knowledge Vault**: Room Schema 3 database & dedicated Knowledge Vault screen with provenance tracing and entity management [COMPLETED].
- **Duplicate Detection**: Content hash (`sha256`) duplicate grouping [COMPLETED].
- **Extract Before Delete**: Re-validating source fingerprint, preserving Knowledge Vault entities, and releasing SAF grants before record removal [COMPLETED].

### PHASE 4 — PERSONAL INTELLIGENCE [STATUS: NEXT MILESTONE]
- **Gmail & Notifications**: Gmail OAuth connection and opt-in Notification Listener with important-only daily digest.
- **Daily Brief**: Non-intrusive daily summary answering "What matters today?".

### PHASE 5 — AI PROVIDERS & ROUTING [STATUS: PLANNED]
- **AI Router**: Multi-provider abstraction (Local, OpenAI, Gemini, Perplexity).
- **Privacy Firewall Integration**: Data sensitivity check, redaction, and request consent before egress.
- **Provider Conflict Synthesis**: Comparing model results and explicitly flagging uncertainty/disagreements.

### PHASE 6 — LIFE & LIFESTYLE [STATUS: PLANNED]
- **Style Studio & Wardrobe**: Photo scanning, clothing item classification, outfit combinations, wardrobe gap analysis, and virtual try-on preview.
- **Personal Care & Grooming**: Product scanner, personal care inventory, routine management, and anti-overspending checks ("What should I buy?").
- **Money Intelligence**: Authorized expense analysis, bill tracking, and spending trends (no automatic transactions).
- **Study & Skills**: OCR revision, quizzes, note summaries, and skill tracking.

### PHASE 7 — ADVANCED AUTOMATION [STATUS: PLANNED]
- **Cross-Domain Reasoning**: Integrated insights across schedule, wardrobe, spending, and device health.
- **Reversible Automation**: User-confirmed, reversible system actions with explicit audit logs.
