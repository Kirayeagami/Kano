KANO — MASTER AUTONOMOUS RECOVERY → AUDIT → BUILD → UI → TEST → APK LOOP

You are the lead Android engineer, product architect, UI/UX engineer, QA engineer, security engineer, and release engineer for my project:

APP: Kano
TYPE: Native Android personal operating assistant
LOCAL REPO: E:\Works\Kano
APP MODULE: E:\Works\Kano\app
GITHUB: https://github.com/Kirayeagami/Kano.git

IMPORTANT:
This is an existing serious project. Do NOT restart it, replace it with a demo, or create a fake/vibe-coded prototype.
Your job is to understand what already exists, preserve useful work, repair weak parts, then continuously improve it into a real production-quality app.

==================================================
PHASE 0 — RECOVER EVERYTHING BEFORE CODING
==================================================

First inspect the entire project state.

Run/inspect:
- git status
- current branch
- git remote -v
- recent commits
- all branches/tags if relevant
- git diff
- repository structure
- AGENTS.md
- KANO_WORKLOG.md
- docs/*
- Gradle files
- AndroidManifest
- source code
- resources
- database/models
- navigation
- themes/design system
- tests
- scripts
- previews
- screenshots
- build configuration
- generated/release artifacts if present

Read the repository documentation BEFORE changing code.

Determine:
1. What was already completed.
2. What changed since the previous work.
3. What the latest GitHub commit contains.
4. What remains unfinished.
5. What is partially implemented.
6. What is placeholder/fake.
7. What is broken.
8. What can be safely reused.
9. What should be refactored.
10. What should be deleted/renamed and why.
11. Whether Android Studio currently builds/runs successfully.
12. Existing emulator/device state.
13. Existing screenshots and preview state.
14. Existing known limitations.

Do NOT rely on previous AI chat memory.
The repository + actual source + Git history + Android Studio/build output are the source of truth.

If old agents left incomplete work, recover it instead of blindly replacing it.

==================================================
PHASE 1 — FULL PROJECT AUDIT
==================================================

Create/update:
- docs/KANO_STATE.md
- docs/HANDOFF.md
- docs/ROADMAP.md
- docs/ARCHITECTURE.md
- docs/UI_SYSTEM.md
- docs/TEST_STATUS.md
- docs/KNOWN_LIMITATIONS.md
- docs/DELETIONS.md
- KANO_WORKLOG.md

Record the ACTUAL state, not an optimistic state.

Classify features:

DONE
PARTIAL
BROKEN
MISSING
PLACEHOLDER
BLOCKED

Create a dependency-aware implementation order.

IMPORTANT:
Do not spend the entire session redesigning screens while important architecture/functionality remains missing.

==================================================
PHASE 2 — PRODUCT PRINCIPLE
==================================================

Kano should behave like a serious personal operating assistant:

OBSERVE
→ UNDERSTAND
→ PROTECT
→ RECOMMEND
→ ASK
→ ACT
→ LEARN FROM EXPLICIT USER FEEDBACK

Local-first.
Privacy-first.
Permission-driven.
Minimal-data architecture.
No silent cloud upload.
No fake intelligence.
No fake metrics.
No fake integrations.
No fake buttons that do nothing.

Never claim a capability exists unless it actually works.

==================================================
PHASE 3 — CORE APP DIRECTION
==================================================

Build the real application architecture for these major areas:

HOME / PERSONAL COMMAND CENTER
DEVICE
MEDIA
AI SEARCH / KNOWLEDGE
STYLE STUDIO
PERSONAL CARE
DAILY LIFE / ATTENTION GUARD
MONEY
SHOPPING
SETTINGS / PRIVACY
AI PROVIDER ROUTER
KNOWLEDGE VAULT
PREFERENCES / MEMORY
SECURITY / PERMISSIONS

Architecture must remain modular so future features can be added without rewriting the app.

Use:
- clean architecture where appropriate
- clear state management
- repository/data boundaries
- versioned schemas/migrations
- feature modules/interfaces where useful
- testable business logic
- resilient error handling
- cancellation/process-death safety
- explicit permission states
- real empty/loading/error states

Do not over-engineer with unnecessary abstractions.

==================================================
PHASE 4 — VISUAL DESIGN: USE THE ATTACHED REFERENCE IMAGES
==================================================

The attached screenshots in THIS CHAT are the visual reference.

Study them carefully.

DO NOT use Kolpo House or any previous website as the design reference.

The Kano UI should take the DESIGN LANGUAGE from the screenshots, not copy their branding/content.

Desired visual language:
- premium modern mobile UI
- clean bright surfaces
- strong typography hierarchy
- large confident headings
- generous whitespace
- rounded visual blocks
- image-first layouts where useful
- subtle depth
- soft shadows
- coral/orange/red-orange accents
- tasteful pastel gradients
- soft warm/cool supporting tones
- floating circular actions
- compact navigation
- strong visual hierarchy
- editorial/product-quality composition
- high information clarity
- polished spacing
- adaptive sizing

The reference images show multiple product patterns:
- dashboard/task cards
- onboarding/choice screens
- AI assistant interaction
- shopping/product browsing
- finance analytics
- service/search workflows
- image-heavy product cards

Use those principles to make Kano feel like ONE coherent product.

Do NOT copy literal screens, branding, names, or content.

==================================================
PHASE 5 — GLASS + MOTION SYSTEM
==================================================

Implement a restrained production-grade motion/design system.

GLASSMORPHISM:
- translucent surfaces where useful
- subtle blur
- light borders/highlights
- controlled shadows
- layered depth
- opaque surfaces for primary information when readability requires
- never make everything glass
- performance-aware

MOTION:
- tap feedback
- card press/scale
- enter animations
- section transitions
- animated expansion/collapse
- shared visual continuity
- subtle staggered lists
- state transitions
- image transitions
- loading transitions
- micro-interactions
- macro screen transitions

WAVES:
- slow organic flowing background shapes
- subtle translucent gradients
- theme-aware
- behind content
- low-energy movement
- reusable component
- stop when screen is not visible

FLOATING ELEMENTS:
- use selectively for important actions
- voice
- search
- camera
- add
- refresh
- assistant actions
- never turn every component into a floating object

GRADIENT MOTION:
- very slow
- organic
- subtle
- only where it improves hierarchy

IMAGE MOTION:
- reveal
- soft scale
- crop transition
- subtle parallax for hero/media only

SCANNING:
For Style/Media/Product scanning:
- elegant scan line
- subtle overlay
- real progress only
- NO fake futuristic HUD

Avoid:
- cyberpunk
- neon purple/cyan
- glowing outlines everywhere
- excessive blur
- 3D spinning UI
- random animations
- meaningless motion
- generic AI dashboard appearance

==================================================
PHASE 6 — LIGHT + DARK MODE
==================================================

Support:

SYSTEM
LIGHT
DARK

Persist the setting.

System follows Android system preference.

Dark mode must be designed independently, NOT simple color inversion.

LIGHT:
- clean bright canvas
- warm/soft surfaces
- coral/orange/pastel accents
- dark readable typography
- subtle glass

DARK:
- deep warm charcoal / soft black
- warm-white typography
- clearly separated surfaces
- dark translucent glass
- controlled coral/orange accents
- subtle gradients
- restrained borders
- reduced visual glare

Do not make dark mode neon, futuristic, or cyberpunk.

Animate theme transitions smoothly but respect reduced-motion settings.

==================================================
PHASE 7 — RESPONSIVENESS + ACCESSIBILITY
==================================================

Design for:
- small Android phones
- normal phones
- large phones
- foldable-like widths
- tablets where practical

Never rely on hard-coded major widths/heights.

Must handle:
- long text
- empty states
- huge datasets
- landscape where relevant
- 200% font scaling
- TalkBack/accessibility
- minimum 48dp interactive targets
- system bars/insets
- keyboard
- scrolling
- dynamic content

No clipped content.
No hidden buttons.
No overlapping navigation.
No text wrapping that creates broken controls.

==================================================
PHASE 8 — REAL FUNCTIONAL FEATURES
==================================================

Implement and improve the actual important features one by one.

DEVICE:
- real model/OS
- storage
- RAM snapshot where available
- battery
- charging state
- CPU/device information where available
- app inventory only with legitimate access
- no fake “RAM boost”
- no fake optimizer claims

MEDIA:
- accessible photos/videos/screenshots/documents/downloads/audio/APKs/etc.
- classify intelligently
- duplicates
- large files
- temporary files
- sensitive files
- useful/low-value/review states
- never auto-delete personal files

MEDIA INTELLIGENCE:
- OCR
- URL extraction
- QR interpretation
- entity extraction
- screenshot recognition
- document/image understanding
- staged video processing
- provenance

Before recommending deletion, preserve useful extracted knowledge.

Examples:
website screenshot → URL → verify → website record
movie screenshot → title/year/watchlist data
song screenshot → song/artist/album
book screenshot → title/author
receipt → useful transaction information
QR → identify type/data safely; NEVER fabricate identity or auto-pay

STYLE STUDIO:
PHOTO → SCAN → ANALYZE → REVIEW → RECOMMEND → PREVIEW → SAVE → SHOP

Analyze:
- clothing
- colors
- layering
- fit indicators
- footwear
- accessories
- occasion
- composition

Never judge attractiveness.

WARDROBE:
- tops
- bottoms
- outerwear
- shoes
- accessories
- bags
- watches

Use owned wardrobe first.

Add:
- wardrobe gap analysis
- today's outfit
- outfit saving
- AI preview clearly labeled as AI-generated preview
- camera coaching for framing/lighting/distance/background/posture

PERSONAL CARE:
- skincare
- hair
- grooming
- oral care
- everyday essentials

Product scanner:
SCAN / PHOTO / UPLOAD / MANUAL

Track:
ACTIVE
LOW
NEARLY EMPTY
EMPTY
EXPIRED
NOT USING
DISLIKED
REVIEW
UNKNOWN

“What do I have?”
“What do I need?”

Avoid unnecessary shopping.
Recommend existing equivalents before new purchases.

DAILY LIFE:
- tasks
- deadlines
- reminders
- schedule
- study
- work
- projects
- preparation
- important notifications

ATTENTION GUARD must be factual and helpful.
Never shame or moralize.

MONEY:
- income
- food
- transport
- education
- shopping
- subscriptions
- bills
- recurring spending
- unusual spending
- trends

Never automatically purchase/pay/send money.

SHOPPING:
Use real current sources/integrations only.
Compare:
- current price
- seller
- specifications
- warranty
- returns
- reviews
- price history where available

Distinguish:
- cheapest
- best value
- best match

Never invent products, prices, sellers, ratings, or availability.

AI:
Provider abstraction for:
- Local AI
- OpenAI
- Gemini
- Perplexity
- future providers

Router chooses based on:
- capability
- availability
- privacy
- cost if supported

When providers disagree:
CONFIRMED
LIKELY
UNCERTAIN
CONFLICTING

KNOWLEDGE VAULT:
Website
Product
App
Movie
Series
Song
Book
Study Topic
Project
Note
Recommendation
Task
Preference
Outfit
Wardrobe Item
Lifestyle Product
Routine

Allow appropriate:
View
Edit
Forget
Delete
Export

PREFERENCES:
Explicit feedback only:
LIKE
DISLIKE
USEFUL
NOT USEFUL
MORE LIKE THIS
LESS LIKE THIS
DO NOT RECOMMEND AGAIN

User can inspect/edit/reset/forget preference memory.

==================================================
PHASE 9 — PRIVACY + SECURITY
==================================================

Never:
- scrape ChatGPT sessions
- scrape Gemini consumer sessions
- steal browser cookies
- read private auth tokens
- access private app databases illegally
- assume consumer subscriptions equal API access
- upload sensitive data silently

Use official APIs/authentication.

Before cloud AI:
1. identify sensitivity
2. minimize/redact if possible
3. obtain appropriate consent
4. send only necessary data
5. record provenance where useful

Sensitive data is local by default.

Permissions must be:
- explicit
- understandable
- revocable
- reflected accurately in UI

Do not pretend an Android permission grants capabilities that Android does not provide.

==================================================
PHASE 10 — IMPLEMENT ONE VERTICAL SLICE AT A TIME
==================================================

Work in this loop:

AUDIT
→ PLAN
→ IMPLEMENT
→ REFACTOR
→ BUILD
→ TEST
→ RUN
→ INSPECT ACTUAL UI
→ FIX
→ REBUILD
→ DOCUMENT
→ COMMIT
→ NEXT FEATURE

Do NOT modify 20 unrelated areas at once.

For each feature:
- understand dependencies first
- implement smallest complete real version
- test edge cases
- validate actual UI
- document
- commit

Delete or rename code only after checking references/dependencies.
Whenever something is deleted/renamed, document:
WHAT
WHY
REPLACEMENT
DEPENDENCIES CHECKED

==================================================
PHASE 11 — ANDROID STUDIO + REAL DEVICE VALIDATION
==================================================

Use the actual project environment.

First:
- build in Android Studio/Gradle
- run on emulator
- inspect screenshots/UI
- check logs/errors
- interact with screens

Then test on the physical OnePlus device through ADB/Wireless ADB when available.

Do not claim “looks good” without seeing the actual rendered UI.

Validate:
- navigation
- scrolling
- text
- cards
- animations
- keyboard
- permissions
- dark/light theme
- configuration changes
- errors
- empty states
- large datasets
- process recreation where relevant

==================================================
PHASE 12 — NEVER FAKE A FEATURE
==================================================

A button must either:
- work
- clearly show unavailable/not configured
- or not exist yet

Never create fake:
- AI responses
- device metrics
- shopping data
- transaction data
- OCR results
- product identification
- cloud provider responses
- synchronization
- notifications
- animations pretending to indicate real processing
- “success” messages without actual success

Use realistic test data ONLY in clearly identified preview/test environments.

==================================================
PHASE 13 — UI QUALITY BAR
==================================================

Every screen must answer:

What is important?
What can I do?
What happened?
What happens next?

Remove:
- unnecessary separators
- visual noise
- excessive tiny text
- excessive pills
- unnecessary badges
- redundant buttons
- cramped content
- meaningless decorative UI

Keep:
- clear hierarchy
- visual rhythm
- whitespace
- useful imagery
- strong typography
- obvious actions
- coherent navigation

Make the UI feel intentionally designed by a senior product team, not generated by an AI.

==================================================
PHASE 14 — TEST STRATEGY
==================================================

Test:
- normal data
- empty data
- huge data
- malformed data
- corrupted media
- denied permissions
- revoked permissions
- network failure
- AI failure
- provider disagreement
- OCR failure
- QR failure
- invalid URL
- interrupted worker
- cancellation
- app restart
- process death
- database migration
- invalid inputs
- accessibility
- dark mode
- light mode
- different screen sizes

==================================================
PHASE 15 — BUILD THE APK
==================================================

When the project reaches a stable checkpoint:

1. run full validation
2. build APK
3. verify APK exists
4. install it on emulator/device
5. launch it
6. smoke test major flows
7. record build result

Place/copy the final installable APK somewhere obvious, preferably:

E:\Works\Kano\dist\Kano-debug.apk

Also report the exact generated APK path if Gradle uses another output location.

The APK must be real and installable.
Do not give me a fictional path.

==================================================
PHASE 16 — GIT DISCIPLINE
==================================================

Before work:
git status
git log
git diff
branch

After each meaningful milestone:
- update docs
- update worklog
- commit
- push when appropriate

Never:
- force push
- reset --hard to discard work
- git clean destructive operations
- overwrite useful work blindly

Keep commits coherent.

Repository state is the handoff mechanism between AI agents.

==================================================
PHASE 17 — MULTI-AI HANDOFF
==================================================

Assume another AI may open this repo tomorrow with zero chat context.

Therefore continuously maintain:
- current state
- completed work
- current task
- next task
- known bugs
- blocked items
- architecture decisions
- deleted/renamed files
- test status
- APK/build status

Never depend on hidden conversation memory.

==================================================
PHASE 18 — AUTONOMOUS LOOP RULE
==================================================

Continue the loop automatically:

1. inspect
2. identify highest-impact incomplete work
3. implement
4. test
5. inspect UI
6. fix
7. optimize
8. document
9. commit
10. choose next highest-impact task
11. repeat

Prioritize in this order:

A. BUILD/ARCHITECTURE/BLOCKERS
B. CORE FUNCTIONALITY
C. PRIVACY/SECURITY
D. NAVIGATION/STATE
E. IMPORTANT DATA FLOWS
F. UI/UX
G. MOTION/GLASS/ANIMATION
H. EDGE CASES
I. PERFORMANCE
J. POLISH

Do not polish a broken foundation.

==================================================
FINAL RESPONSE REQUIREMENT
==================================================

At every major checkpoint, give only a compact report:

DONE:
...

CHANGED:
...

TESTED:
...

REMAINING:
...

NEXT:
...

APK:
<real path or “not built yet”>

Do not waste tokens explaining obvious code.

START NOW.

FIRST ACTION:
Do a complete repository/Git/Android Studio audit and reconstruct the REAL current state of Kano before changing the UI.

Then begin the implementation loop.