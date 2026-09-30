KANO — ASTRA 6
PHASE 2 MASTER AUTONOMOUS IMPLEMENTATION LOOP
REAL STORAGE INTELLIGENCE → ORGANIZATION → CLEANUP FOUNDATION

STATUS:

The physical OnePlus Nord CE 5 is now CONNECTED and AUTHORIZED.

Verified:

MODEL: OnePlus Nord CE 5
ANDROID: 16
API: 36
ADB: E:\Works\tools\kano\android-sdk\platform-tools\adb.exe
PHYSICAL DEVICE: ONLINE
EMULATOR: ONLINE

The physical phone is now the PRIMARY REAL-DEVICE TEST TARGET.

============================================================
MISSION
============================================================

Continue the EXISTING Kano project.

DO NOT rebuild Kano.

DO NOT rebuild Phase 1 Device Intelligence.

DO NOT create another repository.

DO NOT create duplicate architecture.

DO NOT blindly trust previous AI claims.

The goal now is to build the next major Kano capability:

REAL STORAGE INTELLIGENCE.

Kano should eventually become capable of:

UNDERSTANDING MY PHONE
→ UNDERSTANDING MY STORAGE
→ ORGANIZING MY DATA
→ FINDING DUPLICATES
→ FINDING LARGE/UNNECESSARY DATA
→ GIVING ME SMART RECOMMENDATIONS
→ ASKING BEFORE DESTRUCTIVE ACTION
→ REMOVING SAFE ITEMS
→ PERMANENTLY DELETING WHEN I CONFIRM
→ VERIFYING THE RESULT
→ RE-MEASURING STORAGE
→ CONTINUALLY HELPING ME KEEP THE PHONE ORGANIZED

But implement this ONE VERIFIED PHASE AT A TIME.

============================================================
01 — ABSOLUTE REPOSITORY RULE
============================================================

ONLY WORK ON:

E:\Works\Kano

Verify:

git rev-parse --show-toplevel

Expected:

E:/Works/Kano

NEVER:

- create a second Kano project
- clone Kano
- copy Kano
- create another Git repository
- work from E:\Works\tools\kano as source
- move the source repository
- overwrite unrelated projects

E:\Works\tools\kano is TOOLING ONLY.

============================================================
02 — PRESERVE EXISTING WORK
============================================================

Before editing:

git status
git branch
git log --oneline -10
git diff --stat
git diff --check

Inspect all existing modifications.

DO NOT:

git reset --hard
git clean -fd
force push
discard uncommitted changes
restore files blindly

Preserve valid Antigravity work.

============================================================
03 — FIRST: TAKEOVER AUDIT
============================================================

Before implementing Phase 2:

READ:

AGENTS.md
KANO_WORKLOG.md
README.md

and:

docs/KANO_STATE.md
docs/ARCHITECTURE.md
docs/ROADMAP.md
docs/UI_SYSTEM.md
docs/PRIVACY_MATRIX.md
docs/SECURITY_MODEL.md
docs/TEST_STATUS.md
docs/KNOWN_LIMITATIONS.md
docs/DELETIONS.md

Inspect actual code for:

DeviceReader
DeviceScreen
KanoViewModel
MediaRepository
MediaSearch
Room database
existing MediaStore implementation
existing storage implementation
permission/access controller
WorkManager
repositories
domain models
UI navigation

Determine:

ALREADY IMPLEMENTED
PARTIALLY IMPLEMENTED
MISSING
DUPLICATE
BROKEN
MOCK/FAKE

Do not rebuild something that already works.

============================================================
04 — PHASE 1 PRESERVATION
============================================================

Phase 1 Device Intelligence already exists.

Preserve it.

Do not unnecessarily modify:

DeviceReader
DeviceScreen
device models
device tests
device navigation

unless Phase 2 genuinely requires integration.

Existing Device Intelligence must continue working.

After Phase 2 changes:

retest Phase 1.

============================================================
05 — PHASE 2 OBJECTIVE
============================================================

Implement:

KANO STORAGE INTELLIGENCE ENGINE

