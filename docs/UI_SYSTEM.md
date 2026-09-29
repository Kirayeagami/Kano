# KANO — Dual Theme System & UI Specifications

Updated: 2026-09-29

---

## 1. Dual-Theme Palette & Semantic Tokens

### Bright Mode (Light Theme)
- **Canvas Background**: `#FAF8F5` (Warm Off-White)
- **Surface**: `#FFFFFF` (Surface White)
- **Surface Variant**: `#F3F0EC` (Light Surface Variant)
- **Primary Ink**: `#121212` (Near-Black)
- **Secondary Ink**: `#666666` (Muted Charcoal)
- **Coral Accent**: `#E84A27`
- **Peach Container**: `#FFE8E0`
- **Lavender Container**: `#E8E5F8`
- **Green Container**: `#E8F5E9`
- **Glass Surface**: `Color.White.copy(alpha = 0.85f)`
- **Glass Border**: `Color.Black.copy(alpha = 0.08f)`

### Dark Mode (Dark Theme)
- **Canvas Background**: `#141210` (Deep Warm Charcoal)
- **Surface**: `#1E1C18` (Deep Surface)
- **Surface Variant**: `#282520` (Dark Surface Variant)
- **Primary Ink**: `#F7F4F0` (Warm Off-White Ink)
- **Secondary Ink**: `#9E9790` (Soft Warm Gray)
- **Coral Accent**: `#FF6C4B` (Tuned Warm Coral for Dark surfaces)
- **Peach Container**: `#3D1B13` (Dark Warm Peach)
- **Lavender Container**: `#2A263B` (Dark Lavender)
- **Green Container**: `#1C3322` (Dark Green)
- **Glass Surface**: `Color(0xFF221F1A).copy(alpha = 0.88f)`
- **Glass Border**: `Color.White.copy(alpha = 0.12f)`

---

## 2. Theme Architecture & Switching

- **Enum `KanoThemeMode`**: `SYSTEM`, `LIGHT`, `DARK`.
- **Theme Persistence**: Managed via `ThemeManager` in `SharedPreferences` (`"kano_theme_prefs"`).
- **Dynamic System UI**: Coordinated status bar & navigation bar with edge-to-edge support.
- **Theme Selector**: Interactive segmented selector (`System`, `Light`, `Dark`) on the Settings/Privacy screen.

---

## 3. Dual-Theme Compose Previews (`UiPreviews.kt`)

Implemented side-by-side previews for Android Studio split view:
- `HomeScreenPreviewLight` / `HomeScreenPreviewDark`
- `DeviceScreenPreviewLight` / `DeviceScreenPreviewDark`
- `StyleScreenPreviewLight` / `StyleScreenPreviewDark`
- `PersonalCareScreenPreviewLight` / `PersonalCareScreenPreviewDark`
- `SettingsScreenPreviewLight` / `SettingsScreenPreviewDark`

---

## 4. Live Emulator Validation Screenshots

Screenshots captured live on **Android 15 (API 35) Laptop Emulator (`emulator-5554`)**:
- **Light Mode Directory**: `docs/validation/light/` (`home.png`, `device.png`, `media.png`, `style.png`, `care.png`, `privacy.png`)
- **Dark Mode Directory**: `docs/validation/dark/` (`home.png`, `device.png`, `media.png`, `style.png`, `care.png`, `privacy.png`)
