============================================================
MEDIA 2.0 — KANO SMART GALLERY + LENS + MEDIA INTELLIGENCE
============================================================

CRITICAL CHANGE:

DO NOT use the current:

"Choose photos or videos"
"OpenMultipleDocuments"
"select files individually"

model as the primary Media experience.

Kano Media must behave like a REAL PHONE GALLERY.

User should open:

MEDIA

and immediately see the accessible phone media library in a
high-quality gallery/timeline interface after the required Android
media permission has been granted.

Kano must NOT require the user to manually select every photo/video.

============================================================
1. MEDIA ACCESS MODEL
============================================================

Use Android MediaStore for the device media library.

For Android 13+:

READ_MEDIA_IMAGES
READ_MEDIA_VIDEO

For Android 14+:

READ_MEDIA_VISUAL_USER_SELECTED

Support:

FULL ACCESS
PARTIAL ACCESS
DENIED
REVOKED
UNAVAILABLE

Do NOT hide the permission state.

Do NOT repeatedly ask for permission.

Request access contextually when the user enters Media for the
first time and clearly explain:

"Kano needs photo and video access to organize, understand,
search and help clean your gallery."

After full access is granted:

Kano can continuously query MediaStore and display the gallery.

On partial access:

show the accessible collection and provide:

"Manage media access"

so the user can grant more media.

On denial:

Media remains usable with a clear empty/permission state.

NEVER pretend the whole gallery is available when it is not.

Android 14+ allows users to switch between full and selected-photo
access, so Kano must re-check the permission state on lifecycle resume
rather than permanently assuming the previous state. 
============================================================
2. KANO GALLERY
============================================================

Media opens directly into:

KANO GALLERY

not a file picker.

Primary views:

PHOTOS
VIDEOS
ALBUMS
PEOPLE / OBJECTS only when reliably detected
DOCUMENTS
SCREENSHOTS
DOWNLOADS
FAVORITES
RECENT
LARGE FILES
DUPLICATES
RECENTLY ADDED
ARCHIVED / SAVED KNOWLEDGE

Timeline:

Today
Yesterday
28 Sep
27 Sep
...

Use actual MediaStore dates.

Grid should feel like a premium phone gallery:

large thumbnails
mixed aspect ratios where appropriate
video duration
selection state
smooth scrolling
lazy loading
thumbnail caching
image previews
video previews where appropriate

Do not generate fake media.

============================================================
3. MEDIA DASHBOARD
============================================================

At the top show real information:

Photos: X
Videos: X
Total media size: X
Recently added: X
Potential duplicates: X
Large files: X
Screenshots: X

Only display values that were actually computed.

Never invent counts.

Show:

"Updated just now"

or

"Indexed 2 minutes ago"

depending on actual freshness.

============================================================
4. AUTOMATIC MEDIA UNDERSTANDING
============================================================

Once media access is granted, Kano should maintain a local
media intelligence index.

Pipeline:

MediaStore
→ metadata
→ thumbnail
→ classification
→ OCR when useful
→ QR detection
→ entity extraction
→ duplicate analysis
→ semantic grouping
→ knowledge extraction
→ cleanup recommendation

Use progressive/background processing.

Do NOT process every full-resolution image unnecessarily.

Start with:

metadata
thumbnail
basic classification

Then perform deeper analysis only when justified.

============================================================
5. WHAT KANO SHOULD UNDERSTAND
============================================================

Analyze images/videos where useful and technically supported.

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
ALBUM
BOOK
GAME
TUTORIAL
MEME
ADVERTISEMENT
QR
SOCIAL CONTENT
IMPORTANT
TEMPORARY
UNKNOWN

These are classifications, not facts about people.

Never infer sensitive characteristics unnecessarily.

============================================================
6. OCR / GOOGLE-LENS-LIKE EXTRACTION
============================================================

Create:

KANO VISION

It should perform functions similar in spirit to a lens-style
information extraction system.

For images:

OCR
QR detection
URL extraction
text extraction
document recognition
product/entity extraction
screen-content understanding
visual category detection

Where possible use local/on-device processing first.

Existing ML Kit OCR/QR infrastructure should be reused where sound.

Do not pretend Kano is literally Google Lens.

Kano should provide its own local intelligence pipeline.

============================================================
7. SCREENSHOT INTELLIGENCE
============================================================

Automatically detect likely screenshots and analyze them.

Examples:

Website screenshot
→ OCR URL
→ validate URL
→ Website entity
→ Open
→ Save
→ Research
→ optionally remove screenshot

