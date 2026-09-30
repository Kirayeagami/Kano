# Kano native UI system

One Compose system for all existing routes: Home/command search, Device and eight
subpages, Gallery/photo-video sheets/document index, Camera/review/Vision, Vault,
Style/Dress Me/Wardrobe, Care/editor, Shopping, Daily Life and Settings subpages.
Working data, Room, credentials and policies are preserved. Unimplemented features
have factual unavailable states; reference-image example values are not seeded.

## Appearance

Eight persisted styles: Kano Glass, Soft Editorial, Minimal Mono, Warm Studio, Aurora,
Paper, Midnight, Technical. Each has distinct light/dark palettes, radius and typography.
Light/Dark/System mode follows user choice. Existing theme and glass preferences migrate.
Latest user brief supersedes earlier glass modes: the only visible control is Glass OFF.
Normal appearance uses translucent surfaces; OFF uses opaque surfaces with the same
layout/actions. AUTO is accepted only internally as an old persisted preference and
normalized to normal glass-enabled appearance. There is no Auto/On selector in the UI.

Glass surfaces use actual alpha, borders/shadows and subtle ambient gradients. Supported
Android RenderEffect blurs only the ambient layer, never text/images; this is not a
claim of background-pixel sampling/refraction. The footer has a slow reflection plus a
small animated active pill. Expensive effects pause/reduce offscreen, under low memory,
with Reduce Motion or disabled Android animators. OFF retains ordinary UI animations.

## Navigation and interaction

The floating 56dp footer has Home, Device, Media, Vault, Style and Care: six accessible
48dp actions, constrained width, gesture/3-button insets and reserved content space.
The top hamburger opens a fully expanded scrollable sheet for Settings, Appearance,
Privacy, Permissions, providers, Diagnostics, Shopping, Daily Life and About. Secondary
settings retain a back/overview path. Camera hides normal navigation during capture.

Screen fade/slide transitions, native sheet motion, spring press feedback, subtle icon
selection, search morphing, actual image reveal, real-operation recognition motion and
slow contextual waves share motion tokens. Idle Home has no orb/fake AI status. Loading
has no synthetic percentages. Android text sizes and scrollable/adaptive layouts apply.
Device/Settings details reset scroll per page. Gallery grid headers, stats, error and
paging groups use Columns so Compose does not overlap multiple children in one grid item.
Camera review/overlay scroll when large fonts or landscape reduce available space.

Native previews are in UiPreviews.kt; real runtime screenshots and limits are documented
in TEST_STATUS.md. Android Studio launch is distinct from a verified IDE preview/sync.
No unavailable Stitch MCP or authenticated design export is claimed.
