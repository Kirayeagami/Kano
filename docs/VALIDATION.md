# Android validation protocol

Evidence belongs in TEST_STATUS.md; this checklist alone is not a passing result.
Always explicitly launch app.kano/.MainActivity and verify Kano is foreground before
capturing. Launcher screenshots are not Kano acceptance evidence. Inspect real PNGs,
correct observed layout/action defects and rerun the affected paths.

- Core/host tests, lint, both APKs and merged-manifest permission/backup guards.
- API35 emulator then OnePlus Nord CE5/API36; installs preserve app data. Never run
  synthetic/destructive instrumentation fixtures against personal phone data.
- Home, Device, Storage, RAM, Battery, Connectivity, Apps, Diagnostics/System/Security,
  Gallery and detail sheets, Vault, Style tabs, Care/editor, Settings/subpages/menu.
- All8 styles in Light/Dark, System contrast, sole Glass OFF toggle, Reduce Motion.
- 320/360dp and wide layouts, portrait/landscape, 100/200% fonts, gesture/3-button safe
  areas, TalkBack and physical controls. Record which cases remain untested.
- Reach end-of-list actions above footer, hamburger close/scroll, actual press/navigation
  feedback; no dead actions, fake AI widget or invented reading/price/recommendation.
- Camera permission denial/settings recovery, preview, lens/flash availability, actual
  capture/review/retake/discard, rotation recovery, explicit publication and local analysis.
- Real MediaStore permission/type/search/page behavior, partial/denied/revoked access,
  thumbnails with unknown/invalid content, content changes/resume and source-open failure.
- OCR/QR malformed/oversized input, sensitive QR/text, digest mutation, reviewed save,
  idempotence and cancellation; source original must survive index forgetting.
- Keystore binding/tamper/redaction, Room migration/preservation, logs and backup scope.

No public release until declared functionality, wider Android/device matrix, encrypted
sensitive storage, release signing and policy review are verified. Current original-file
cleanup and cloud integrations remain unavailable.
