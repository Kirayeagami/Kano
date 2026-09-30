# Permissions and actual data flow

| Capability | Actual access | Processing/retention and limits |
|---|---|---|
| Device | StatFs, ActivityManager.MemoryInfo, sticky battery broadcast, Build | Foreground snapshots; volume/RAM/battery/hardware only, no fabricated per-app/CPU/health attribution |
| Connectivity | ACCESS_NETWORK_STATE normal permission | Connection metadata only; INTERNET removed |
| Gallery | READ_MEDIA_IMAGES/VIDEO, Android 14+ selected-access permission; legacy READ_EXTERNAL_STORAGE max API32 | Permission-aware MediaStore pages; full/images-only/videos-only/partial/denied/revoked states; no all-files access |
| Selected documents | ACTION_OPEN_DOCUMENT and persisted read grants | Room metadata and bounded hashes; up to100 documents and100MiB/hash; forget releases index/grants, keeps originals |
| Camera | Explicit CAMERA runtime permission; optional camera feature | Lifecycle-bound CameraX; private pending capture reviewed before explicit Use; API29+ Use publishes Pictures/Kano; retake/discard removes pending private capture only |
| OCR/QR | Authorized image content URI | Bundled on-device Latin OCR/QR, encoded20MiB cap and sampled decoder; candidates uncertain, no auto-open/auto-save; sensitive patterns blocked |
| Vault | Explicit reviewed save with fresh source digest | App-private Room schema3; database-level encryption absent; legacy sensitive rows masked in UI, raw rows retained |
| Care | Explicit user entry | Local validated inventory/drafts; stock status is user supplied; confirmed record deletion |
| Source cleanup | No executor exposed | Original files are retained; durable extraction receipt and OS delete confirmation remain missing |
| Cloud/accounts | No adapter, request or Internet permission | OpenAI/Gemini/Perplexity/Gmail/browser/notification sources remain disconnected; no external upload |
| Credentials | Android Keystore adapter | Encrypted bound credential files; no live credentials used; backup/transfer disabled |

Third-party document providers can themselves fetch remote files. Sensitive-pattern
screening is conservative but does not guarantee detection. No private source content,
raw URI or exception payload is deliberately logged. Physical validation screenshots
may show user content and are kept outside Git. No accessibility service/root/private
app scraping/blanket package visibility is implemented.
