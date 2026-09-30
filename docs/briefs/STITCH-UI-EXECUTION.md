KANO — MASTER STITCH UI + FULL PRODUCT EXECUTION LOOP

PROJECT:
E:\Works\Kano

REPO:
https://github.com/Kirayeagami/Kano.git

STITCH:
https://stitch.withgoogle.com/projects/18143319273405655566?pli=1

ROLE:
Act as Kano's lead product engineer, Android engineer, UI/UX engineer,
AI architect, security engineer and QA engineer.

IMPORTANT:
Use the Stitch project above as the PRIMARY visual reference.
Use my supplied OxygenOS screenshots as the PRIMARY reference for
information depth/system-app completeness.

DO NOT clone/copy Stitch, Apple or OxygenOS.
Create an original Kano design language inspired by them.

==================================================
1. FIRST: AUDIT, DON'T REBUILD BLINDLY
==================================================

Before coding:

git status
git branch
git log -n 15
git diff

Read:

AGENTS.md
KANO_WORKLOG.md
docs/KANO_STATE.md
docs/HANDOFF.md
docs/STATUS.md
docs/FEATURE-CATALOG.md
docs/KNOWN_LIMITATIONS.md
docs/ARCHITECTURE.md
docs/UI_SYSTEM.md
docs/ROADMAP.md
docs/PLAN.md
docs/TEST_STATUS.md
docs/VALIDATION.md
docs/DELETIONS.md
docs/briefs/*

Inspect ALL source, Gradle, manifest, resources, tests and existing UI.

Create a truth table:

DONE
PARTIAL
BROKEN
MISSING
OBSOLETE
DUPLICATED
FAKE/MOCK
PLATFORM-LIMITED

Treat actual code + runtime as truth, not old documentation.

==================================================
2. SAFE CLEANUP
==================================================

Preserve useful existing architecture.

Delete/rename only after checking:

references
dependencies
navigation
tests
resources
database
documentation

Record every deletion in:

docs/DELETIONS.md

Remove obsolete/dead/duplicate code when proven unnecessary.

Do not destroy Room schema history, security code or working repositories.

==================================================
3. MULTI-AI SYNC
==================================================

GITHUB = shared memory/source of truth.

STITCH:
visual design, screen composition, design tokens, interaction concepts.

CODEX:
architecture, implementation, refactoring, tests, documentation.

ANDROID STUDIO:
build, emulator, Logcat, profiler, ADB, runtime validation.

Only one agent edits the working tree at a time.

Synchronize through:

AGENTS.md
KANO_WORKLOG.md
docs/HANDOFF.md
docs/KANO_STATE.md
docs/STATUS.md

If Stitch MCP/integration is available, inspect the actual Stitch project.
If unavailable, use exported Stitch designs/assets/screenshots.

Never invent unavailable Stitch data.

==================================================
4. PRODUCT PRINCIPLE
==================================================

Kano is NOT a normal dashboard.

Kano is a PERSONAL AI OPERATING LAYER.

Core loop:

OBSERVE
→ UNDERSTAND
→ PROTECT
→ RECOMMEND
→ ASK
→ ACT
→ VERIFY
→ LEARN FROM EXPLICIT USER FEEDBACK

UI should adapt to intent instead of always showing every feature.

==================================================
5. STITCH-INSPIRED VISUAL DIRECTION
==================================================

Create:

premium
AI-native
spatial
fluid
clean
glass
minimal
responsive
deep
calm

Use:

large typography
strong hierarchy
soft surfaces
translucent layers
ambient gradients
subtle borders
depth
organic motion
contextual color
floating controls
morphing surfaces

Avoid:

generic SaaS dashboard
card-grid overload
cyberpunk
neon HUD
cheap AI look
giant permanent cards
same black/red treatment everywhere

==================================================
6. GLASSMORPHISM
==================================================

Implement real premium glass where practical:

translucency
background blur where supported
soft highlight
subtle border
depth
shadow
ambient color bleed

Modes:

GLASS OFF
GLASS AUTO
GLASS ON

Do not blur everything.

Glass must remain readable and performant.

Reduce Motion must reduce expensive/nonessential effects.

==================================================
7. LIGHT + DARK + THEMES
==================================================

Support:

SYSTEM
LIGHT
DARK

Themes:

Kano Glass
Soft Editorial
Minimal Mono
Warm Studio
Aurora
Paper
Midnight
Technical

Theme changes must affect:

background
surface
glass
accent
gradient
navigation
shadow
corners
AI visualization

NOT merely one accent color.

==================================================
8. SECTION VISUAL IDENTITIES
==================================================

HOME:
personal / calm / intelligent

DEVICE:
technical / precision / diagnostic

MEDIA:
visual / discovery / gallery

VAULT:
memory / archival / knowledge

STYLE:
fashion / editorial

CARE:
clean / calm / personal

PRIVACY:
trust / security / control

Each section gets its own atmosphere while remaining Kano.

==================================================
9. AI-NATIVE HOME
==================================================

Home should center Kano itself.

Show:

time
context
important information
daily priorities
device state
relevant actions
AI interaction

Create an AI visual surface/orb/wave.

States:

IDLE
LISTENING
THINKING
SEARCHING
ACTING
SUCCESS
ERROR
WAITING
PERMISSION REQUIRED

Do not use generic loading spinners as the primary AI experience.

==================================================
10. GENERATIVE UI
==================================================

Kano should generate a focused interface from user intent.

Examples:

"What is taking up my storage?"
→ storage intelligence UI

"How much RAM am I using?"
→ memory UI

"What's connected?"
→ connectivity UI

"What should I do today?"
→ daily intelligence UI

"Find my saved movies."
→ movie knowledge UI

"Show clothes I own."
→ wardrobe UI

"Find this cheaper."
→ shopping comparison UI

Interfaces can:

expand
collapse
merge
split
morph

Preserve spatial continuity.

==================================================
11. NAVIGATION
==================================================

Replace the current oversized multi-row bottom navigation.

Use:

compact floating dock
or compact pill
or contextual navigation

Never cover content.

Never create vertical text.
Never clip controls.

Use proper WindowInsets/safe drawing regions.

Support gesture navigation and 3-button navigation.

==================================================
12. DEVICE CONTROL CENTER
==================================================

Complete Device section with:

STORAGE
RAM
BATTERY
PERFORMANCE
CONNECTIVITY
APPS
DIAGNOSTICS
SYSTEM
SECURITY
OPTIMIZATION

Every runtime metric:

value
source
updatedAt
status
updateMode
permissionState
availability
error

States:

LIVE
UPDATING
STALE
UNAVAILABLE
PERMISSION_REQUIRED
ERROR

Never fake a metric.

==================================================
13. RAM OPTIMIZATION
==================================================

Add direct:

OPTIMIZE RAM

Only perform legitimate Android/Kano-owned optimization:

Kano caches
bitmaps
media buffers
Kano workers
temporary resources
cancellable Kano work
memory-trim handling

Show:

RAM BEFORE
ACTIONS
RESOURCES RELEASED
RAM AFTER

If no meaningful system-wide RAM change occurs,
say so.

Never create fake:

RAM BOOST
+4 GB RAM
CPU BOOST
MAGIC CLEANER

==================================================
14. STORAGE INTELLIGENCE
==================================================

Create a full system-style storage explorer:

TOTAL
USED
FREE
PERCENTAGE

Categories where actually measurable:

Apps
System
System Apps
User Apps
Images
Videos
Audio
Documents
Downloads
Archives
APKs
Cache
Kano
Other
Recently Deleted

If a category cannot be measured through public APIs:

show unavailable.

Do not fabricate sizes.

Flow:

ANALYZE
→ SORT
→ REVIEW
→ SELECT
→ CONFIRM
→ DELETE/TRASH
→ VERIFY

Sort:

largest
smallest
newest
oldest
type
duplicate
temporary
location

==================================================
15. MEDIA MUST BECOME A REAL GALLERY
==================================================

IMPORTANT:

Do NOT keep the old manual-file-selection model as the main Media UI.

After the user grants appropriate Android media access:

Media behaves like a real Gallery.

Use MediaStore for accessible photos/videos.

Support:

FULL ACCESS
PARTIAL ACCESS
DENIED
REVOKED

Re-check permission state on resume.

Do not pretend to have full access when only partial access exists.

==================================================
16. KANO GALLERY
==================================================

Media opens into:

PHOTOS
VIDEOS
ALBUMS
RECENT
SCREENSHOTS
LARGE FILES
DUPLICATES
DOCUMENTS
DOWNLOADS
SAVED KNOWLEDGE

Timeline:

Today
Yesterday
dates

Use real dates and actual thumbnails.

Show where measured:

photo count
video count
total media size
screenshots
duplicates
large files

Use lazy loading, paging and thumbnail caching.

Never load the entire gallery into memory.

==================================================
17. MEDIA INTELLIGENCE
==================================================

Shared pipeline:

MediaStore
→ metadata
→ thumbnails
→ classification
→ OCR
→ QR
→ entity extraction
→ duplicate analysis
→ knowledge extraction
→ cleanup suggestions

Possible categories:

PERSONAL
TRAVEL
COLLEGE
STUDY
PROGRAMMING
PROJECT
WORK
FINANCE
RECEIPT
DOCUMENT
SCREENSHOT
WEBSITE
PRODUCT
SHOPPING
MOVIE
SERIES
ANIME
SONG
BOOK
GAME
TUTORIAL
MEME
ADVERTISEMENT
QR
SOCIAL
TEMPORARY
UNKNOWN

==================================================
18. KANO VISION
==================================================

Create Google-Lens-like functionality in Kano's own implementation.

Use local/on-device processing where practical.

Support:

OCR
URL extraction
QR detection
text extraction
document recognition
entity extraction
visual classification
screenshot understanding

Never claim unsupported recognition.

Show confidence/uncertainty where needed.

==================================================
19. SCREENSHOT UNDERSTANDING
==================================================

Website screenshot:
OCR → verify URL → Website record → Open/Save/Research

Movie screenshot:
identify → verify → Movie record → watchlist

Song:
song → artist → album when verified

Book:
title → author

Game:
title → platform/details when verified

Product:
identify → Product record → shopping research

Study:
OCR → topic → Study record

QR:
decode → type → payload
NEVER fabricate identity.
NEVER auto-initiate payment.

==================================================
20. MEMORY
==================================================

Kano remembers useful extracted knowledge.

Store structured entities:

Website
Product
App
Movie
Series
Song
Book
Game
Study Topic
Project
Note
Recommendation
Task
Preference
Outfit
WardrobeItem
CareProduct

Track:

source
timestamp
confidence
user confirmation
user edits

Then support:

"What movie did I save?"
"What game was that?"
"What was that website?"
"What products do I own?"
"What clothes do I have?"

==================================================
21. CLEANUP
==================================================

Create intelligent cleanup:

DUPLICATES
LARGE VIDEOS
OLD SCREENSHOTS
TEMPORARY FILES
REPEATED MEDIA
POTENTIAL LOW-VALUE CONTENT

Never silently delete.

Do not label personal media "unwanted" merely because AI dislikes it.

Use evidence + explanation.

Before deletion:

EXTRACT
→ VERIFY
→ SAVE KNOWLEDGE
→ SHOW USER
→ CONFIRM
→ DELETE
→ VERIFY

==================================================
22. CAMERA — MUST WORK
==================================================

Current camera is broken/incomplete.

Implement real CameraX:

permission
preview
front camera
rear camera
switch
capture
flash if supported
rotation
lifecycle
error handling
retake
save
cancel

Flow:

OPEN
→ PERMISSION
→ PREVIEW
→ CAPTURE
→ REVIEW
→ USE/SAVE/RETAKE

Integrate Camera with:

MEDIA
STYLE
CARE
VAULT
OCR
QR
PRODUCT SCAN

No fake preview.
No black screen.
No dead button.

==================================================
23. STYLE / DRESS ME
==================================================

Flow:

GALLERY or CAMERA
→ SCAN
→ ANALYZE
→ OUTFIT
→ REVIEW
→ RECOMMEND
→ AI PREVIEW
→ SAVE

Detect actual wardrobe items:

tops
bottoms
outerwear
shoes
bags
watches
accessories

Use objective criteria:

color coordination
layering
fit consistency
occasion
footwear
accessories
composition

Do not judge attractiveness.

Never invent wardrobe items.

==================================================
24. CARE / BEAUTY
==================================================

Scan actual product photos.

Categories:

SKINCARE
HAIR
GROOMING
ORAL
EVERYDAY

Extract only verified:

brand
name
category
variant
size
quantity
visible ingredients
expiry
batch

Inventory states:

ACTIVE
LOW
NEARLY EMPTY
EMPTY
EXPIRED
NOT USING
DISLIKED
REVIEW
UNKNOWN

Support:

"What do I have?"
"What do I need?"
"What should I buy?"

Always check existing inventory first.

==================================================
25. SHOPPING INTELLIGENCE
==================================================

Create Buyhatke-like shopping intelligence.

Support:

identify product
search current options
compare products
compare sellers
compare prices
alternatives
budget matching
value comparison
availability
warranty/return evidence where available
price observations where actually tracked

Labels:

CHEAPEST
BEST VALUE
BEST MATCH
ALTERNATIVE

Never fabricate:

price
seller
rating
reviews
availability
price history

Never auto-purchase.

==================================================
26. UNIVERSAL SEARCH
==================================================

One natural-language search.

Search authorized:

Media
Vault
Device
Apps
Style
Care
Study
Notifications
Email
Browser imports
Shopping
AI results
Web

Examples:

"Find my React tutorial."
"Show movie screenshots."
"What is taking storage?"
"What clothes do I own?"
"Which skincare products do I have?"
"Find my saved games."

==================================================
27. DAILY LIFE
==================================================

Where authorized:

calendar
tasks
deadlines
work
college
study
projects
reminders
goals

Daily Brief:

WHAT MATTERS TODAY?

Do not shame or manipulate.

==================================================
28. NOTIFICATIONS
==================================================

With explicit listener access:

IMPORTANT
PERSONAL
WORK
COLLEGE
FINANCIAL
TRANSACTION
PROMOTIONAL
LOW PRIORITY
UNKNOWN

Create digest and supported actions.

Never promise arbitrary in-app ad removal.

==================================================
29. GMAIL / BROWSER
==================================================

Use official APIs/auth.

Gmail:
OAuth
least privilege
disconnect
revoke
purge

Browser:
share/import/extension/supported APIs

Never scrape:
cookies
session tokens
private databases

==================================================
30. AI PROVIDERS
==================================================

Provider abstraction:

LOCAL
OPENAI
GEMINI
PERPLEXITY
FUTURE

Router based on:

capability
availability
privacy
latency
cost
user preference

Sensitive cloud processing:

detect sensitivity
redact where possible
ask consent
send only approved request-bound data

==================================================
31. PRIVACY CENTER
==================================================

Show:

permissions
data sources
local/cloud processing
AI providers
camera
media
notifications
monitoring
storage

Explain:

WHAT
WHY
WHERE
WHEN
HOW TO REVOKE

==================================================
32. ANIMATION SYSTEM
==================================================

Create a centralized animation language.

MICRO:
tap
press
toggle
selection

CONTENT:
fade
scale
slide
reveal
expand
collapse

MACRO:
screen transitions
morphing
shared continuity
image transitions
navigation

AMBIENT:
waves
floating layers
slow gradients
soft light

PROCESSING:
queued
indexing
analyzing
complete
failed

IMPORTANT:
Animation must reflect REAL STATE.

Do not fake progress.
Do not show fake AI thinking.
Do not animate constantly without purpose.

==================================================
33. WAVES / FLUID UI
==================================================

Create reusable theme-aware organic wave backgrounds.

Use:

soft curves
slow gradients
translucent layers
subtle floating motion

Stop/reduce expensive motion when:

screen not visible
Reduce Motion enabled
device performance requires it

==================================================
34. LOADING
==================================================

Avoid generic "Loading..." everywhere.

Use contextual loading:

Media:
thumbnail skeleton

OCR:
analysis animation

AI:
thinking surface

Device:
metric updating animation

Storage:
allocation animation

Camera:
initializing state

Never fake percentage completion.

==================================================
35. REAL-TIME DATA
==================================================

Use:

event-driven callbacks where available
periodic sampling where needed
ON_RESUME
manual refresh
lifecycle-aware collection

Do not poll everything every second.

Every metric:

value
source
status
updatedAt
updateMode
permissionState
availability
error

==================================================
36. RESPONSIVE UI
==================================================

Primary device:

OnePlus Nord CE 5
Android 16/API 36

Use adaptive layouts.

No hardcoded screen sizes.

No fixed bottom offsets.

Support:

small phones
normal phones
large phones
large fonts
200% text
gesture navigation
3-button navigation

No:

clipping
overflow
vertical text
hidden buttons
giant unusable cards
content under system UI

Use proper Android edge-to-edge/window inset handling.

==================================================
37. PERFORMANCE
==================================================

Optimize:

Compose recomposition
lazy lists
image loading
bitmap memory
blur
animations
camera
OCR
video
database
workers
battery

Avoid:

constant polling
memory leaks
large retained images
heavy blur everywhere
unnecessary workers
blocking main thread

==================================================
38. ANDROID 16
==================================================

Audit and, where compatible, migrate:

compileSdk
targetSdk

toward API 36.

Validate:

edge-to-edge
WindowInsets
predictive back
camera
MediaStore
permissions
adaptive layouts
background work

Do not blindly upgrade dependencies.

Build after changes.

==================================================
39. TESTING LOOP
==================================================

For EVERY major feature:

READ
→ PLAN
→ IMPLEMENT
→ BUILD
→ UNIT TEST
→ INSTALL
→ RUN
→ SCREENSHOT
→ INSPECT
→ FIX
→ RETEST
→ DOCUMENT
→ COMMIT

Test:

normal
empty
huge datasets
permission denied
permission revoked
offline
AI failure
camera failure
OCR failure
QR failure
storage failure
low RAM
low storage
database failure
process death
rotation
large fonts
light
dark
glass
reduced motion

==================================================
40. REAL DEVICE VALIDATION
==================================================

FIRST:

Android Studio emulator.

THEN:

real OnePlus Nord CE 5 using ADB/Wireless ADB when available.

Validate:

Home
Device
Storage
RAM
Optimize RAM
Battery
Connectivity
Apps
Diagnostics
Media
Gallery
Photos
Videos
OCR
QR
Camera
Style
Dress Me
Care
Vault
Shopping
Search
Privacy
Settings
Themes

Take actual screenshots.

Inspect them.

Fix visual problems.

Repeat.

==================================================
41. ABSOLUTE NO-FAKE RULE
==================================================

NEVER fabricate:

device data
RAM improvement
storage sizes
battery health
camera output
OCR
QR identity
products
prices
sellers
reviews
availability
AI provider access
permissions
Gmail data
browser data
successful actions
recommendations

If unavailable:

show UNAVAILABLE.

If permission required:

show PERMISSION REQUIRED.

If unsupported:

show UNSUPPORTED.

==================================================
42. DOCUMENTATION
==================================================

Keep documentation truthful.

Update:

AGENTS.md
KANO_WORKLOG.md
docs/KANO_STATE.md
docs/HANDOFF.md
docs/STATUS.md
docs/TEST_STATUS.md
docs/KNOWN_LIMITATIONS.md
docs/UI_SYSTEM.md
docs/DELETIONS.md

==================================================
43. FINAL AUTONOMOUS LOOP
==================================================

CONTINUOUSLY REPEAT:

AUDIT
→ PRIORITIZE
→ IMPLEMENT
→ BUILD
→ TEST
→ RUN
→ SCREENSHOT
→ VISUAL REVIEW
→ DATA REVIEW
→ SECURITY REVIEW
→ ACCESSIBILITY REVIEW
→ PERFORMANCE REVIEW
→ FIX
→ RETEST
→ DOCUMENT
→ COMMIT
→ REASSESS

Do not stop because:
- build passes
- tests pass
- one screen looks good
- a mockup looks good

The installed application is the final authority.

==================================================
44. FINAL QUALITY GATE
==================================================

KANO MUST FEEL LIKE:

PERSONAL AI
+
PHONE INTELLIGENCE
+
SMART GALLERY
+
KNOWLEDGE MEMORY
+
STYLE ASSISTANT
+
CARE ASSISTANT
+
SHOPPING INTELLIGENCE
+
PRIVATE DIGITAL ORGANIZER

WITH:

real data
real permissions
real camera
real gallery
real storage
real RAM information
real actions
real animations
real errors
real privacy
real responsiveness

VISUALLY:

STITCH-INSPIRED
GLASS
SPATIAL
FLUID
PREMIUM
CALM
AI-NATIVE
ORIGINAL

NOT:

GENERIC DASHBOARD
NOT:
VIBE-CODED UI
NOT:
FAKE SYSTEM OPTIMIZER

==================================================
START NOW
==================================================

FIRST AUDIT THE CURRENT KANO REPOSITORY.

THEN RECONCILE IT AGAINST THIS SPECIFICATION + STITCH.

THEN FIX THE HIGHEST-PRIORITY REAL GAPS.

THEN BUILD.

THEN RUN.

THEN INSPECT THE ACTUAL UI.

THEN FIX.

THEN REPEAT UNTIL THE PRODUCT QUALITY GATE PASSES.

KEEP YOUR CHAT OUTPUT LOW-TOKEN:
DONE
CHANGED
TESTED
ISSUES
NEXT
APK