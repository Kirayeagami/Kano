# KANO — Deletions and Renames Register

This file records any file or component deletions/renames across all multi-agent development sessions.

---

## Deletions Log

| Date | Agent | Path | Action | Reason | Dependencies Checked | Replacement | Test Result |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 2026-09-29 | AGENT_A | N/A | INITIALIZED | Register established; no files deleted in initial baseline slice. | Yes | N/A | PASS |

---

## Rules for Deletions
1. Always search for references across all modules (`:app`, `:core`) before removing any file.
2. Verify that replacement logic or class exists and is tested.
3. Record every deletion or rename in this log with exact path, reason, and test result.

## Recovery removal of unsupported runtime content
- InformationScreens: removed hard-coded storage/care/queue alerts and absolute privacy claims; replaced with functional navigation and explicit capability descriptions.
- DeviceScreen: removed unmeasured queued-item alert; retained actual platform snapshots.
- MediaScreen: removed fabricated semantic counts and inert Review action; retained index flow.
- StyleScreen: removed simulated analyzer and its private ScoreRow/WardrobeCategoryTile/GapRow helpers; replaced with explicit unavailability.
- PersonalCareScreen: replaced seeded PersonalCareItem/ProductInventoryCard UI with persisted manual CareRecord editing.
- Theme: removed mutable global palette assignments; composition-scoped semantic getters replace them.
References checked across app/src with rg before replacements. No schema-1 fields, original media files or unrelated projects were deleted.

## 2026-09-30 — complete presentation replacement
Root is the only writer. Source references were searched with rg before replacement.
No Room schema, database field, security implementation, repository history, original
media or unrelated project was deleted.
- Theme.kt: retired unused KanoCard/KanoCircularIconButton and fixed light/dark constants.
  No call sites existed; semantic Theme/VisualSystem tokens replace them.
- MotionComponents.kt: retired unused KanoAnimatedCard, KanoFloatingControl,
  KanoGradientHero and KanoAnimatedNumericText. No call sites existed; centralized
  surfaces, waves, processing visualization and navigation transitions replace them.
- All old dashboard compositions were replaced in place. Function/route references
  remain; there is one active presentation system, not duplicate old/new screens.
- MediaScreen duplicate KnowledgeEntityCard removed. Vault now owns source review and
  confirmed deletion, eliminating the duplicate unconfirmed delete action.
- UiPreviews fabricated sampleDeviceSnapshot removed; previews use explicit loading
  or empty states and the real native components.
- MediaRepository.extractBeforeDelete renamed forgetRecord; reference/tests changed.
  The old name implied deleting originals; implementation only forgets index/grant.
Build/runtime validation evidence is recorded in TEST_STATUS.md; no current pass result
is assumed from earlier sessions.
