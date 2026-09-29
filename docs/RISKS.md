# Technical risk register

| Risk | Severity | Mitigation / release gate |
|---|---|---|
| Secret detector misses sensitive content | Critical | Unknown/personal/financial fail closed; no Internet permission; heuristic not a DLP guarantee; adversarial policy tests |
| User-selected provider hangs / returns changing bytes | High | Bounded streams, cancellation, known-size check; hash evidence only, never auto-delete; test hostile/slow providers on device |
| Revocation or process death leaves stale metadata | High | Recheck grants each job; mark inaccessible and clear identifying fields; resumable unique work; verify process-death/revocation on device |
| Race between forget and worker write | High | Stop request plus shared repository mutex around scan and forget; cancelled queued jobs cannot re-add grants; instrumented race tests still needed |
| Database upgrade destroys data | Critical | Export schema, no destructive fallback; migration tests mandatory before schema 2 |
| Metadata leakage via backup/logging | High | Backup/transfer excluded; no raw logs, no analytics; encrypted sensitive vault deferred |
| Hash treated as permission to delete | Critical | Cleanup policy separate from executor; no delete implementation now; byte revalidation and OS consent later |
| Scope exceeds verifiable release | High | Versioned roadmap, explicit development availability; no empty integration buttons |
| Android API/OEM differences | High | Min/API checks; test API 26/35+, selected media, restricted providers, rotation/background/large fonts |
| Dependencies / target baseline aging | Medium | Pinned versions; validate AGP/Kotlin/SDK compatibility; vulnerability and store target audit before shipping |
| No physical device / emulator | High | Host build/lint/unit checks do not establish UX/runtime quality; connected checks remain release-blocking |
| Cloud consent becomes reusable blanket permission | Critical | Bind digest, provider, purpose, capability, expiry; no automatic fallback; production adapter needs revocation/one-use enforcement |

No cloud credentials, signing keys, OAuth application, distribution account, or model
licenses are assumed. These are integration prerequisites, not reasons to fake results.
