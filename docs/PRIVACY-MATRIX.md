# Permissions, data, and capability matrix

| Capability | Access / source | Processing and retention | Native limits / current state |
|---|---|---|---|
| Device readings | StatFs, ActivityManager, sticky battery broadcast, Build | On-demand memory; no saved telemetry | Real device/storage-volume scope; no per-app battery or CPU attribution |
| Selected media | ACTION_OPEN_DOCUMENT; persistable read URI grant | Local metadata + bounded streaming hash until user forgets | Only selected image/video documents; permission may be revoked; implemented slice |
| Whole library | MediaStore + OS-version-specific media grants | Incremental local index, exclusions | Planned; partial Android 14+ access, no all-files permission |
| Apps | PackageManager visibility + optional usage special access | Local app inventory/usage | Planned; arbitrary installed-app visibility/storage and force-stop unavailable |
| OCR/QR | Selected image bytes | Local extraction into reviewed vault | Planned; QR/URL never automatically opened |
| Delete media | MediaStore delete/trash request or eligible document provider | Explicit file-specific confirmation after durable extraction | Planned; not every URI is deletable; cannot silently delete |
| Notifications | NotificationListener user grant | Sensitive local filtering, opt-in retention | 1.1; cannot read historical notifications or remove in-app ads |
| Gmail | OAuth minimum scopes, provider review | Local minimized records, disconnect/purge | 1.1; credentials needed, no private Gmail DB access |
| Browser | Share intent / user import / supported extension | Selected links only | Planned; no arbitrary app history access |
| Cloud AI | Provider API credential + reviewed request consent | Minimized approved payload; documented provider retention | Disabled; no Internet permission or provider in this build |
| Money / wardrobe / study | Explicit imports and selected sources | Sensitive storage review required | Planned; no payments or inference of sensitive traits |
| Update metadata | Bundled release catalog initially | No user data transmitted | No remote available-version claim; store/update adapter planned |

Android app-private storage is not a promise of absolute secrecy. Device compromise,
unlocked-device access, screenshots, and user exports are separate threats. No raw file
content, URI, name, or exception payload is written to diagnostics. Production logging
must preserve this. Third-party document providers may themselves fetch cloud files;
Kano does not control the provider's network/retention policy.
