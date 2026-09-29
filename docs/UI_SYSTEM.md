# KANO UI 2.0 System Architecture

---

## 1. Design Language & Visual Principles

The Kano UI 2.0 system is designed around the visual language extracted from mobile product design reference images:
- **Clean Off-White Canvas**: Bright Mode uses a warm off-white background (`#F8F6F2`) with pure white elevated surface cards (`#FFFFFF`).
- **Nothing-Style Dark Mode**: Dark Mode uses a pure black canvas (`#000000`), deep charcoal card surfaces (`#161616`), warm-white display typography (`#FFFFFF`), and clean 1dp borders (`#2A2A2A`).
- **Oversized Display Typography**: Bold section titles and giant display metrics (e.g. `72%` storage used, `3 Things Need Attention`).
- **Generous Rounded Corner Radii**: Hero cards, search inputs, and visual blocks feature 20dp – 28dp rounded corner shapes (`RoundedCornerShape(24.dp)`).
- **Vibrant Accent Palette**: Primary accent is Coral/Orange (`#FF3B1D` / `#E84A27`) paired with pastel container tints (Peach `#FFEAE5`, Soft Blue `#E8F2FF`, Mint Green `#E8F8F0`, Soft Lavender `#F0ECF9`).
- **Floating Bottom Navigation Bar**: Floating navigation surface (`RoundedCornerShape(28.dp)`) elevated above the canvas with active tab pill indicator and Material 3 vector icons.

---

## 2. Component Inventory

| Component | Class | Description |
| --- | --- | --- |
| **Glass Card** | `KanoGlassCard` | Translucent glassmorphism container surface with 24.dp rounded corners, soft shadow, and low-opacity border. |
| **Elevated Card** | `KanoCard` | Pure surface card with 24.dp rounded corners and 1.dp outline border. |
| **Hero Button** | `KanoHeroButton` | Full-width linear gradient button (`#FF5A36` $\rightarrow$ `#E82E0E`) with 20.dp rounded corners. |
| **Button** | `KanoButton` | Standard primary CTA button with 18.dp rounded corners. |
| **Outlined Button** | `KanoOutlinedButton` | Secondary outlined button with 18.dp rounded corners and subtle border. |
| **Status Chip** | `StatusChip` | Pill badge container with bold uppercase label. |
| **Wave Background** | `KanoWaveBackground` | Ambient, slow organic background wave drawn on canvas layer behind content. |
| **Gradient Hero** | `KanoGradientHero` | Translucent gradient hero block. |

---

## 3. Screen Visual Identity Matrix

- **Home**: Command center layout with user greeting, rounded search box, hero brief card, intelligence services grid (Device, Media, Vault, Style, Care), and real-time active status.
- **Device**: Technical analytics dashboard with giant `72%` storage percentage, progress meter, memory snapshot card, battery charging badge, and device specs.
- **Media**: Image-first document list with AI Found category chips, search bar, status badges, and Local OCR & QR scan action buttons.
- **Vault**: Knowledge Vault screen with category filter chips (`ALL`, `WEBSITES`, `STUDY`, `MOVIES`, `RECEIPTS`, `QR`), search, provenance links, and entity management.
- **Style Studio**: Outfit scanner camera viewport (220dp container with warm gradient background and camera framing box), today's outfit recommendation, style criteria matrix, and wardrobe catalog.
- **Personal Care**: Product scanner actions, anti-overspending smart check card, and Room DB backed product inventory list.
- **Privacy & Settings**: Interactive theme switcher (`System Default`, `Light`, `Dark`), Glass Mode toggle, Reduced Motion toggle, and scoped permission disclosures.