Kano must be able to understand the storage it is legitimately allowed
to access.

The system must move from:

"Storage = 128 GB"

to:

"Here is what is consuming the accessible storage,
what is duplicated,
what is large,
what is potentially temporary,
what should be reviewed,
and why."

============================================================
06 — REAL DATA ONLY
============================================================

ZERO fabricated information.

ZERO fake metrics.

ZERO fake progress.

ZERO fake scanning.

ZERO fake AI analysis.

ZERO fake recommendations.

ZERO fake storage recovery.

ZERO placeholder numbers in production UI.

Every important number must come from:

Android API
MediaStore
filesystem metadata
Room
or a deterministic calculation from real data.

============================================================
07 — STORAGE ARCHITECTURE
============================================================

Reuse existing architecture.

Use legitimate Android APIs including where appropriate:

MediaStore
StatFs
StorageStatsManager
Storage Access Framework
Room
Coroutines
WorkManager

Do not use:

hidden APIs
reflection hacks
root
security exploits
private app databases
another application's private storage

Maximum legitimate Android capability only.

============================================================
08 — STORAGE BOUNDARIES
============================================================

Clearly distinguish:

DEVICE STORAGE
SHARED STORAGE
MEDIASTORE
KANO PRIVATE STORAGE
ACCESSIBLE FILES
INACCESSIBLE APP DATA

Never imply Kano can see another application's private sandbox.

If information is unavailable:

show:

UNAVAILABLE

or:

NOT FULLY MEASURABLE

Never convert unavailable into zero.

============================================================
09 — REAL STORAGE OVERVIEW
============================================================

Implement:

TOTAL
USED
AVAILABLE

Also calculate where legitimately possible:

KANO STORAGE
ACCESSIBLE MEDIA
ACCESSIBLE FILES

The UI should clearly explain what each number represents.

============================================================
10 — REAL STORAGE CATEGORIES
============================================================

Analyze accessible content into:

PHOTOS
VIDEOS
SCREENSHOTS
SCREEN RECORDINGS
AUDIO
DOCUMENTS
DOWNLOADS
APKS
ARCHIVES
LARGE FILES
DUPLICATES
TEMPORARY
KANO DATA
OTHER
UNKNOWN

Category sizes must be calculated from actual indexed data.

============================================================
11 — MEDIA INDEX
============================================================

Build/reuse an efficient MediaStore index.

Track where available:

URI
NAME
SIZE
MIME TYPE
DATE ADDED
DATE MODIFIED
WIDTH
HEIGHT
DURATION
MEDIA TYPE

Do not load the entire gallery into RAM.

Do not decode full-resolution images just for indexing.

Do not load entire videos.

Use metadata first.

Use thumbnails/previews only when required.

============================================================
12 — FILE INDEX
============================================================

For legitimately accessible files maintain:

ID
URI/path where legitimate
NAME
EXTENSION
MIME
SIZE
CREATED/ADDED
MODIFIED
CATEGORY
CLASSIFICATION
HASH STATE
SCAN STATE
ACCESS STATE
SENSITIVITY

Design for future:

OCR
QR
URL extraction
knowledge extraction
AI analysis
duplicate detection
deletion state

============================================================
13 — DATABASE
============================================================

Reuse existing Room.

Do not create another database.

If schema changes:

create a proper migration.

Test:

fresh database
existing database
upgrade
existing user data

Never wipe user data to make migrations pass.

============================================================
14 — CLASSIFICATION
============================================================

Use:

KEEP
USEFUL
REVIEW
LOW_VALUE
DUPLICATE
TEMPORARY
SENSITIVE
UNKNOWN

CRITICAL:

UNKNOWN = KEEP.

Never decide something is unnecessary only because:

old
large
unopened
poorly named
unfamiliar

============================================================
15 — DUPLICATE DETECTION
============================================================

Implement REAL exact duplicate detection.

Pipeline:

SIZE
→ MIME
→ DIMENSIONS/DURATION
→ CANDIDATE GROUP
→ HASH/FINGERPRINT
→ EXACT MATCH

Avoid O(n²).

