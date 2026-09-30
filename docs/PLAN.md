# Current implementation plan

The full presentation migration has been implemented. Latest user direction is
refinement only; do not regenerate the project or replace the backend.

1. Finish runtime/visual validation of Kano itself on emulator and connected OnePlus.
2. Inspect every major route, light/dark, Glass OFF, hamburger and large fonts; fix
   concrete defects. Preserve actual data, actions, Room and credentials.
3. Record evidence, limits and final APK hash in TEST_STATUS.md and KANO_WORKLOG.md;
   keep physical-phone private imagery outside the repository.
4. Continue highest-priority actual product gaps after this UI checkpoint: Gallery
   intelligence and safe extraction/cleanup, then explicit wardrobe/product records
   using the shared local pipeline. Do not call unavailable model/provider screens
   complete feature implementations.

The user roadmap remains 1.0 Device+Media → 1.1 Gmail/notifications → 1.2 Style →
1.3 Shopping → 1.4 Perplexity → 2.0 graph → 2.1 Android capabilities → 3.0 local AI.
Dates and external API availability are not assumed. Cloud work needs actual adapter
implementation, account/credential setup and reviewed request consent before enabling
INTERNET. Original deletion requires fresh item-level review, durable saved extraction
and supported OS confirmation; current UI does not delete original media.
