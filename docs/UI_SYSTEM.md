# KANO — Advanced UI System & Motion Specifications

Updated: 2026-09-29

---

## 1. Palette & Surface Hierarchy

| Surface Level | Color Code / Spec | Description |
| --- | --- | --- |
| **Off-White Base** | `#FAF8F5` | Clean, warm off-white background |
| **Surface White** | `#FFFFFF` | Opaque surface container |
| **Translucent Glass** | `Color.White.copy(0.85f)` | Translucent glassmorphism surface with 1.dp `#00000014` border |
| **Peach Container** | `#FFE8E0` | Soft coral container for notifications & hero cards |
| **Lavender Container**| `#E8E5F8` | Soft secondary container for Style & Care features |
| **Coral Primary** | `#E84A27` | Primary brand accent and gradient hero background (`#FF5A36` -> `#D8391A`) |
| **Ink Text** | `#121212` | High-contrast primary text |
| **Muted Ink** | `#666666` | Secondary body & label text |

---

## 2. Glassmorphism System

Implemented reusable glass components:
- `KanoGlassSurface`: Translucent container with soft diffuse shadow (4.dp elevation) and crisp subtle border.
- `KanoGlassCard`: Column wrapper over `KanoGlassSurface` with 18.dp content padding.

---

## 3. Motion & Animation Timing Standards

| Category | Duration / Easing | Usage |
| --- | --- | --- |
| **FAST** | `150ms` (Spring Stiffness 400f) | Card press scale down (1.0 -> 0.98 -> 1.0) & button touch feedback |
| **MEDIUM** | `350ms - 500ms` (`FastOutSlowInEasing`) | Card entrance fade & slide-up, tab state transitions, numeric text transitions |
| **SLOW / AMBIENT** | `2200ms - 8000ms` (`FastOutSlowInEasing`) | Ambient wave drift (`KanoWaveBackground`) & floating circular button translation |

---

## 4. Reusable Motion Components (`MotionComponents.kt`)

- `KanoAnimatedCard`: Card with entrance slide/fade and spring press scale interaction.
- `KanoFloatingControl`: Circular floating action button (`CircleShape`) with subtle ambient Y-offset floating loop.
- `KanoWaveBackground`: Ambient background wave layer drawn behind content.
- `KanoGradientHero`: Linear gradient background container with 24.dp rounded corners.
- `KanoAnimatedNumericText`: Vertically sliding numeric text transition for dynamic display metrics.

---

## 5. Accessibility & Reduced Motion

- **TalkBack Semantics**: Every floating control, icon button, and surface card provides explicit `contentDescription` or `semantics`.
- **Text Scaling**: Tested up to **200% font scale**; display headings wrap naturally without horizontal clipping.
- **Touch Targets**: Minimum **48 dp x 48 dp** on all interactive surfaces.
- **Contrast**: Text is never placed over low-contrast glass without an opaque or high-contrast background layer.