Movie screenshot
→ identify likely title
→ verify when needed
→ save Movie entity
→ watchlist

Song screenshot
→ identify song/artist when reliable
→ save Song entity

Book screenshot
→ identify title/author when reliable
→ save Book entity

Product screenshot
→ identify product when reliable
→ save Product
→ research current price when requested

Study screenshot
→ OCR
→ classify topic
→ save Study Knowledge

QR screenshot
→ decode QR
→ classify type
→ save payload if useful

Never fabricate identity.

Never automatically make a payment.

============================================================
8. MOVIE / SERIES / ANIME MEMORY
============================================================

Kano must remember media discovered in the gallery.

Example:

User:
"What movies do I have saved?"

Kano searches Knowledge Vault + media evidence.

Return:

MOVIES FOUND

Movie A
Movie B
Movie C

with:

source image
detected title
year/metadata if verified
watchlist state
current legitimate watch options when researched

Likewise:

series
anime
songs
albums
books
games

User can say:

"Save this."
"Remind me later."
"Show details."
"Where can I watch it?"
"Give me similar movies."
"Show my saved games."

All saved information must have provenance.

============================================================
9. DISCOVER / RECOMMENDATION MEMORY
============================================================

When Kano detects recommendations in screenshots/media:

extract
→ verify
→ structure
→ save

Examples:

movie recommendation
song recommendation
game recommendation
book recommendation
product recommendation
website recommendation
course recommendation
app recommendation

Allow:

SAVE
DISMISS
NOT INTERESTED
SHOW SIMILAR
DO NOT RECOMMEND AGAIN

Never silently turn every detected item into a recommendation.

============================================================
10. VIDEO INTELLIGENCE
============================================================

For videos:

metadata first

Then, when useful:

representative frames
OCR
audio/transcript when authorized and justified
scene/content classification
entity extraction

Classify:

personal
travel
tutorial
study
project
social
movie
series
anime
music
advertisement
meme
unknown

Do not fully decode every large video unnecessarily.

Use battery/CPU-aware staged processing.

============================================================
11. AUTO ORGANIZATION
============================================================

Kano should organize media by:

date
type
source when reliably known
topic
semantic category
album
duplicate group
large files
screenshots
documents
saved knowledge

Do NOT physically move files without explicit user action.

Virtual organization is preferred.

============================================================
12. DUPLICATE INTELLIGENCE
============================================================

Detect:

exact duplicates
near duplicates where technically reliable
repeated screenshots
repeated downloaded media
similar photos

Display:

ORIGINAL
DUPLICATE
SIMILAR

Do not automatically delete.

For every cleanup recommendation show:

file
location
size
reason
duplicate relationship
confidence

============================================================
13. SMART CLEANUP
============================================================

Create:

CLEAN MY GALLERY

Flow:

ANALYZE
→ CLASSIFY
→ REVIEW
→ SELECT
→ CONFIRM
→ DELETE
→ VERIFY

Possible cleanup groups:

DUPLICATES
LARGE VIDEOS
OLD SCREENSHOTS
MEMES
ADVERTISEMENT IMAGES
TEMPORARY DOWNLOADS
BLURRY/LOW-VALUE CANDIDATES
REPEATED MEDIA
EXPIRED/OUTDATED FILES where objectively identifiable

IMPORTANT:

Do NOT call something unwanted just because AI thinks it is
unimportant.

Use:

POTENTIAL CLEANUP
and explain the evidence.

Personal photos/videos must never be silently deleted.

============================================================
14. EXTRACT-BEFORE-DELETE
============================================================

Before deleting a media item that contains useful knowledge:

EXTRACT
→ VERIFY
→ SAVE TO VAULT
→ SHOW USER
→ CONFIRM DELETE

Example:

Screenshot:
"Top 5 React libraries"

Kano extracts the useful information.

Then:

Knowledge saved.

"This screenshot can now be deleted."

Only after explicit confirmation should deletion occur.

============================================================
15. DELETE IMPLEMENTATION
============================================================

Use supported Android MediaStore/document-provider deletion flows.

For media deletion:

show selected items
→ request/perform supported OS confirmation
→ delete/trash
→ re-query MediaStore
→ verify actual result

If Kano lacks permission:

show system route.

Never show "Deleted successfully"
unless verification confirms it.

Do not use private filesystem tricks.

============================================================
16. SEARCH
============================================================

Create powerful Media search.

Examples:

"Show my food videos."

"Find screenshots with websites."

"Find my DBMS notes."

