# Validation protocol

Host checks: core policy tests, Android unit tests, lint, debug APK, instrumentation APK.
Read STATUS.md for actual executions; this protocol does not itself mean checks passed.

## Device acceptance matrix
- API 26 and API 35+; 375 dp width; 100% and 200% font; portrait/landscape; TalkBack.
- Start offline; navigate Home/Device/Media/Privacy; back navigation and rotation.
- Check real readings against OS storage-volume/battery scope; unavailable readings remain unknown.
- Cancel document picker; pick 0/1/100+ documents; duplicate URI; duplicate names.
- Search names with percent, underscore, backslash, Unicode; empty search and empty next page.
- Unsupported MIME despite picker filter; unavailable cloud provider; missing/unknown sizes.
- Zero-byte file; 100 MiB boundary; larger/misreported size; mutation during hashing; slow provider.
- Stop while indexing; process death; low-battery queued work; restart and explicit refresh.
- Revoke grant/delete original between import and indexing; verify name/hash purge and reselection.
- Confirm/decline forget during indexing; verify no rows reappear and original media survives.
- Inspect manifest: no INTERNET, broad media, QUERY_ALL_PACKAGES, or accessibility service.
- Inspect logs/backups: no names/URIs/content; all private data excluded from backup/transfer.
- Fresh install then upgrade preservation once schema 2 exists; no destructive migration.

## Release gates
No public release until all required device checks pass, signing and target API policies
are reviewed, privacy disclosures match actual data flows, dependency audit is complete,
and extraction/cleanup are verified end to end for the declared Kano 1.0 scope.
