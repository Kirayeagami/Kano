# KANO — Known Limitations & Technical Debt

---

## 1. Current Development Scope Limitations

- Development Version: `0.1.0-dev`. Not final Kano 1.0.
- No active cloud or local LLM model is currently connected or executing inference.
- Document selection cap is 100 documents; max file size for SHA-256 hashing is 100 MiB.
- Text search is currently literal filename matching, not universal or semantic AI search.
- Credential entry UI is not yet connected (Keystore store adapter exists and is tested).

---

## 2. Platform & Provider Limitations

- Storage Access Framework URI permissions may be revoked by the OS or user; repository handles this gracefully by setting state to `ACCESS_REVOKED`.
- Document providers (e.g. Google Drive, OneDrive) may block or time out during stream reads; cooperative cancellation is enforced.
- Android app-private storage relies on device hardware encryption; no separate SQLCipher layer is used for metadata Room DB.