"Show large videos."

"Find duplicate photos."

"Show movie screenshots."

"Find QR codes."

"Show photos from my college."

"Find product screenshots."

"Show saved game recommendations."

Search metadata + local extracted knowledge.

============================================================
17. NATURAL-LANGUAGE MEDIA CONTROL
============================================================

User:

"Show me all screenshots from last week."

→ generate filtered media view.

"Find videos larger than 1 GB."

→ generate large-video view.

"Find all movie recommendations."

→ generate recommendation view.

"Clean duplicate photos."

→ generate review interface.

"Show things Kano learned from my screenshots."

→ generate Knowledge results.

This is GENERATIVE MEDIA UI.

============================================================
18. MEDIA DETAILS PAGE
============================================================

Opening any media item should provide:

full preview
filename
date
size
dimensions
duration
format
location/collection where available
favorite state
duplicate information
classification
OCR text
detected URLs
detected QR
detected entities
saved knowledge
privacy sensitivity
processing status

Do not display metadata Android cannot actually provide.

============================================================
19. PHOTO DETAILS
============================================================

Show:

resolution
orientation
date/time where available
size
format
location only if available/authorized
camera metadata only if actually present
AI/local classifications
OCR
objects/entities where supported
related Vault records

============================================================
20. VIDEO DETAILS
============================================================

Show:

duration
resolution
size
format
date
thumbnail
processing status
detected text
classification
related knowledge

============================================================
21. STYLE INTEGRATION
============================================================

Media and Style must share the same media intelligence layer.

User can say:

"Use my wardrobe photos."

Kano searches the actual accessible gallery.

Then:

detect clothing
→ classify garments
→ colors
→ categories
→ accessories
→ footwear
→ build wardrobe records

Categories:

tops
bottoms
outerwear
shoes
bags
watches
accessories

Never invent clothing items.

============================================================
22. DRESS-ME / STYLE STUDIO
============================================================

Flow:

GALLERY / CAMERA
→ SELECT OR CAPTURE
→ SCAN
→ ANALYZE
→ OUTFIT
→ REVIEW
→ RECOMMEND
→ AI PREVIEW
→ SAVE

User may select several photos.

Kano should combine evidence across them.

Show:

WHAT I FOUND

shirt
trousers
shoes
watch
bag
accessories

with confidence/provenance.

Then:

WHAT GOES TOGETHER

based on:

color coordination
layering
fit consistency
occasion
footwear
accessories
user preferences

No attractiveness judgment.

============================================================
23. CARE / BEAUTY INVENTORY
============================================================

Media can feed the Care system.

Scan product photos.

Identify where reliable:

brand
product
category
variant
size
visible ingredients
expiry
batch

Categories:

SKINCARE
HAIR
GROOMING
ORAL
EVERYDAY

Build inventory from actual detected evidence.

User can verify/edit each item.

Never fabricate quantities or ingredients.

============================================================
24. SKINCARE / CARE ASSISTANCE
============================================================

Kano can help organize:

morning routine
evening routine
college/work routine
travel routine
product inventory

It can remind the user what products they already have.

Anti-overconsumption logic:

ALREADY OWN
DUPLICATE
NEED NOW
NEED SOON
OPTIONAL

Do not provide medical diagnosis.

============================================================
25. PRODUCT SCAN
============================================================

User can:

PHOTO
→ SCAN PRODUCT
→ IDENTIFY
→ VERIFY
→ SAVE

Sources can include:

camera
gallery
screenshot
barcode
QR
OCR
authorized web/product sources

============================================================
26. AI SHOPPING / BUYHATKE-LIKE INTELLIGENCE
============================================================

Create:

KANO SHOPPING INTELLIGENCE

User can ask:

"Find this product cheaper."

"Is this a good time to buy?"

"Compare these."

"Find alternatives."

"Do I already own something similar?"

"Show the cheapest legitimate option."

"Show best match for my budget."

"Track this price."

Use current authorized product/web sources.

For each product:

name
variant
seller
price
availability
shipping where available
return policy where available
warranty where available
rating/review evidence where available
timestamp
source

Clearly distinguish:

CHEAPEST
BEST VALUE
BEST MATCH
ALTERNATIVE

Never fabricate price history.

Never automatically purchase.

Before recommending:

check existing Care/Wardrobe inventory.

============================================================
27. PRICE TRACKING
============================================================

If the user explicitly saves a product:

PRICE WATCH

Track using supported/current sources.

Show:

current price
previous observed price
observed date
seller
availability

