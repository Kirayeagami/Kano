KANO 2.0 — MASTER AUTONOMOUS RECOVERY → CORE BUILD → INTEGRATION → UI → MOTION → TEST → APK LOOP

PROJECT
App: Kano
Platform: Native Android
Repo: E:\Works\Kano
Module: E:\Works\Kano\app
GitHub: https://github.com/Kirayeagami/Kano.git

ROLE

Act as the lead:
- Android engineer
- software architect
- product engineer
- UI/UX designer
- motion designer
- privacy/security engineer
- QA engineer
- performance engineer
- release engineer

Operate as a senior production engineering team, not as a demo generator.

Kano is an existing project.
DO NOT restart the project.
DO NOT replace working architecture unnecessarily.
DO NOT create fake functionality.
DO NOT create a visually impressive shell around unfinished logic.
DO NOT use generic AI/vibe-coded patterns.

Your job is to inspect what already exists, understand all previous work, preserve good work, fix weak work, implement missing core functionality, then redesign and polish the entire product.

==================================================
0. SOURCE OF TRUTH / RECOVER PREVIOUS WORK
==================================================

Before writing code, reconstruct the REAL current state.

Inspect:

- git status
- current branch
- git remote
- recent commits
- branches
- git diff
- repository tree
- AGENTS.md
- KANO_WORKLOG.md
- docs/*
- Gradle configuration
- AndroidManifest
- Kotlin/Java source
- Compose/UI code
- themes
- navigation
- database
- models
- repositories
- workers
- services
- permissions
- integrations
- tests
- previews
- scripts
- screenshots
- APK/build outputs
- Android Studio project state
- emulator/device state if available

Read repository documentation before editing.

Determine:

DONE
PARTIAL
BROKEN
MISSING
PLACEHOLDER
BLOCKED

Find what changed recently in GitHub.

Understand:
- what previous agents implemented
- what files were modified
- what UI work is already finished
- what functionality is real
- what is only planned
- what is placeholder
- what is broken
- what dependencies exist
- what can safely be reused
- what should be refactored
- what should be deleted
- what should be renamed

NEVER infer project status from old AI conversation memory when the repository can prove it.

==================================================
1. DOCUMENT THE RECOVERED STATE
==================================================

Create/update:

AGENTS.md
KANO_WORKLOG.md

docs/KANO_STATE.md
docs/HANDOFF.md
docs/ROADMAP.md
docs/ARCHITECTURE.md
docs/UI_SYSTEM.md
docs/PRIVACY_MATRIX.md
docs/SECURITY_MODEL.md
docs/AI_PROVIDERS.md
docs/DECISIONS.md
docs/TEST_STATUS.md
docs/KNOWN_LIMITATIONS.md
docs/DELETIONS.md

Keep these documents continuously updated.

Another AI must be able to open the repository with ZERO chat history and continue correctly.

==================================================
2. IMPLEMENTATION PRIORITY — IMPORTANT
==================================================

DO NOT start with visual polishing.

Work in this order:

PHASE A
BUILD + ARCHITECTURE + BLOCKERS

PHASE B
CORE DATA + STATE + NAVIGATION + PERMISSIONS

PHASE C
IMPORTANT REAL FEATURES

PHASE D
AI / GOOGLE / DEVICE / MEDIA INTEGRATIONS

PHASE E
SECURITY + PRIVACY

PHASE F
PERFORMANCE + RELIABILITY

PHASE G
MAJOR UI REDESIGN

PHASE H
GLASSMORPHISM + MOTION + ANIMATION

PHASE I
RESPONSIVE + ACCESSIBILITY

PHASE J
FULL DEVICE VALIDATION

PHASE K
APK RELEASE

Do not spend hours polishing a screen while the underlying feature does not work.

==================================================
3. KANO PRODUCT PRINCIPLE
==================================================

Kano should work as a real personal operating assistant:

OBSERVE
→ UNDERSTAND
→ PROTECT
→ RECOMMEND
→ ASK
→ ACT
→ LEARN FROM EXPLICIT USER FEEDBACK

Core principles:

LOCAL-FIRST
PRIVACY-FIRST
PERMISSION-FIRST
MINIMUM-DATA
FAST
SECURE
RELIABLE
MODULAR
EXTENSIBLE
TRANSPARENT

Never fabricate capability.

==================================================
4. DEVICE ACCESS / PERMISSION MODEL
==================================================

Kano should provide the user with as much useful device information and functionality as Android legitimately allows.

However:

DO NOT bypass Android security.
DO NOT root the phone.
DO NOT exploit private APIs.
DO NOT read private databases of other apps.
DO NOT steal cookies/tokens.
DO NOT silently access protected information.

Use official Android APIs and permissions.

When a capability needs permission:

Explain:
WHAT
WHY
WHAT DATA IS ACCESSED
WHERE IT IS USED
HOW TO REVOKE IT

Ask for permission at the appropriate moment.

Never request every permission on first launch just because it is available.

Create a clear permissions/privacy center where the user can see:

GRANTED
NOT GRANTED
OPTIONAL
REQUIRED
REVOKED

After login/authorization, unlock only the functionality that is genuinely available.

Without permissions:
show device/basic app information that Android allows.

==================================================
5. GOOGLE ACCOUNT / GOOGLE DRIVE
==================================================

Implement official Google authentication/OAuth only.

Desired experience:

SIGN IN WITH GOOGLE
→ authenticated Google account
→ show authorized Google services
→ connect supported services without unnecessary repeated login

Use one authenticated account where Google APIs legitimately support the same authorization/session.

Do NOT assume one login magically grants every Google capability.

Request only the scopes actually required.

Drive integration:
- browse authorized files
- search
- inspect metadata
- upload/download only when explicitly requested
- respect Drive permissions
- never expose private files without authorization

Show account and connected-service status inside Kano.

==================================================
6. AI PROVIDER LOGIN / CONNECTION CENTER
==================================================

Create a proper:

AI & SERVICES CONNECTION CENTER

Possible providers:
- OpenAI
- Gemini
- Perplexity
- local AI
- future providers

Use official supported authentication/API methods only.

IMPORTANT:

A consumer ChatGPT subscription does NOT automatically mean API access.

A Google login does NOT automatically grant every Gemini API capability.

Only enable a provider when actual supported authentication/API access exists.

Never simulate successful login.

Never create fake provider responses.

Show:

CONNECTED
NOT CONNECTED
AUTHORIZATION REQUIRED
API NOT AVAILABLE
ERROR
DISCONNECTED

The user can disconnect a provider and revoke access.

==================================================
7. DATA PRIVACY
==================================================

DEFAULT:
Private data stays local whenever practical.

Never silently upload:

- personal photos
- videos
- documents
- messages
- financial information
- private files
- contacts
- notifications
- passwords
- authentication tokens
- sensitive content

Before cloud AI processing:

1. determine sensitivity
2. minimize data
3. redact where possible
4. ask for required consent
5. send only what is needed
6. clearly show where it goes
7. store provenance when appropriate

Never sell/share/distribute user data.

Never send data to an unrelated third party.

Never use private user data for hidden training/marketing.

==================================================
8. SECURITY
==================================================

Protect:
- OAuth credentials
- API credentials
- tokens
- local secrets
- private indexes
- sensitive metadata

Use Android Keystore where appropriate.

Never hardcode secrets.

Never put API keys in source control.

Never expose authentication tokens through logs.

Use secure storage and least privilege.

==================================================
9. CORE KANO SECTIONS
==================================================

Build the actual product around:

HOME
DEVICE
MEDIA
AI SEARCH
KNOWLEDGE VAULT
STYLE STUDIO
PERSONAL CARE
DAILY LIFE
ATTENTION GUARD
MONEY
SHOPPING
SETTINGS
PRIVACY
AI PROVIDERS
PREFERENCES

Every section must have a meaningful purpose.

Each section can have its own visual language while still clearly belonging to Kano.

==================================================
10. CORE FUNCTIONALITY
==================================================

DEVICE

Real information only:
- device model
- Android version
- storage
- battery
- charging
- memory information where available
- CPU/device information where Android permits
- installed-app inventory only with legitimate APIs/access

Do not create fake optimization numbers.

Do not claim that killing apps magically creates permanent RAM.

MEDIA

Support practical local media workflows for accessible content:

- photos
- videos
- screenshots
- documents
- downloads
- audio
- APK files
- archives
- duplicates
- large files
- temporary files
- sensitive files

Classification:

KEEP
USEFUL
REVIEW
LOW VALUE
DUPLICATE
TEMPORARY
SENSITIVE
UNKNOWN

Never auto-delete personal files.

MEDIA INTELLIGENCE

Where technically and legally supported:

- OCR
- URL extraction
- QR interpretation
- entity extraction
- image classification
- screenshot understanding
- staged video analysis
- document extraction

Preserve extracted knowledge before deletion recommendations.

STYLE STUDIO

PHOTO
→ SCAN
→ ANALYZE
→ REVIEW
→ RECOMMEND
→ PREVIEW
→ SAVE
→ SHOP

Analyze:
- clothing
- colors
- layering
- fit indicators
- shoes
- accessories
- occasion
- composition

Do not judge attractiveness.

WARDROBE:

TOPS
BOTTOMS
OUTERWEAR
SHOES
ACCESSORIES
BAGS
WATCHES

Use wardrobe inventory before recommending purchases.

PERSONAL CARE

Categories:
- skincare
- hair
- grooming
- oral
- everyday essentials

Product scanner:
SCAN
PHOTO
UPLOAD
MANUAL

Track real inventory state:

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

Avoid unnecessary consumption.

DAILY LIFE

- tasks
- schedule
- reminders
- deadlines
- study
- work
- projects
- preparation
- important notifications

ATTENTION GUARD must be factual and useful.
Never shame the user.

MONEY

Where legitimate authorized data is available:

- income
- spending
- food
- transport
- education
- subscriptions
- bills
- recurring expenses
- unusual spending
- trends

Never automatically transfer/pay/purchase.

SHOPPING

Use real current data only when supported.

Show:
- product
- seller
- price
- specifications
- availability
- warranty
- returns
- reviews
- price history where actually available

Never invent shopping information.

AI SEARCH

Create a universal search layer over authorized sources.

Possible sources:
- local files
- media
- knowledge vault
- notifications
- email
- browser integrations
- connected providers
- authorized external services

Always provide provenance.

==================================================
11. KNOWLEDGE VAULT
==================================================

Support structured entities:

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

Where appropriate provide:

VIEW
EDIT
FORGET
DELETE
EXPORT

==================================================
12. EXPLICIT MEMORY / PREFERENCES
==================================================

Only learn preferences through legitimate explicit signals.

Support:

LIKE
DISLIKE
USEFUL
NOT USEFUL
MORE LIKE THIS
LESS LIKE THIS
DO NOT RECOMMEND AGAIN

Provide a user-visible preference manager.

User can:
- inspect
- edit
- forget
- reset

==================================================
13. UI DESIGN — AFTER CORE FUNCTIONALITY
==================================================

Use the reference screenshots attached to this conversation as the primary visual direction.

Do NOT use Kolpo House as visual inspiration.

Do not copy the screenshots literally.

Extract their design principles:

- premium mobile composition
- clean canvas
- confident typography
- strong hierarchy
- large rounded cards
- beautiful imagery
- soft gradients
- coral/orange accents
- pastel secondary colors
- floating actions
- compact navigation
- whitespace
- depth
- subtle shadows
- excellent spacing
- polished product presentation

Kano must feel like one carefully designed premium product.

==================================================
14. EACH SECTION GETS ITS OWN VISUAL LANGUAGE
==================================================

Do not make every page identical.

HOME:
warm premium command-center design

DEVICE:
technical but clean

MEDIA:
image/content-first

STYLE:
fashion/editorial

PERSONAL CARE:
soft lifestyle/product design

MONEY:
clean analytical design

SHOPPING:
premium commerce design

AI:
minimal intelligent interaction design

KNOWLEDGE:
editorial/library design

SETTINGS:
quiet utilitarian design

Each section can use different accent colors, imagery and motion patterns while sharing:

- typography system
- spacing system
- icon language
- navigation principles
- accessibility
- interaction rules

==================================================
15. DARK MODE — NOTHING-INSPIRED DIRECTION
==================================================

Take inspiration from the visual philosophy of Nothing UI:

- strong monochrome foundation
- black/white contrast
- extremely clean composition
- restrained visual language
- technical precision
- minimal typography
- geometric details
- subtle information density
- controlled accent color
- understated motion

But DO NOT COPY:
- Nothing logos
- exact layouts
- proprietary glyph designs
- branding
- text
- product UI
- trademark-specific visuals

Combine that restrained dark design philosophy with Kano's own identity.

DARK MODE:

deep black / warm charcoal
warm-white text
subtle tonal layers
controlled coral/red-orange accent
very limited secondary color
subtle glass
soft gradients
minimal glow
clean technical details

Do NOT make it:
- cyberpunk
- neon
- purple/cyan gamer UI
- glowing HUD
- sci-fi dashboard

==================================================
16. LIGHT MODE
==================================================

LIGHT MODE should feel:

bright
premium
airy
modern
warm
clean

Use:
- soft whites
- light gray surfaces
- pastel gradients
- coral/orange accents
- subtle shadows
- restrained glass

==================================================
17. THEME MODES
==================================================

Provide:

SYSTEM
LIGHT
DARK

Persist user's selection.

SYSTEM follows Android setting.

Do not simply invert colors.

Create separate design tokens for light/dark:
- surfaces
- text
- borders
- shadows
- glass opacity
- gradient colors
- waves
- icons
- navigation
- system bars

==================================================
18. OPTIONAL “GLASS MODE”
==================================================

Add a user-facing visual preference:

GLASS MODE
ON / OFF

When ON:
use tasteful translucent/glass surfaces, blur, soft borders and depth where performance permits.

When OFF:
use cleaner opaque premium surfaces.

This is a visual preference, NOT a fake “performance” feature.

Persist the preference.

Respect accessibility and reduced motion.

==================================================
19. GLASSMORPHISM
==================================================

Use glass as an enhancement, not the entire application.

Good glass:
- translucent card
- soft blur
- subtle highlight
- faint border
- controlled shadow
- background depth

Avoid:
- every component being glass
- heavy blur
- unreadable text
- excessive transparency
- performance-heavy effects

Primary information must remain readable.

==================================================
20. RESPONSIVE UI
==================================================

FULL RESPONSIVE DESIGN IS REQUIRED.

Support:
- small phones
- normal phones
- large phones
- foldable-like widths
- tablets where practical
- portrait
- landscape where relevant

No major UI element should disappear because of screen size.

Never hide important options.

Never use hardcoded screen widths for major layout decisions.

Adapt:
- card size
- grid columns
- spacing
- typography
- content density
- navigation
- imagery
- controls

Handle:
- long text
- empty content
- large content
- keyboard
- insets
- accessibility font sizes
- 200% font scale
- TalkBack

Minimum touch target:
48dp.

==================================================
21. PREMIUM ANIMATION SYSTEM
==================================================

Animation must improve:
- hierarchy
- feedback
- continuity
- perceived quality
- comprehension

Not simply exist everywhere.

Use:

MICRO:
tap
toggle
press
selection

CARD:
fade
small vertical movement
soft scale
expansion
collapse
state changes

MACRO:
screen transitions
shared-element-like continuity
section changes
navigation
large image transitions

AMBIENT:
slow gradients
soft floating elements
waves
background movement

PROCESSING:
QUEUED
INDEXING
PROCESSING
COMPLETE
FAILED

Use real state for real processing.

Never animate a fake process to imply work is happening.

==================================================
22. ANIMATION QUALITY
==================================================

Prefer:
- spring-based motion where appropriate
- natural easing
- subtle scale
- fade/slide combinations
- shared continuity
- staggered list entrances
- animated gradients
- soft image reveals
- smooth navigation

Avoid:
- dramatic 3D spins
- bouncing every element
- excessive zoom
- constant movement
- distracting parallax
- random animation
- motion without meaning

Use animation tiers:

FAST
interaction feedback

MEDIUM
screen/content/state changes

SLOW
ambient motion

Stop expensive ambient animation when the screen is not visible.

==================================================
23. WAVES / FLOATING ELEMENTS
==================================================

Create a reusable organic wave system.

Use:
- low-frequency curves
- soft gradients
- translucent layers
- very slow motion
- theme-aware variants

Place waves behind content.

Floating elements can be used for important contextual actions:
- search
- voice
- camera
- add
- assistant
- refresh

Do not make every element float.

==================================================
24. SCANNER ANIMATION
==================================================

For:
- Style scanner
- Product scanner
- Media scanning

Use:
- elegant scan line
- subtle illumination
- clean progress state
- realistic processing feedback

No futuristic HUD.
No fake AI scanning animation.

==================================================
25. PERFORMANCE
==================================================

Kano must feel fast.

Optimize:
- recomposition
- image loading
- lazy lists
- database queries
- background work
- bitmap memory
- animations
- blur usage
- networking
- AI processing
- video processing
- battery usage

Avoid:
- unnecessary infinite animations
- giant bitmaps
- excessive background workers
- needless network calls
- blocking the UI thread
- unnecessary recomposition
- expensive shaders everywhere

Measure real performance where possible.

==================================================
26. ACCESSIBILITY
==================================================

Support:
- TalkBack
- content descriptions
- focus order
- scalable text
- high contrast
- touch targets
- reduced motion

Reduced Motion should:
- reduce/disable ambient movement
- reduce parallax
- simplify macro transitions
- retain useful feedback

==================================================
27. FEATURE IMPLEMENTATION LOOP
==================================================

For EACH feature:

AUDIT
→ PLAN
→ IMPLEMENT
→ UNIT TEST
→ BUILD
→ RUN
→ TEST REAL FLOW
→ INSPECT UI
→ FIX
→ OPTIMIZE
→ DOCUMENT
→ COMMIT

Then continue to the next feature.

Never implement ten half-finished features simultaneously.

==================================================
28. CODE QUALITY
==================================================

Keep code:
- readable
- modular
- maintainable
- testable
- documented only where useful
- free of dead code
- free of fake implementations
- free of unnecessary abstractions

Before deleting or renaming:
check all references and dependencies.

Record deletions/renames in:
docs/DELETIONS.md

==================================================
29. FAILURE HANDLING
==================================================

Every real integration must have:

LOADING
EMPTY
SUCCESS
PARTIAL
ERROR
RETRY
PERMISSION DENIED
NOT CONNECTED
UNAVAILABLE

Never hide failures behind fake success UI.

==================================================
30. TEST EVERYTHING
==================================================

Test:

normal data
empty data
huge datasets
malformed files
corrupted media
permission denied
permission revoked
network failure
AI failure
provider disagreement
OAuth failure
expired authorization
OCR failure
QR failure
invalid URL
interrupted work
cancellation
process death
app restart
database migration
theme changes
screen-size changes
large fonts
accessibility

==================================================
31. ANDROID STUDIO VALIDATION
==================================================

Use Android Studio and the actual project.

First:
build

Then:
run emulator

Then:
inspect real rendered UI

Then:
interact with the application

Then:
check logs/errors

Then:
fix

Then:
rebuild

Then:
run again

After emulator validation:
test physical OnePlus device using ADB/Wireless ADB when available.

Do not say “UI looks good” without actually inspecting the rendered result.

==================================================
32. GIT / MULTI-AI WORKFLOW
==================================================

Before modifying:

git status
git branch
git log -n 10
git diff

Only one active agent should modify the working tree at a time.

Use coherent commits.

Push meaningful completed milestones.

Never:
- force push
- git reset --hard to destroy work
- destructive git clean
- blindly overwrite existing implementation

Repository state + documentation = agent-to-agent memory.

==================================================
33. APK REQUIREMENT
==================================================

At stable milestone:

BUILD
→ VERIFY
→ INSTALL
→ LAUNCH
→ SMOKE TEST
→ PACKAGE APK

Create a real APK.

Prefer:

E:\Works\Kano\dist\Kano-debug.apk

But only report a path if the file actually exists.

Also report the actual Gradle generated APK path.

Install the APK on the emulator/OnePlus when possible and verify it launches.

==================================================
34. ABSOLUTE NO-FAKE RULE
==================================================

NEVER invent:

- AI responses
- device data
- RAM numbers
- battery data
- OCR results
- QR contents
- products
- sellers
- prices
- reviews
- transactions
- provider connections
- Google data
- Drive files
- ChatGPT access
- Gemini access
- permissions
- successful operations

A feature is either REAL,
or clearly marked as unavailable/not configured,
or not exposed yet.

==================================================
35. CONTINUOUS AUTONOMOUS LOOP
==================================================

After each completed task:

inspect remaining work
→ identify highest-impact unfinished item
→ implement
→ test
→ fix
→ optimize
→ document
→ commit
→ continue

Do not stop merely because one screen looks finished.

Keep improving until the project reaches the current practical release milestone.

==================================================
36. FINAL REPORT FORMAT
==================================================

Keep reports compact:

DONE
<what was actually completed>

CHANGED
<important files/features>

TESTED
<actual build/device/test evidence>

ISSUES
<real remaining problems>

NEXT
<next highest-priority task>

APK
<real APK path or NOT BUILT>

Do not waste tokens explaining obvious implementation details.

==================================================
START NOW
==================================================

STEP 1:
Fully audit E:\Works\Kano and reconstruct the current state from the repository, GitHub history, documentation, source, build system and Android Studio.

STEP 2:
Determine what I have already completed since the previous AI stopped.

STEP 3:
Create the real current roadmap.

STEP 4:
Fix/build the important underlying functionality first.

STEP 5:
Implement integrations and permission architecture.

STEP 6:
Only after the core foundation is stable, redesign the UI using the attached reference screenshots.

STEP 7:
Add:
- Light/Dark/System
- Nothing-inspired dark visual language
- optional Glass Mode
- premium glassmorphism
- responsive layouts
- section-specific visual identities
- waves
- floating elements
- micro animations
- card animations
- macro transitions
- image transitions
- scanner animations
- reduced-motion support

STEP 8:
Validate on emulator.

STEP 9:
Validate on my OnePlus device.

STEP 10:
Build a REAL installable APK.

DO NOT START BY REDESIGNING RANDOM SCREENS.
FIRST UNDERSTAND THE CURRENT PROJECT.
THEN FINISH THE IMPORTANT THINGS.
THEN MAKE IT BEAUTIFUL.
THEN MAKE IT FAST.
THEN MAKE IT STABLE.
THEN GIVE ME THE REAL APK.