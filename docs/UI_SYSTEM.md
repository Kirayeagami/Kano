# KANO — UI System & Design Specifications

---

## 1. Palette & Surface Hierarchy

| Element | Color Code | Purpose |
| --- | --- | --- |
| **Paper Background** | `#F5F2EB` | Primary warm paper surface |
| **Wine Accent** | `#5C1D24` | Primary brand accent and interactive callouts |
| **Ink Text** | `#1C1B1F` | High-contrast body and heading text |
| **Subtle Border** | `#E0DCD3` | Surface boundaries and card dividers |
| **Container Surface** | `#EFECE4` | Elevated content cards and list items |

---

## 2. Radii & Spacing Rules

- **Corner Radii**: Restrained 4 dp to 8 dp. Never use 24 dp+ extreme bento rounding or pill controls everywhere.
- **Touch Targets**: Minimum **48 dp x 48 dp** for all interactive controls.
- **Padding Grid**: 8 dp, 16 dp, 24 dp increments.

---

## 3. Typography & Accessibility

- **Font Scaling**: Explicitly tested up to **200% system font size**.
- **Small Screens**: Minimum supported width is **375 dp** with full scrollable container fallbacks.
- **Navigation Layout**: Navigation items fall back to a two-row adaptive bar under large font scale.
- **TalkBack Semantics**: Every icon button and image element includes concrete semantic `contentDescription` strings.

---

## 4. UI States

Every feature view MUST handle all 6 primary states explicitly:
1. **LOADING**: Progress indicator with descriptive activity message.
2. **EMPTY**: Informative explanation with primary user call-to-action.
3. **SUCCESS**: High-density, legible presentation of data.
4. **ERROR**: Human-readable error explanation with retry capability.
5. **PARTIAL / PROCESSING**: Progress indicator showing processed vs remaining items.
6. **REVOKED / DENIED**: Clear recovery instructions directing user to Android permissions settings.