Use bounded concurrency.

Use background processing.

Support large libraries.

Report:

copies
total size
potential recoverable size
files
dates
confidence

IMPORTANT:

Before deletion:

"POTENTIALLY RECOVERABLE"

After verified deletion:

"ACTUALLY RECOVERED"

Never confuse these.

============================================================
16 — LARGE FILE INTELLIGENCE
============================================================

Detect:

100 MB+
500 MB+
1 GB+

or configurable thresholds.

But:

LARGE ≠ UNNECESSARY.

Show:

FILE
SIZE
TYPE
DATE
LOCATION
WHY IT IS BEING SHOWN

============================================================
17 — DOWNLOAD INTELLIGENCE
============================================================

Analyze Downloads.

Recognize:

APK
ZIP
RAR
7Z
EXE
INSTALLER
PDF
DOCUMENT
IMAGE
VIDEO
AUDIO
ARCHIVE
DUPLICATE
UNKNOWN

Old does not automatically mean delete.

============================================================
18 — TEMPORARY DATA
============================================================

Identify only genuinely reconstructible data.

Separate:

KANO TEMPORARY DATA
ANDROID-MANAGED DATA
USER DATA

Kano may manage its own safe reconstructible data.

Do not pretend to clear other apps' private caches.

============================================================
19 — SMART RECOMMENDATIONS
============================================================

Every recommendation must contain:

WHAT
WHY
EVIDENCE
SIZE
CONFIDENCE
RISK
ACTION

Examples:

"18 exact duplicate photos — 640 MB potentially recoverable."

"4 videos are larger than 1 GB — review recommended."

"Kano has 280 MB of reconstructible temporary data."

"Storage is 92% full."

Recommendations must be derived from real data.

============================================================
20 — SCANNER
============================================================

Implement:

SCAN
PAUSE
RESUME
CANCEL
RESCAN

Scanner must be cancellable.

Persist scan state.

Handle process death.

Handle worker interruption.

Handle app restart.

If progress is measurable:

show actual progress.

If not:

use indeterminate progress.

NEVER fabricate percentages.

============================================================
21 — INCREMENTAL INDEXING
============================================================

Do not perform a complete scan every time.

Support:

INITIAL SCAN
NEW MEDIA
CHANGED MEDIA
REMOVED MEDIA
INCREMENTAL UPDATE

Reuse Android MediaStore/version mechanisms where appropriate.

Track:

last scan
duration
items scanned
items added
items updated
items removed
errors

============================================================
22 — BACKGROUND PROCESSING
============================================================

Use WorkManager where appropriate.

Respect:

Doze
battery restrictions
background execution
process death

Support:

retry
cancel
resume
partial progress

Never claim "always running."

============================================================
23 — PERMISSIONS
============================================================

Use the existing permission/access architecture.

Support:

FULL
LIMITED
DENIED
REVOKED

When access is limited:

tell the user exactly what Kano can analyze.

When revoked:

stop using it.

Update UI.

Provide a clear route to Android Settings when needed.

============================================================
24 — USER CONTROL
============================================================

Kano should eventually support:

AUTO
ASK
MANUAL

For Phase 2:

AUTO:
scan/index authorized data.

ASK:
permission requests.

MANUAL:
user-triggered scan.

No destructive action yet.

============================================================
25 — SECURITY
============================================================

The user wants maximum capability.

DO NOT bypass Android security.

NEVER:

root
exploit Android
bypass permissions
bypass scoped storage
access private app sandboxes
read private app databases
read credentials
read cookies
steal tokens
secretly access camera/microphone

Use public Android APIs.

Maximum legitimate capability only.

============================================================
26 — LOCAL FIRST
============================================================

Storage intelligence must work without cloud AI.

Do:

local metadata
local indexing
local classification
local duplicate detection

Do not upload the entire gallery.

Keep architecture ready for the future Kano AI Router.

============================================================
27 — STORAGE UI
============================================================

Use existing Kano visual language.

Do not create a generic phone-cleaner interface.

Make it:

premium
clean
information-first
responsive
real-data-first

