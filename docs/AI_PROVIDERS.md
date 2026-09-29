# KANO — AI Provider Architecture & Contracts

---

## 1. Provider Abstraction (`:core`)

All AI capabilities are abstracted behind pure Kotlin interfaces in `:core`:

```
               ┌───────────────────────┐
               │    Kano AI Router     │
               └───────────┬───────────┘
                           │
             ┌─────────────┴─────────────┐
             ▼                           ▼
  ┌─────────────────────┐     ┌─────────────────────┐
  │ Local AI Provider   │     │ Remote AI Provider  │
  │ (On-device models)  │     │ (OpenAI / Gemini)   │
  └─────────────────────┘     └──────────┬──────────┘
                                         │
                              ┌──────────▼──────────┐
                              │  Privacy Firewall   │
                              └─────────────────────┘
```

---

## 2. Router Responsibilities

1. **Capability Selection**: Route requests based on needed capability (`REASON`, `OCR`, `SUMMARIZE`, `CLASSIFY`, `RESEARCH`, `EXTRACT`).
2. **Egress Boundary Check**: Pass outbound payloads through `PrivacyFirewall`.
3. **Availability State**: Report provider readiness (`Available`, `MissingCredentials`, `NetworkUnavailable`, `DisabledByUser`).
4. **Conflict Synthesis**: When multiple connected providers return responses, compare outputs, flag disagreements, and present a unified summary without fabricating consensus.

---

## 3. Account & Access Rules

- Official provider APIs only.
- No session scraping, cookie theft, or unauthorized browser token reuse.
- Shared logins do not imply permission; each integration requires explicit user authorization.
