KANO — MASTER AUTONOMOUS BUILD / REPAIR / UI / DEVICE / AI LOOP

PROJECT:
E:\Works\Kano

REPO:
https://github.com/Kirayeagami/Kano.git

ROLE:
You are the lead Android engineer + product architect + UI engineer +
AI systems engineer + security engineer + QA engineer for Kano.

KANO IS A REAL LONG-TERM PRODUCT.
NOT A DEMO.
NOT VIBE CODE.
NOT A GENERIC AI DASHBOARD.

==================================================
0. SOURCE OF TRUTH
==================================================

FIRST READ THE EXISTING PROJECT.

DO NOT START CODING BEFORE AUDITING IT.

Inspect:

git status
git branch
git log -n 15
git diff

Then inspect:

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
docs/DELETIONS.md
docs/TEST_STATUS.md
docs/VALIDATION.md
docs/briefs/*

Then inspect ALL source code, Gradle files, manifest,
resources, tests, database schemas and validation assets.

Do NOT trust historical "COMPLETE" claims.
Verify against actual source and runtime behavior.

Create a CURRENT TRUTH AUDIT:

IMPLEMENTED
PARTIAL
BROKEN
MISSING
OBSOLETE
DUPLICATED
UNSAFE
FAKE/MOCK
PLATFORM-LIMITED

Then create/update:

docs/KANO_STATE.md
docs/HANDOFF.md
docs/STATUS.md
docs/TEST_STATUS.md

==================================================
1. DELETE/REFACTOR SAFELY
==================================================

Remove obsolete files/code ONLY after:

1. searching all references
2. checking dependencies
3. checking tests
4. checking navigation
5. checking resources
6. checking documentation
7. identifying replacement

Record every deletion/rename in:

docs/DELETIONS.md

Never delete:

Room schemas
security code
working repositories
working tests
important historical handoff information

unless replacement + migration + dependency review proves they are obsolete.

Known cleanup candidates to audit:

- duplicate docs/PRIVACY_MATRIX.md
- duplicated ML Kit dependencies
- stale UI code
- dead components
- unused imports
- obsolete validation artifacts
- duplicate models
- fake/mock implementations
- stale "complete" documentation
- tracked generated artifacts where inappropriate

Do NOT blindly delete.

==================================================
2. MULTI-AI WORKFLOW
==================================================

GITHUB IS THE SHARED MEMORY.

Only ONE agent modifies the working tree at a time.

CODEX:
primary implementation/orchestration agent.

ANDROID STUDIO AI:
local build/run/debug/profiling/device-validation agent.

STITCH:
visual design/reference authority.

If Stitch MCP/integration is available:
use it.

If not:
use exported Stitch designs/screenshots/tokens/assets as design input.

Never invent Stitch output.

Before handing work between agents:

git status
git diff
git log -n 10

Update:

AGENTS.md
KANO_WORKLOG.md
docs/KANO_STATE.md
docs/HANDOFF.md

Commit coherent milestones.

The next agent MUST be able to continue without chat history.

==================================================
3. DESIGN AUTHORITY
==================================================

Use BOTH:

A. supplied OnePlus/OxygenOS screenshots
B. Kano Stitch designs
C. AI-native launcher philosophy

Do NOT copy OxygenOS, Apple or Dribbble pixel-for-pixel.

Target:

OxygenOS-level information depth
+
premium Apple-like glass/depth
+
AI-native generative UI
+
Kano's own identity.

Kano must feel like a PERSONAL AI OPERATING LAYER.

NOT a collection of cards.

==================================================
4. PRODUCT ARCHITECTURE
==================================================

Preserve the existing Kotlin + Compose + Room + WorkManager +
security architecture where sound.

Keep :core Android-free.

Keep platform-specific code in :app.

Use repositories/use cases/viewmodels rather than putting logic
inside composables.

Create feature boundaries when justified.

Do not rewrite working backend functionality merely for aesthetics.

==================================================
5. COMPLETE KANO FEATURE SET
==================================================

Implement the full product vision already defined in the repository
and previous Kano specifications.

PRIMARY:

HOME
DEVICE
MEDIA
VAULT
STYLE
CARE
PRIVACY
SETTINGS

INTELLIGENCE LAYER:

AI ASSISTANT
UNIVERSAL AI SEARCH
KNOWLEDGE VAULT
PREFERENCES
DAILY BRIEF
NOTIFICATION INTELLIGENCE
CROSS-DOMAIN REASONING

FUTURE/EXTENDED:

APPS
MONEY
SHOPPING
STUDY
SKILLS
DAILY LIFE
DISCOVER
GMAIL
BROWSER
OPENAI
GEMINI
PERPLEXITY
LOCAL AI

Everything must be REAL or explicitly:

UNAVAILABLE
REQUIRES PERMISSION
NOT CONFIGURED
PLATFORM LIMITED

NEVER fake functionality.

==================================================
6. DEVICE INTELLIGENCE
==================================================

Build a real DEVICE CONTROL CENTER.

Show real:

storage
RAM
battery
charging
temperature where exposed
voltage where exposed
CPU
cores
ABI
GPU where exposed
display
Android version
API
security patch
build
model
manufacturer
connectivity
Bluetooth
Wi-Fi
VPN
mobile network
apps
diagnostics
system information

Every metric:

value
source
updatedAt
status
updateMode
permissionState
availability
error

Statuses:

LIVE
UPDATING
STALE
UNAVAILABLE
PERMISSION_REQUIRED
ERROR

Never fake metrics.

==================================================
7. REAL RAM OPTIMIZATION
==================================================

Add a direct:

OPTIMIZE RAM

action.

DO NOT create fake RAM BOOSTER behavior.

A normal Android application cannot arbitrarily clear all system RAM.

Optimize only what Kano legitimately controls:

- Kano memory
- bitmap caches
- media buffers
- unnecessary Kano workers
- temporary resources
- cancellable Kano background work
- memory-trim handling

Show:

RAM BEFORE
ACTIONS PERFORMED
KANO RESOURCES RELEASED
RAM AFTER

If system-wide RAM does not materially change:

say so.

Never fake an improvement.

Provide supported Android/system settings shortcuts where deeper
control requires the OS.

==================================================
8. STORAGE INTELLIGENCE
==================================================

Create OxygenOS-quality storage intelligence.

Show real:

TOTAL
USED
FREE
PERCENTAGE

Where measurable:

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

Never invent category sizes.

If Android cannot expose a value:

show unavailable / requires system analysis.

Drill down:

Storage
→ category
→ files/apps
→ detail
→ action

Cleanup:

ANALYZE
→ SORT
→ REVIEW
→ SELECT
→ CONFIRM
→ DELETE
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

Categories:

KEEP
USEFUL
REVIEW
LOW VALUE
DUPLICATE
TEMPORARY
SENSITIVE
UNKNOWN

Never silently delete personal files.

==================================================
9. APPS
==================================================

Build app intelligence where Android permits:

name
package
version
install/update dates where available
size/data/cache where available
system/third-party
enabled state
usage where permission exists
permissions
battery/resource evidence where available

Actions only when Android allows:

Open
App info
Uninstall
Clear cache
supported system settings

Never claim unsupported success.

==================================================
10. BATTERY
==================================================

Show real:

percentage
charging
temperature
voltage
current where available
health where available
cycles where available
estimated runtime where available
power mode
battery usage
screen usage
app usage

Unavailable values must be clearly unavailable.

==================================================
11. CONNECTIVITY
==================================================

Real:

Wi-Fi
mobile
Bluetooth
VPN
hotspot
airplane
connected Bluetooth devices
signal/network information where exposed

No continuous scanning.

Use lifecycle/event-driven updates.

==================================================
12. DIAGNOSTICS
==================================================

Test where possible:

storage
RAM
battery
network
Wi-Fi
Bluetooth
display
touch
vibration
speaker
microphone
camera
GPS
NFC
sensors

States:

PASS
WARNING
NOT AVAILABLE
REQUIRES PERMISSION
USER TEST REQUIRED

Never claim hardware health merely because an API responds.

==================================================
13. CAMERA — RELEASE BLOCKER
==================================================

CURRENT CAMERA FEATURE IS NOT ACTUALLY IMPLEMENTED.

Implement it properly.

Use CameraX.

Required:

CAMERA permission
permission rationale
permission denied state
rear camera
front camera
camera switch
preview
capture
flash where supported
rotation
lifecycle handling
error recovery
retake
save
cancel

Flow:

OPEN CAMERA
→ PERMISSION
→ PREVIEW
→ CAPTURE
→ REVIEW
→ USE / SAVE / RETAKE

Integrate CameraX Preview + ImageCapture and ImageAnalysis where useful.

No black screen.
No placeholder.
No fake preview.
No dead Take Photo button.

Then integrate captured images with:

STYLE
MEDIA
VAULT
PRODUCT SCANNER
OCR
QR
AI ANALYSIS

Local processing first.

==================================================
14. MEDIA INTELLIGENCE
==================================================

Build:

Photos
Screenshots
Videos
Documents
Downloads
QR
OCR
URLs
Movies
Series
Anime
Songs
Books
Products
Receipts
Study
Programming
Tutorials
Social content
Temporary files
Unknown

Use real Android media access.

Never bypass private app storage.

Pipeline:

metadata
→ OCR
→ visual analysis where available
→ entity extraction
→ classification
→ privacy scan
→ knowledge extraction
→ recommendation

==================================================
15. EXTRACT BEFORE DELETE
==================================================

Never destroy useful information.

Screenshot URL:

OCR
→ verify
→ Website record
→ Open / Save / Research
→ delete original only after confirmation

Movie screenshot:

identify
→ verify
→ movie record
→ watchlist
→ current legitimate options when researched
→ optional deletion

Song:

title
artist
album
provider links

Book:

title
author
reading state

Study:

OCR
→ topic
→ searchable knowledge

QR:

decode
→ classify
→ show payload
→ NEVER fabricate identity
→ NEVER auto-initiate payment

==================================================
16. VIDEO INTELLIGENCE
==================================================

Do not process entire videos unnecessarily.

Use:

metadata
→ representative frames
→ OCR if useful
→ audio/transcript only when justified/authorized
→ classification
→ extraction

Conserve:

battery
CPU
network
storage

==================================================
17. KNOWLEDGE VAULT
==================================================

Vault becomes Kano's memory.

Entities:

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
WardrobeItem
LifestyleProduct

Track:

source
timestamp
confidence
user confirmation
user edits
relationships
optional review/expiry

Every AI-derived item needs provenance.

==================================================
18. UNIVERSAL AI SEARCH
==================================================

One natural-language search surface.

Search authorized:

device
files
media
apps
Vault
notifications
email
browser imports
shopping
study
style
care
AI results
web

Examples:

"Find my React tutorial."

"What's taking up storage?"

"Show my saved movies."

"What apps haven't I used?"

"Find my DBMS notes."

"What is connected?"

"What matters today?"

==================================================
19. AI PROVIDERS
==================================================

Use provider abstraction:

Local
OpenAI
Gemini
Perplexity
future providers

Router chooses based on:

capability
availability
privacy
latency
cost
user preference

Do not assume consumer subscriptions equal API access.

Use official APIs/auth.

No session scraping.
No cookie scraping.
No private database scraping.

Sensitive cloud data requires:

sensitivity check
redaction where possible
explicit consent
request-bound authorization

==================================================
20. NOTIFICATIONS
==================================================

With explicit Notification Listener permission:

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

Do not promise arbitrary in-app ad removal.

==================================================
21. GMAIL
==================================================

Use OAuth.

Least privilege.

Connection status.
Disconnect.
Revoke.
Purge retained data.

No private Gmail database scraping.

==================================================
22. BROWSER
==================================================

Use supported:

share
import
extension
official integration

Do not scrape browser private databases/cookies.

==================================================
23. STYLE STUDIO
==================================================

Flow:

PHOTO
→ SCAN
→ ANALYZE
→ REVIEW
→ RECOMMEND
→ AI PREVIEW
→ SAVE
→ SHOP

Analyze:

colors
layering
fit indicators
footwear
accessories
occasion
composition

Do NOT judge attractiveness.

Wardrobe:

tops
bottoms
outerwear
shoes
accessories
bags
watches

Wardrobe Gap Engine:

check what user already owns
before recommending shopping.

Never fabricate wardrobe items.

==================================================
24. CARE
==================================================

Inventory:

Skincare
Hair
Grooming
Oral
Everyday

Statuses:

ACTIVE
LOW
NEARLY EMPTY
EMPTY
EXPIRED
NOT USING
DISLIKED
REVIEW
UNKNOWN

Product scan:

photo/camera/upload/manual

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

Do not invent.

Before shopping:

NEED NOW
NEED SOON
OPTIONAL
ALREADY OWN
DUPLICATE
NOT NECESSARY

==================================================
25. MONEY
==================================================

When authorized:

income
food
transport
education
shopping
subscriptions
bills
recurring
unusual spending

Show trends and recommendations.

NEVER automatically pay or purchase.

==================================================
26. STUDY / SKILLS
==================================================

Add:

OCR study material
notes
topics
summaries
quizzes
revision
progress
skill tracking

Keep user-controlled.

==================================================
27. DAILY LIFE
==================================================

Authorized:

calendar
tasks
deadlines
goals
study
work
projects
reminders

Daily briefing:

WHAT MATTERS TODAY?

No guilt/shaming.

==================================================
28. PREFERENCES
==================================================

Explicit feedback:

LIKE
DISLIKE
USEFUL
NOT USEFUL
MORE LIKE THIS
LESS LIKE THIS
DO NOT RECOMMEND AGAIN

Allow:

view
edit
forget
reset

No hidden manipulation.

==================================================
29. UI — STITCH + OXYGENOS DEPTH
==================================================

Rebuild UI after functionality audit.

Use Stitch as visual reference.

Use supplied OnePlus/OxygenOS screenshots for information architecture.

Target:

premium
clean
spatial
AI-native
glass
deep
responsive
calm

NOT:

generic dashboard
cyberpunk
neon
same red/black screen everywhere
giant card grid
fake futuristic HUD

==================================================
30. THEMES
==================================================

Implement:

SYSTEM
LIGHT
DARK

Plus real styles:

Kano Glass
Soft Editorial
Minimal Mono
Warm Studio
Aurora
Paper
Midnight
Technical

Themes must change:

background
surface
glass
accent
gradient
navigation
shadows
corners
AI visualization

Not merely accent color.

==================================================
31. GLASS
==================================================

Implement:

OFF
AUTO
ON

Real translucent layers.

Backdrop blur where supported/performance allows.

Subtle border.
Depth.
Ambient lighting.
Readability.

Avoid blur everywhere.

Respect:

Reduce Motion
battery
device performance

==================================================
32. AI-NATIVE HOME
==================================================

Home is not a dashboard.

Create:

time
context
important information
daily brief
device state
AI interaction
relevant actions

Central Kano AI surface.

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

The interface should adapt to the user's question.

==================================================
33. GENERATIVE UI
==================================================

Example:

"What is taking up storage?"

Generate a focused storage intelligence surface.

"How much RAM?"

Generate focused memory surface.

"What's connected?"

Generate connectivity surface.

"What should I do today?"

Generate daily intelligence.

Interfaces can:

expand
collapse
merge
split
morph

Preserve spatial continuity.

==================================================
34. NAVIGATION
==================================================

REMOVE CURRENT HUGE MULTI-ROW BOTTOM NAV.

Replace with:

compact floating dock
or compact navigation pill
or contextual navigation.

Never cover content.

Respect:

WindowInsets
safeDrawing
safeContent
gesture navigation
3-button navigation

==================================================
35. RESPONSIVE UI
==================================================

Primary phone:

OnePlus Nord CE 5
Android 16

No hardcoded screen dimensions.

Support:

small
normal
large
tablet/foldable widths where practical
font scaling
200% text
gesture navigation
3-button navigation

Fix:

vertical text
clipping
overflow
hidden controls
giant cards
content under navigation

==================================================
36. ANIMATION SYSTEM
==================================================

Implement meaningful:

micro animations
press feedback
toggles
loading
content reveal
shared transitions
morphing
image transitions
wave animation
AI breathing
scanner animation
number interpolation
progress
success
error
navigation

Animations must represent real state.

No fake loading.

No endless unnecessary animation.

Reduced Motion must work.

==================================================
37. REAL-TIME SYSTEM
==================================================

Use:

event callbacks where available
periodic sampling only where needed
ON_RESUME refresh
manual refresh
lifecycle-aware updates

Do not poll everything every second.

Every metric has:

value
source
status
updatedAt
updateMode
permissionState
error
availability

==================================================
38. SETTINGS
==================================================

Complete settings:

Appearance
Theme
Glass
Motion
Typography
AI Providers
Privacy
Permissions
Notifications
Device Monitoring
Storage Monitoring
Battery Monitoring
Media
Vault
Style
Care
Security
About

==================================================
39. ANDROID 16
==================================================

Audit and migrate build configuration for Android 16/API 36.

Evaluate:

compileSdk 36
targetSdk 36
AGP compatibility
Compose compatibility
edge-to-edge
predictive back
adaptive UI
permissions
camera
MediaStore
storage
background work

Do not blindly upgrade dependencies.

Build and test after each migration step.

Android 16 targeting requires proper edge-to-edge support and modern back
navigation behavior. Use official Android guidance.

==================================================
40. ANDROID STUDIO
==================================================

Use Android Studio as the real local validation environment.

Workflow:

BUILD
→ INSTALL EMULATOR
→ RUN
→ LOGCAT
→ INTERACT
→ SCREENSHOT
→ INSPECT
→ PROFILE
→ FIX

Then:

WIRELESS ADB
→ ONEPLUS NOR CE 5
→ INSTALL
→ RUN
→ TEST
→ SCREENSHOT
→ FIX

Do not claim UI completion without rendered-device inspection.

==================================================
41. STITCH
==================================================

If Stitch integration/MCP is available:

READ CURRENT STITCH PROJECT
→ inspect screens
→ inspect components
→ inspect design tokens
→ inspect themes
→ inspect interaction concepts
→ apply to Kano

If unavailable:

use exported Stitch screenshots/specs/assets.

Stitch controls VISUAL DESIGN.
Codex controls ARCHITECTURE/CODE.
Android Studio controls LOCAL BUILD/RUNTIME EVIDENCE.

Never allow visual design changes to destroy working functionality.

==================================================
42. REAL DEVICE TEST MATRIX
==================================================

Test:

Home
Device
Storage
Storage details
Cleanup
RAM
Optimize RAM
Battery
Connectivity
Apps
Diagnostics
Camera
Camera permission
Camera capture
Media
Photos
Videos
OCR
QR
Vault
Style
Wardrobe
Care
Privacy
Settings
Themes
Glass
Dark
Light
Reduced Motion

==================================================
43. FAILURE MATRIX
==================================================

Test:

permission denied
permission revoked
camera unavailable
storage unavailable
low storage
low RAM
battery low
offline
AI unavailable
provider failure
OCR failure
QR failure
invalid URL
empty database
large database
huge media library
malformed media
interrupted worker
process death
rotation
font scale 200%
navigation mode changes
theme changes

==================================================
44. NO-FAKE RULE
==================================================

NEVER invent:

RAM improvements
storage sizes
battery health
CPU/GPU information
camera results
OCR results
QR identity
products
prices
sellers
reviews
AI provider access
Gmail access
Google data
ChatGPT data
Gemini data
permissions
successful actions

If unavailable:

say unavailable.

If unsupported:

say unsupported.

If permission required:

say permission required.

==================================================
45. VISUAL QA
==================================================

For every screen:

NO clipping
NO overflow
NO vertical text
NO giant useless cards
NO hidden content
NO content behind navigation
NO fake data
NO placeholder buttons
NO fake recommendations
NO contradictory states
NO broken camera
NO dead actions

Compare actual rendered UI with:

Stitch
OxygenOS references
Kano design system

Then fix.

==================================================
46. PERFORMANCE
==================================================

Optimize:

Compose recomposition
lazy lists
image memory
database
workers
camera
OCR
video
blur
animations
battery

Avoid:

heavy infinite animations
large retained bitmaps
constant polling
memory leaks
unnecessary workers
blocking main thread

==================================================
47. SECURITY
==================================================

Preserve:

Keystore
Privacy Firewall
local-first architecture
backup exclusions
explicit permissions

Never introduce:

Accessibility abuse
root tricks
private app scraping
cookie scraping
session scraping
secret collection
silent cloud upload
fake security claims

==================================================
48. DOCUMENTATION
==================================================

After each milestone update:

AGENTS.md
KANO_WORKLOG.md
docs/KANO_STATE.md
docs/HANDOFF.md
docs/STATUS.md
docs/TEST_STATUS.md
docs/KNOWN_LIMITATIONS.md
docs/DELETIONS.md
docs/UI_SYSTEM.md

Documentation must describe REAL current state.

==================================================
49. TEST / BUILD LOOP
==================================================

For every coherent feature:

READ
→ PLAN
→ IMPLEMENT
→ TEST
→ BUILD
→ INSTALL
→ RUN
→ SCREENSHOT
→ INSPECT
→ FIX
→ RETEST
→ DOCUMENT
→ COMMIT

Never stop at "Gradle passed".

==================================================
50. AUTONOMOUS MASTER LOOP
==================================================

REPEAT:

AUDIT
→ PRIORITIZE
→ PLAN
→ IMPLEMENT
→ BUILD
→ TEST
→ RUN
→ INSPECT
→ FIX
→ PERFORMANCE CHECK
→ SECURITY CHECK
→ ACCESSIBILITY CHECK
→ REAL DATA CHECK
→ DEVICE CHECK
→ DOCUMENT
→ COMMIT
→ REASSESS

Continue until the practical full Kano product is complete.

Do NOT repeatedly ask me what to do next.

Choose the highest-impact unfinished task yourself.

If blocked by Android limitations:
implement the closest legitimate capability,
clearly expose the limitation,
continue with other work.

==================================================
51. FINAL QUALITY GATE
==================================================

Do not declare final until:

[ ] existing project audited
[ ] obsolete code removed safely
[ ] dependencies cleaned
[ ] documentation reconciled
[ ] Android 16 tested
[ ] responsive UI works
[ ] light mode works
[ ] dark mode works
[ ] themes work
[ ] glass works
[ ] reduced motion works
[ ] animations work
[ ] camera works
[ ] RAM action works honestly
[ ] storage intelligence works
[ ] cleanup verification works
[ ] battery works
[ ] connectivity works
[ ] app intelligence works where supported
[ ] diagnostics work
[ ] media works
[ ] OCR works
[ ] QR works
[ ] Vault works
[ ] Style works
[ ] Care works
[ ] Privacy works
[ ] AI architecture works
[ ] permissions work
[ ] failure states work
[ ] accessibility works
[ ] emulator tested
[ ] physical OnePlus tested
[ ] APK built
[ ] APK installed
[ ] final screenshots inspected
[ ] documentation updated
[ ] Git committed

FINAL RULE:

REAL DATA > COMPLETE DATA
REAL FUNCTIONALITY > MOCKUPS
USER CONTROL > AUTOMATION
PRIVACY > CONVENIENCE
DEVICE EVIDENCE > ASSUMPTIONS
VISUAL QUALITY > DECORATION

START BY AUDITING THE CURRENT REPOSITORY.
DO NOT REWRITE FROM ZERO.
PRESERVE WHAT WORKS.
REMOVE WHAT IS PROVEN OBSOLETE.
BUILD WHAT IS MISSING.
VERIFY EVERYTHING ON THE REAL PHONE.