# Current feature catalog

Native Kotlin/Compose :app plus Android-free policy :core. Version 0.1.0-dev, Room
schema 3, non-destructive migrations 1→2→3. Product groups describe ownership, not
empty Gradle modules.

| Area | Current implementation | Remaining work |
|---|---|---|
| Foundation | Eight native themes, light/dark/system, single Glass OFF switch, reduced motion, compact dock, hamburger, Room, WorkManager, Keystore, fail-closed text policy | Release signing, encrypted sensitive storage, wider device/accessibility validation |
| Device | Foreground 3-second storage-volume/RAM/battery/connection/hardware readings, Android settings actions, measured Kano-only thumbnail-cache release | App inventory, usage attribution, health/cycle/CPU metrics not available through current adapter |
| Media | Real permission-aware MediaStore gallery, 60-item pages, filename/type/size/likely-screenshot filters, page albums, real thumbnails/details, CameraX preview/capture/review | Whole-library intelligence, perceptual groups, durable extraction receipts and OS-confirmed original-file cleanup |
| Local Vision | Bundled ML Kit Latin OCR and QR, EXIF-aware bounded decoder, sensitive-pattern suppression, reviewed URL/text candidates | Other scripts, semantic vision/identity, model lifecycle and accuracy assessment |
| Vault/search | Explicit reviewed candidate save, source/digest recheck, provenance, idempotency, filtering/details/confirmed record deletion; Home local command and current-page keyword matching | Persistent full cross-domain index, graph/semantic search, sensitive storage, export and retention controls |
| Life | Manual Care inventory with validation/edit/confirmed delete; new native Style/Dress Me/Wardrobe/Shopping/Daily Life screens expose real unavailable state and real camera/gallery/inventory links | Clothing/wardrobe model, product identity, shopping provider and daily-source adapters |
| Cloud/integrations | Router contracts and local privacy boundary; no Internet permission/live credential/provider | OpenAI/Gemini/Perplexity/Gmail/browser/notification adapters with request consent and credential review |
| Advanced | Product roadmap only | Knowledge graph, cross-domain reasoning and audited automation |

No prices, recommendations, clothing, provider responses, battery-health scores,
cleanup success or RAM speedup are synthesized. Media metadata is page-scoped;
Android data-volume capacity is not a complete device partition breakdown.
