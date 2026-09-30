KANO — UI REFINEMENT LOOP ONLY

IMPORTANT:
DO NOT REBUILD THE APP.
DO NOT REWRITE THE EXISTING APP ARCHITECTURE.
DO NOT START THE UI FROM ZERO.

The new UI is already implemented.
Now CONTINUE FROM THE CURRENT UI and refine/polish it according to these requirements.

==================================================
1. MAIN GOAL
==================================================

Keep the current implemented Kano UI structure and functionality.

Refine:

- visual quality
- spacing
- hierarchy
- navigation
- footer
- glassmorphism
- animations
- responsiveness
- consistency
- usability

Do not unnecessarily replace working screens/components.

==================================================
2. LIQUID GLASS / GLASSMORPHISM
==================================================

Make the UI feel like premium liquid glass inspired by modern
iPhone/Apple glass interfaces, BUT DO NOT COPY APPLE.

Use:

- translucent surfaces
- layered transparency
- soft background blur where practical
- subtle highlights
- soft reflections
- thin borders
- depth
- soft shadows
- ambient color diffusion
- gentle light movement

Glass should feel fluid and dimensional rather than simply
a transparent card.

Use glass especially for:

- footer/navigation
- floating controls
- search
- important buttons
- sheets
- dialogs
- contextual controls

Do NOT put glass on everything.

==================================================
3. GLASS SETTING
==================================================

ONLY ONE GLASS SETTING:

Glass OFF

NO:
Glass Auto
Glass On
Glass modes
multiple glass selectors

Normal Kano appearance:
liquid glass enabled.

When Glass OFF is enabled:
replace glass surfaces with clean opaque premium surfaces.

Keep:
same layout
same functionality
same animations
same hierarchy

==================================================
4. FOOTER / NAVIGATION — IMPORTANT
==================================================

CHANGE THE CURRENT FOOTER COMPLETELY.

Do not use the current large rectangular/card-like footer.

Create a compact floating LIQUID GLASS navigation bar.

It should feel:

- floating
- translucent
- rounded
- lightweight
- premium
- spatial
- subtle
- iPhone-inspired in polish, NOT copied

Use only the essential primary navigation actions.

Example:

Home
Device
Media
Vault
Style
Care

Secondary/advanced controls should NOT overcrowd the footer.

Move secondary options into the hamburger menu.

Footer must:

- stay compact
- never cover content
- never clip content
- respect gesture area
- respect system insets
- work with 3-button navigation
- work with gesture navigation

Active button should have a subtle liquid-glass highlight/pill,
not a huge colored block.

==================================================
5. HAMBURGER MENU
==================================================

Keep secondary controls inside a clean glass drawer/sheet.

Examples:

Settings
Themes
Appearance
Glass OFF
Reduce Motion
Privacy
Permissions
AI Providers
Notifications
Diagnostics
About

Do not put everything in the footer.

==================================================
6. REMOVE UNNECESSARY UI
==================================================

Remove anything that does not provide real value.

Especially remove:

- fake decorative circle/orb
- empty placeholder cards
- fake AI widgets
- fake statistics
- redundant labels
- unnecessary sections
- decorative buttons
- inactive controls
- repeated information
- visual filler

DO NOT add decorative elements just to make the UI look futuristic.

Whitespace is acceptable.

==================================================
7. NO FAKE AI
==================================================

Do not add artificial AI visuals merely for appearance.

Only show AI visuals when a REAL AI state exists.

Valid states:

IDLE
LISTENING
THINKING
SEARCHING
ACTING
SUCCESS
ERROR
PERMISSION REQUIRED

No fake:
"AI thinking"
"AI analyzing"
"AI optimized"
unless the underlying application is actually doing that.

==================================================
8. ANIMATIONS — KEEP AND EXPAND
==================================================

Animations are REQUIRED.

Do not remove the existing animation system.

Improve it across the app.

Use meaningful animations for:

APP OPEN
screen transitions
navigation
footer selection
hamburger opening
hamburger closing
button press
toggle changes
card expansion
card collapse
lists
loading
success
error
dialogs
sheets
search
gallery
image opening
image closing
image transitions
storage changes
RAM changes
battery changes
camera
scanning
OCR
QR
Style
Care
Vault

Use subtle:

spring motion
fade
scale
slide
shape morphing
shared visual continuity
number interpolation
soft blur transitions
liquid movement
ambient gradients
wave motion

==================================================
9. LIQUID GLASS ANIMATION
==================================================

The glass should feel alive but subtle.

Add small visual motion such as:

- moving highlight
- soft light reflection
- subtle gradient movement
- gentle surface response
- smooth active-state transition
- fluid expansion/collapse

