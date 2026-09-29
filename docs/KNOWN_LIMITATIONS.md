# Known limitations

- Development build; no production readiness claim.
- No OCR/QR, knowledge vault, outfit analysis, product scan, quantities inferred from
  photos, real shopping data, account login, notifications or live AI provider.
- Care is a manual list, capped at 200 entries: name (120 characters), category (60),
  explicit status. No automatic purchase recommendations, medical guidance or expiry inference.
- Database is app-private, not additionally encrypted. Credentials use a separate
  Keystore adapter; no real credential entry is enabled. Hardware backing is not guaranteed.
- Media: 100 selected image/video documents, 100 MiB hashing cap, literal filename search.
  Provider reads may block; cancellation is cooperative, not an enforced timeout.
  Same-size changes during hashing are not fully detected. Hash equality cannot authorize deletion.
- Firewall matching is heuristic, not complete DLP. Router has no multi-provider synthesis.
- Glass is surface translucency, not backdrop blur. Background waves stop when motion
  is reduced or activity is not resumed. Full motion/performance benchmarks remain open.
- Pending physical OnePlus, API 26, TalkBack, large datasets, process-death failure matrix.
- Historical screenshots and prior worklog entries may contain the fabricated baseline UI;
  use recovery screenshots and current source. Earlier 'KNOWN_ISSUES: None' was inaccurate.
