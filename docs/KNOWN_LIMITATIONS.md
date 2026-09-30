# Known limits — current development build

- Full product functionality is unfinished. UI migration does not establish live
  Style/Dress Me, Shopping, Gmail, notifications, calendar or cloud AI integration.
- Local Vision supports bundled Latin OCR and QR/web-address candidates. It does not
  verify movies, brands, garments, products, ingredients, medical content or quantities.
- Sensitive-pattern blocking is conservative but incomplete. Query/fragment/credential
  URLs are blocked in excerpts as well as URL fields. Private/sensitive storage requires
  further review; Room is app-private, not additionally encrypted.
- Gallery counts and sizes cover each accessible 60-item page. Large files means >=100
  MiB; likely screenshots uses the reported album name. No semantic certainty is inferred.
- Global gallery indexing, near duplicates, cleanup/trash/restore and durable source
  extraction receipts are unfinished. Original-file deletion is not exposed.
- Optimize RAM only drops Kano thumbnail-cache references. Displayed images can retain
  bitmaps. System readings fluctuate independently and do not prove an improvement.
- SAF utility retains 100 selected documents and a 100MiB hash cap. It is separate from
  the primary gallery; forgetting releases grants/removes index rows, not original files.
- Home search is local commands/keyword matching, not an LLM or complete universal index.
- Connectivity uses local ACCESS_NETWORK_STATE, without INTERNET. Bluetooth device
  permissions are not requested. Other-app/system/category sizes may be unavailable.
- Glass blurs a decorative ambient layer on API31+, not a sampled backdrop. Performance
  profiling, TalkBack, API26, large libraries, full process-death/permission matrix and
  API36 target migration remain validation gates.
- Device Intelligence hardware boundaries: Live CPU core frequencies and raw per-zone
  thermal sysfs files are restricted by Android SELinux sandboxing on public releases.
  Live battery current draw (mA) and charge counter (µAh) require OEM battery fuel-gauge
  HAL support; when unsupported by OEM hardware or emulators, they gracefully report
  unavailable while standard battery percent and status remain live. Zero metrics are synthetic.
- Physical device OnePlus Nord CE 5 (CPH2717, Android 16 / API 36) is connected,
  authorized, and validated live via ADB (PJQ8DYB6EI6HRG4X).
- Phase 2 Storage Intelligence strictly covers truthful metadata scanning, keyset paging,
  Room database indexation (migration 3->4), duplicate detection by exact SHA-256 hash,
  and advisory categorization (Large, Downloads, Duplicates, Temporary). In accordance with
  the Phase 2 mandate, destructive operations, permanent delete, bulk delete, trash, and
  RAM optimizations are excluded.
- Scoped storage boundaries on Android 16: Public MediaStore contents require user-granted
  media permissions; without media permissions or document tree selections, Kano gracefully
  reports access restrictions and only inspects own-created downloads and persisted SAF grants.
- Existing historical screenshots/worklog claims can show fake baseline UI; use the
  current dated validation evidence and source.