Do NOT make the entire screen constantly move.

==================================================
10. MICRO-INTERACTIONS
==================================================

Every interaction should have appropriate feedback.

Examples:

Button:
small press scale + smooth release.

Toggle:
thumb movement + subtle glow/track transition.

Footer:
active icon smoothly moves into active glass pill.

Card:
small elevation/scale response.

Navigation:
smooth morph/slide.

Success:
subtle confirmation animation.

Error:
subtle shake/highlight only where useful.

==================================================
11. LOADING
==================================================

Improve all loading states.

Use:

skeleton
shimmer
soft placeholder movement
progressive reveal
contextual animation

Do NOT use fake progress.

Do NOT use generic spinner everywhere.

==================================================
12. WAVES / ORGANIC MOTION
==================================================

Keep subtle Kano wave/organic backgrounds where appropriate.

Use:

soft curves
slow gradients
transparent layers
very slow motion

Do not let them compete with content.

Respect Reduce Motion.

==================================================
13. HOME CLEANUP
==================================================

Keep Home minimal.

Remove the fake central circle.

Keep only:

KANO
time/date
real search/command
important real information
important shortcuts

Do not fill empty space with fake widgets.

==================================================
14. RESPONSIVE POLISH
==================================================

Primary device:

OnePlus Nord CE 5
Android 16

Fix all UI sizing issues.

NO:

clipping
overflow
vertical text
hidden content
footer covering content
oversized navigation
giant cards
hardcoded screen heights

Support:

small width
normal width
large width
large font
200% font
gesture navigation
3-button navigation

==================================================
15. EXISTING FUNCTIONALITY
==================================================

Do not replace functioning backend logic.

Every existing visible UI action must remain connected to its
real implementation.

Do not create new fake actions.

Do not leave:

onClick = {}

dead buttons
dead navigation
fake toggles
placeholder interactions

If an existing action is intentionally unavailable,
show a truthful unavailable state.

==================================================
16. ALL SCREENS
==================================================

Apply the SAME refinement across the existing UI:

Home
Device
Storage
RAM
Battery
Connectivity
Apps
Diagnostics
Media
Gallery
Photo Detail
Video Detail
Cleanup
Vault
Style
Dress Me
Wardrobe
Care
Product Scan
Shopping
AI Search
Privacy
Settings
Themes
About

Do not redesign backend logic.

Only improve presentation, interaction, animation and navigation.

==================================================
17. DESIGN LANGUAGE
==================================================

Target:

premium
minimal
liquid
spatial
glass
soft
modern
intelligent
clean

Use Stitch as visual direction and my supplied screenshots as
reference.

Do not copy the source designs.

==================================================
18. PERFORMANCE
==================================================

Keep glass and animation performant.

Avoid:

heavy blur everywhere
constant animation
expensive shaders everywhere
large bitmap retention
unnecessary recomposition

Pause/reduce expensive animation when the screen is not visible.

Reduce Motion must simplify nonessential animation.

==================================================
19. UI VALIDATION LOOP
==================================================

For each screen:

INSPECT CURRENT UI
→ MAKE SMALL COHERENT REFINEMENT
→ BUILD
→ RUN
→ SCREENSHOT
→ VISUAL CHECK
→ FIX
→ RETEST

Do not replace the whole project.

Do not regenerate already-working functionality.

==================================================
20. FINAL UI QUALITY CHECK
==================================================

Confirm:

[ ] Current UI preserved
[ ] No unnecessary rebuild
[ ] Liquid glass implemented
[ ] Glass OFF only
[ ] No Auto/On glass options
[ ] Footer redesigned
[ ] Footer is compact
[ ] Footer uses liquid glass
[ ] Hamburger contains secondary controls
[ ] Fake circle removed
[ ] Fake UI removed
[ ] Fake AI visuals removed
[ ] Animations improved everywhere
[ ] Micro-interactions work
[ ] Loading states polished
[ ] Light mode works
[ ] Dark mode works
[ ] Responsive on OnePlus
[ ] Large text works
[ ] Reduce Motion works
[ ] No clipping
[ ] No overflow
[ ] No dead buttons
[ ] No unnecessary cards
[ ] All existing real actions still work

==================================================
FINAL LOOP
==================================================

DO NOT REBUILD.

REFINE THE CURRENT IMPLEMENTATION:

INSPECT
→ IDENTIFY UI PROBLEM
→ FIX
→ ANIMATE
→ POLISH
→ BUILD
→ RUN
→ SCREENSHOT
→ REVIEW
→ FIX
→ RETEST
→ CONTINUE

Continue through the ENTIRE APP.

Do not stop after Home.

Keep output low-token:

DONE
CHANGED
TESTED
ISSUES
NEXT