Suggested structure:

STORAGE

TOTAL
USED
AVAILABLE

STORAGE BREAKDOWN

Photos
Videos
Screenshots
Audio
Documents
Downloads
APKs
Archives
Other

SMART REVIEW

Duplicates
Large Files
Downloads
Temporary Data

RECOMMENDATIONS

SCAN STATUS

Last scan
Items indexed
Last update

Avoid excessive cards.

============================================================
28 — REAL UI STATES
============================================================

Implement:

FIRST RUN
NO ACCESS
LIMITED ACCESS
EMPTY
SCANNING
PAUSED
CANCELLED
COMPLETED
ERROR
PARTIAL FAILURE
STALE INDEX
UPDATING

No fake state.

============================================================
29 — PERFORMANCE
============================================================

Test:

100 files
1,000 files
10,000+ files

Do not:

scan on main thread
load full gallery
load full videos
create unlimited coroutines
hash everything blindly
rescan everything unnecessarily

Use:

IO
batching
paging
bounded concurrency
Room transactions
incremental updates
cancellation

============================================================
30 — REAL PHONE TESTING
============================================================

The OnePlus Nord CE 5 is now connected.

Use it as the PRIMARY REAL DEVICE.

Verify:

adb devices -l

Expected:

OnePlus Nord CE 5 = device

Install the current APK on the physical phone.

Test real:

storage
photos
videos
downloads
screenshots
large files
duplicates
permissions
scan
cancel
resume
rescan

Compare important storage numbers with:

Android Settings
File Manager

Do not copy those numbers into Kano.

Kano must independently calculate/read them.

============================================================
31 — EMULATOR TESTING
============================================================

Keep emulator testing too.

Run:

unit tests
instrumentation tests
UI tests

Then physical-device verification.

Both matter.

============================================================
32 — PHASE 1 REGRESSION
============================================================

After Storage implementation:

retest:

Device Overview
Performance
RAM
Storage
Battery
Thermal
Connectivity
Hardware
Sensors
Diagnostics

Phase 1 must not regress.

============================================================
33 — TEST MATRIX
============================================================

Test:

empty storage
normal storage
low storage
nearly full storage
large library
large video
corrupt file
missing metadata
invalid URI
duplicate files
permission denied
limited media access
permission revoked
permission restored
scan cancellation
worker interruption
app restart
device restart
database migration
dark mode
light mode

============================================================
34 — BUILD LOOP
============================================================

After coherent implementation:

:core:test
:app:testDebugUnitTest
:app:lintDebug
:app:assembleDebug

Then:

INSTALL
→ LAUNCH
→ TEST
→ SCREENSHOT
→ INSPECT
→ FIX
→ REBUILD
→ RETEST

Do not stop at compilation.

============================================================
35 — VISUAL QA LOOP
============================================================

Capture and inspect:

Storage overview
Storage breakdown
Scanning
Completed scan
Duplicates
Large files
Downloads
Temporary data
No permission
Limited permission
Empty state
Error state

Test:

LIGHT MODE
DARK MODE

Check:

typography
spacing
contrast
scrolling
footer
navigation
system insets
long filenames
large numbers
loading
empty states

============================================================
36 — NO DELETION YET
============================================================

DO NOT implement:

automatic deletion
bulk deletion
permanent deletion
trash
destructive cleanup

in Phase 2.

Phase 2 must only:

SCAN
INDEX
UNDERSTAND
CLASSIFY
RECOMMEND

Phase 3 will implement:

REVIEW
REMOVE
TRASH
PERMANENT DELETE
CONFIRMATION
VERIFICATION
ACTUAL SPACE RECOVERY

============================================================
37 — RAM BOUNDARY
============================================================

DO NOT implement RAM optimization yet.

Phase 4 will later implement:

OPTIMIZE KANO

using legitimate operations.

Never claim system-wide RAM boosting.

============================================================
38 — MASTER AUTONOMOUS LOOP
============================================================

Repeat this exact cycle for every meaningful change:

1. INSPECT
2. UNDERSTAND
3. SEARCH EXISTING CODE
4. PLAN
5. IMPLEMENT
6. COMPILE
7. UNIT TEST
8. LINT
9. BUILD APK
10. INSTALL
11. RUN
12. REAL DATA TEST
13. EMULATOR TEST
14. PHYSICAL ONEPLUS TEST
15. CAPTURE SCREENSHOTS
16. VISUAL INSPECTION
17. FIND PROBLEMS
18. FIX PROBLEMS
19. REBUILD
20. RETEST
21. VERIFY REAL RESULTS
22. UPDATE DOCUMENTATION
23. REVIEW GIT DIFF
24. CONTINUE

Do not stop because:

"build successful"

is not the definition of done.

============================================================
39 — FAILURE LOOP
============================================================

If anything fails:

DO NOT hide it.

Record:

FAILURE
CAUSE
AFFECTED FEATURE
FIX
TEST BEFORE
TEST AFTER

Then repeat:

FIX
→ BUILD
→ TEST
→ INSTALL
→ RUN
→ VERIFY

============================================================
40 — DOCUMENTATION
============================================================

Update only after verified behavior:

KANO_WORKLOG.md
docs/KANO_STATE.md
docs/ARCHITECTURE.md
docs/ROADMAP.md
docs/TEST_STATUS.md
docs/KNOWN_LIMITATIONS.md

Document actual implementation.

Never claim:

COMPLETE

until it is actually verified.

============================================================
41 — GIT SAFETY
============================================================

Do not commit automatically.

Do not push automatically.

Before final report:

git status
git diff --stat
git diff --check

Preserve unrelated work.

============================================================
42 — DEFINITION OF DONE
============================================================

Phase 2 is COMPLETE only when:

REAL STORAGE ENGINE
+
REAL MEDIA INDEX
+
REAL FILE INDEX
+
REAL CATEGORIES
+
REAL DUPLICATE DETECTION
+
REAL LARGE-FILE DETECTION
+
REAL DOWNLOAD ANALYSIS
+
REAL TEMPORARY-DATA ANALYSIS
+
REAL RECOMMENDATIONS
+
INCREMENTAL SCANNING
+
CANCELLATION
+
PERMISSION HANDLING
+
DATABASE SAFETY
+
PERFORMANCE VALIDATION
+
UNIT TESTS
+
LINT
+
BUILD
+
EMULATOR TEST
+
PHYSICAL ONEPLUS TEST
+
LIGHT MODE
+
DARK MODE
+
EDGE CASES
+
DOCUMENTATION
+
NO FABRICATED DATA
+
NO SECURITY BYPASS

============================================================
43 — FINAL REPORT
============================================================

When Phase 2 is actually complete, STOP.

Report:

KANO — PHASE 2 FINAL REPORT

Repository:
Branch:
Git status:

Files changed:

Existing architecture reused:

New architecture:

Storage APIs:

Database changes:

Permission behavior:

Media indexing:

File indexing:

Duplicate engine:

Large-file engine:

Download intelligence:

Temporary-data engine:

Recommendation engine:

Incremental scanning:

Performance:

Unit tests:

Instrumentation tests:

Lint:

Build:

APK:

Emulator:

ONEPLUS NORD CE 5:

Real storage verification:

Screenshots:

Known limitations:

Security/privacy:

Phase 3 recommendation:

DO NOT COMMIT.
DO NOT PUSH.

============================================================
START NOW
============================================================

The OnePlus Nord CE 5 is confirmed ONLINE and AUTHORIZED.

Begin with the takeover audit.

Preserve Antigravity's Phase 1 work.

Then implement Phase 2.

Work ONLY inside:

E:\Works\Kano

Make Kano actually work.

Use real phone data.

Test on the real OnePlus.

Inspect the actual UI.

Fix what you find.

Repeat the loop until the phase is genuinely complete.

DO NOT STOP AT A CODE-ONLY IMPLEMENTATION.
DO NOT FABRICATE RESULTS.
DO NOT DELETE USER DATA.
DO NOT BYPASS ANDROID SECURITY.

ONE VERIFIED PHASE AT A TIME.