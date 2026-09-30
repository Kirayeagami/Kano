# Security and access

INTERNET, all-files access, blanket package visibility and legacy write-storage
permission are absent. ACCESS_NETWORK_STATE reads local connectivity metadata.
Camera and appropriate photo/video/selected-media access are requested explicitly;
legacy READ_EXTERNAL_STORAGE is capped at API32. No notification listener,
accessibility service, account scraping, OAuth tokens or cloud credentials are used.

Room data remains app private, without additional database encryption. Backups and
transfer are disabled. Credential ciphertext uses bounded atomic no-backup records and
Android Keystore AES-GCM with slot/version-bound AAD; hardware backing is not guaranteed.

Shared Vision blocks conservative sensitive signals, including credentials in QR,
text excerpts and URL query/fragment/user-info. A heuristic cannot prove content safe.
Review and exact-input revalidation precede Vault saving. Legacy unsafe display fields
are hidden without silently deleting existing records.

No original media cleanup is exposed. Explicit camera Use publishes only that capture;
record deletion and index forgetting explain their local scope. Gallery permission
changes invalidate thumbnail caches and recheck access. No private media/text is logged,
exported to cloud or added to Git during physical-device verification.