Never invent historical prices.

Network connectivity is required for live shopping information.

============================================================
28. MEMORY
============================================================

Everything useful discovered from Media can become structured memory.

Examples:

Movie
Song
Book
Game
Product
Website
Course
Study topic
Outfit
Wardrobe item
Care product

Store:

source
timestamp
confidence
user confirmation
user edits

Then later:

"What was that game I saved?"

"What was that movie screenshot?"

"Which skincare products do I own?"

"What clothes do I have?"

"Which products did I want to buy?"

Kano should answer from its stored evidence.

============================================================
29. MEDIA → KNOWLEDGE → ACTION
============================================================

Core pipeline:

GALLERY
↓
UNDERSTAND
↓
EXTRACT
↓
STORE
↓
SEARCH
↓
RECOMMEND
↓
ACT

This is one of Kano's central differentiators.

============================================================
30. PRIVACY
============================================================

Full gallery access must still be explicit Android permission.

Kano must tell the user:

WHAT
WHY
WHEN
WHERE PROCESSED

Prefer on-device processing.

Do not upload gallery content automatically.

Cloud AI requires:

sensitivity analysis
request-bound consent
explicit authorization

Never inspect another application's private storage.

Never bypass Android permissions.

============================================================
31. MEDIA UI
============================================================

Media UI should visually combine:

premium gallery
AI search
glass
contextual surfaces
timeline
large imagery
fluid transitions

Avoid:

giant dashboard cards
boring file lists
per-file permission buttons
permanent OCR buttons everywhere

Primary experience:

REAL MEDIA.

============================================================
32. MEDIA ANIMATION
============================================================

Use:

thumbnail shared transitions
smooth zoom into image
gallery-to-detail morph
selection animation
video reveal
scan animation
OCR reveal
knowledge extraction animation
duplicate grouping animation
cleanup review animation
delete verification animation
wave/ambient background

No fake AI scanning.

Processing animations must correspond to real work.

============================================================
33. PERFORMANCE
============================================================

Large galleries require:

Paging
Lazy grids
thumbnail caching
bounded bitmap memory
incremental indexing
background WorkManager jobs
cancellation
battery awareness

Never load the entire gallery into RAM.

Never analyze every full-resolution asset at once.

============================================================
34. LIVE LIBRARY
============================================================

MediaStore data must be refreshed when Kano resumes.

Where useful, use content observers/change signals rather than
constant polling.

New media should appear without requiring a destructive rebuild.

Deleted media should disappear.

Changed permissions should be reflected.

============================================================
35. IMPORTANT ARCHITECTURAL REPLACEMENT
============================================================

The old Media architecture:

ACTION_OPEN_DOCUMENT
→ select up to 100 files
→ local filename index

must no longer be the primary gallery architecture.

Replace it with:

MEDIASTORE
→ REAL GALLERY
→ INCREMENTAL INDEX
→ MEDIA INTELLIGENCE
→ KNOWLEDGE VAULT
→ CLEANUP ENGINE

Keep SAF only for cases that genuinely require user-selected
documents/directories that MediaStore does not cover.

============================================================
36. REALITY CHECK
============================================================

Do not promise "Google Lens level" understanding unless the actual
implemented pipeline supports it.

Implement progressively:

PHASE A:
gallery + metadata + thumbnails

PHASE B:
OCR + QR + screenshot classification

PHASE C:
semantic media understanding

PHASE D:
knowledge extraction

PHASE E:
style/care/product intelligence

PHASE F:
shopping/research integrations

Each phase must work independently.

============================================================
37. UPDATE PRODUCT MAP
============================================================

Media is now a foundational intelligence layer for:

MEDIA
VAULT
STYLE
CARE
SHOPPING
DISCOVER
STUDY
AI SEARCH
DAILY LIFE

Do not build separate duplicate scanners for each section.

Create one shared Media Intelligence Engine.

============================================================
38. FINAL MEDIA ACCEPTANCE
============================================================

Open Kano.

Tap Media.

Expected:

REAL GALLERY.

Not:

"Choose photos or videos."

Grant media access once.

Then:

gallery appears
→ thumbnails load
→ dates organize
→ videos show duration
→ search works
→ analysis runs incrementally
→ OCR works
→ QR works
→ entities can be saved
→ Vault receives knowledge
→ duplicates are identified
→ cleanup can be reviewed
→ deletion is explicit and verified

Then test:

Camera
→ photo
→ Media
→ Style
→ Care
→ Vault

Everything must use the same underlying media intelligence layer.