# StakeFlow — Product Requirements Document (v1.0)

**Owner:** Amol
**Document status:** Draft for build
**Last updated:** 20 April 2026
**Platforms:** iOS, Android (Expo + EAS), Web (parity from day one)
**Stack:** Expo/React Native · React (web) · Supabase (DB, Auth, Storage, Edge Functions) · Render (backend services) · TypeScript · Zod

-----

## 1. Executive summary

StakeFlow is a mobile-first stakeholder management app for consultants and PMI-trained project managers. It replaces the static PowerPoint grid that PMs keep re-doing between meetings with a **living matrix** — a 2×2 board of Influence vs Engagement, a SEAM attitude layer, a server-owned Risk Score, and a premium AI layer that turns the board from a tracker into an advisor.

v1 ships three things worth paying for:

1. A **frictionless matrix** you can actually update during a meeting — one hand, drag, snap, done.
1. A **trusted, deterministic Risk Score** (Formula B) that explains *why* a stakeholder is a problem.
1. A **Premium AI tier** that produces a ranked daily brief, drift forecasts, and a message draft you can send in the next 30 minutes.

The north-star success metric is **weekly active stakeholder updates per consultant** — the product works when consultants keep their boards alive between meetings, not when they build them once.

-----

## 2. Positioning and target user

**Primary user:** Independent and firm-based consultants, change-management leads, and PMI-certified project managers running stakeholder-heavy programs (transformation, M&A, regulatory, tech implementation).

**What they know already:** The PMI stakeholder framework — Influence/Engagement as axes, SEAM (Unaware → Resistant → Neutral → Supportive → Leading) as attitude, and the idea that stakeholder posture is dynamic, not static.

**What breaks their current workflow:**

- Matrix lives in a slide that ages the moment the meeting ends.
- No shared language for “how big a problem is this person” across a portfolio.
- No mechanism to prompt action between weekly stand-ups.
- AI tools they try are generic ChatGPT prompts — not grounded in the PMI framework.

**Not the target user:** Enterprise PMOs wanting SSO-gated collaboration, Jira integrations, or corporate email sync. StakeFlow is a **standalone consultant tool**, distributed via public app stores, explicitly outside corporate ecosystems.

-----

## 3. Scope boundaries for v1

### In scope

- Single-user projects (one consultant owns their own boards).
- Matrix view, list view, gaps view, stakeholder detail view.
- SEAM tracking, Influence/Engagement scoring, Risk Score (Formula B).
- Notes log with typed entries, decoupled from interaction dates.
- Premium AI tier: stakeholder briefs, drift forecast, confidence scoring, message draft, portfolio “top 5 this week.”
- Export: matrix as PNG/PDF.
- iOS, Android, Web parity.
- Online-optimistic with queue-and-retry on reconnect.

### Out of scope (deferred to v2+)

- Multi-user projects, roles, sharing.
- Corporate integrations (Jira, MS Teams, Slack, Gmail, Outlook).
- “Opposed” SEAM state (requires SEAM UI redesign + migration).
- True offline-first with local SQLite sync (WatermelonDB / PowerSync).
- Volatility boost in risk scoring (requires historical delta tracking).
- Risk labels directly on matrix nodes, glow/pulse on Critical nodes.
- Microsoft OAuth (Apple + Google only at launch).
- Stakeholder archetype overlays (Timebomb, Saboteur, etc.) — design-validated but deferred to v1.1.

-----

## 4. Core domain model

### 4.1 Entities

**Project** — a container for a set of stakeholders. Owned by one user. Has name, description, created/updated timestamps.

**Stakeholder** — belongs to one project. Fields:

|Field                      |Type     |Notes                                                     |
|---------------------------|---------|----------------------------------------------------------|
|`id`                       |uuid     |PK                                                        |
|`project_id`               |uuid     |FK                                                        |
|`name`                     |string   |required                                                  |
|`role`                     |string   |optional                                                  |
|`organization`             |string   |optional                                                  |
|`is_internal`              |boolean  |default true                                              |
|`influence`                |float 0–1|Y-axis                                                    |
|`engagement`               |float 0–1|X-axis                                                    |
|`seam_state`               |enum     |`unaware`, `resistant`, `neutral`, `supportive`, `leading`|
|`desired_seam_state`       |enum     |same enum; the gap drives node colour                     |
|`risk_score`               |int 0–100|server-computed, read-only, nullable for legacy rows      |
|`last_interaction_at`      |timestamp|optional, user-set                                        |
|`last_interaction_recap`   |text     |optional, decoupled from date                             |
|`created_at` / `updated_at`|timestamp|system                                                    |

**Note** — belongs to one stakeholder. Typed log entry. Fields: `id`, `stakeholder_id`, `type` (enum: `meeting`, `project_update`, `observation`), `body` (text), `occurred_at` (timestamp, user-set), `created_at` (system).

**AI Brief** (premium only) — a cached, server-generated artifact. Fields: `id`, `stakeholder_id`, `generated_at`, `salience_score`, `drift_forecast_json`, `confidence`, `brief_markdown`, `message_draft`, `model_version`, `inputs_hash`.

### 4.2 Derived / computed

- **SEAM gap** — `desired_seam_state.level − seam_state.level`. Drives **node colour** on matrix (green = 0, red = 3+).
- **Risk Score (Formula B)** — drives **node size** on matrix. See §6.
- **Salience Score (Premium)** — power + legitimacy + urgency blend. See §7.

-----

## 5. Functional requirements

### 5.1 Matrix Board view (primary surface)

**Layout**

- 2×2 grid occupies the top 60% of the mobile viewport; full canvas on web.
- Y-axis: Influence (low → high, bottom → top).
- X-axis: Engagement (low → high, left → right).
- Quadrants (corrected):
  - **Top-left: Danger Zone** — high influence, low engagement.
  - **Top-right: Manage Closely** — high influence, high engagement.
  - **Bottom-left: Minimal Effort** — low influence, low engagement.
  - **Bottom-right: Keep Informed** — low influence, high engagement.

**Nodes**

- Colour = SEAM gap (green → amber → red).
- Size = Risk Score band (5 tiers, 0–20, 21–40, 41–60, 61–80, 81–100).
- Size transitions animate at ~300ms ease-out.
- Tap → open Stakeholder Detail bottom sheet.
- Long-press + drag → reposition on grid; influence/engagement update live.
- Pinch-to-zoom when stakeholders cluster.

**Interactions**

- Haptic feedback on pick-up, drop, and SEAM state change.
- Optimistic UI: node moves instantly; write queues if offline.
- Floating Action Button (bottom-right) → Add Stakeholder.
- Top-right AI icon (✨) → Global Board Analysis (premium).

### 5.2 List view

- Toggle from Matrix Board.
- Sortable columns: name, Risk Score, SEAM state, last interaction, SEAM gap.
- Default sort: Risk Score descending.
- Swipe left on a row → quick actions (log note, update SEAM, open detail).

### 5.3 Gaps view (portfolio-level)

- Lists all stakeholders where `desired_seam_state > seam_state`.
- Grouped by gap size (3-level gap first, then 2, then 1).
- Each row shows name, current SEAM, desired SEAM, days since last interaction, Risk Score.
- Designed to answer “who do I need to close the gap on this week?”

### 5.4 Stakeholder Detail (bottom sheet)

**Snap points:** 25% (peek), 50% (edit), 90% (AI reading).

**Sections:**

1. **Header** — name, role, organization, internal/external badge, Risk badge (band + label + colour).
1. **Matrix controls** — haptic sliders for Influence and Engagement, SEAM selector (current + desired).
1. **Last interaction** — date picker + optional recap field (decoupled; either can be empty).
1. **Notes log** — reverse-chronological, typed entries (meeting / project update / observation), inline add.
1. **Premium AI section** (if entitled) — brief, drift forecast, recommended action, message draft, confidence + explainability.

**Risk badge** — updates live as the user drags sliders or changes SEAM, *before save*, by running the pure function locally.

### 5.5 Add Stakeholder flow

- FAB → modal sheet.
- Required: name. Everything else optional.
- Default placement: centre of grid (0.5, 0.5), SEAM = `neutral`, desired SEAM = `supportive`.
- Risk Score computes on save.

### 5.6 Export

- Matrix View → PNG or PDF via `react-native-view-shot` (mobile) / `html2canvas` or server-side render (web).
- Includes legend (SEAM colour key, risk size key).
- Share sheet handoff to native share on mobile; download on web.

### 5.7 Authentication

- Magic link (existing, retained).
- Apple Sign-In (iOS + web; mandatory on iOS if any other social login ships).
- Google Sign-In (iOS, Android, web).
- Microsoft → deferred to v1.1.
- All OAuth flows routed through Supabase Auth.

-----

## 6. Risk Score — Formula B (free tier, canonical)

The deterministic, server-owned risk number. Same formula runs on mobile, web, and server.

**Formula**

```
Risk = [(0.5 × I) + (0.3 × (100 − E))] × S + (I × (100 − E) × S) / 200
```

Where:

- `I` = influence × 100
- `E` = engagement × 100
- `S` = SEAM risk factor

Result rounded to nearest integer, clamped to 0–100.

**SEAM risk factors**

|SEAM state|Factor|
|----------|------|
|leading   |0.05  |
|supportive|0.20  |
|neutral   |0.50  |
|resistant |0.80  |
|unaware   |0.90  |

**Bands**

|Range |Label   |Colour          |
|------|--------|----------------|
|0–20  |Low     |Green           |
|21–40 |Watch   |Yellow          |
|41–60 |Medium  |Orange          |
|61–80 |High    |Red             |
|81–100|Critical|Dark red / flame|

**Contract rules**

- Implemented as a pure function in `packages/shared` (TypeScript), consumed by Expo app, web app, and API.
- `risk_score` column on `project_stakeholders`, nullable for migration safety.
- Client-side: null → 0 for display/sizing.
- **Read-only to clients** — Create/Update API schemas strip any client-sent `risk_score`. Only the server persists it.
- Computed on every create and every update, after validation/normalization.

-----

## 7. Premium AI tier (Salience + Drift + Brief)

Premium is **not** a chatbot. It is a decision-support layer that answers three questions for the consultant:

1. Who matters most right now?
1. What is changing?
1. What do I do before the next meeting?

### 7.1 Salience Score

A weighted blend of three PMI-grounded inputs:

- **Power** — derived from Influence axis value, role signals (seniority terms in job title), and user-flagged “decision owner” boolean.
- **Legitimacy** — derived from internal/external flag and stakeholder type.
- **Urgency** — derived from days-to-next-milestone (project-level), recent note recency, and SEAM state movement in the last 30 days.

Salience is separate from Risk. Risk says *“this person is in a bad state.”* Salience says *“this person deserves your attention this week.”* A consultant uses Risk to triage and Salience to sequence.

### 7.2 Drift forecast

A probabilistic prediction of SEAM movement in the next 7, 14, and 30 days, produced by the LLM using:

- SEAM state history.
- Notes log (sentiment trajectory, action completion).
- Time since last interaction.
- Recent Risk Score trajectory.

Output format: `"38% chance of drifting Neutral → Resistant within 14 days unless engaged."`

### 7.3 Confidence score

Every AI output carries a confidence value (0.0–1.0) based on four provenance inputs:

1. Interaction recency and volume.
1. Sentiment consistency across notes.
1. Action completion rate (logged follow-ups vs logged outcomes).
1. SEAM update staleness.

Low confidence does not suppress the output — it flags it and explains why.

### 7.4 Stakeholder brief

One-tap artifact per stakeholder. Rendered as mobile markdown, copy-to-clipboard enabled. Contains:

- Priority rank (within this project).
- Current SEAM, desired SEAM, gap.
- Salience score and band.
- Risk score and contributing signals.
- Drift forecast.
- Confidence score + explainability paragraph.
- Likely primary concern (LLM hypothesis, labelled as such).
- Recommended next action, preferred channel.
- **Message draft** — consultant-ready, short, in the user’s declared tone.
- Watchouts list.

### 7.5 Portfolio Intelligence (Global Board Analysis)

Premium-only screen. Answers:

- **Top 5 to engage this week** — ranked by Salience × Risk × drift probability.
- **Concentration of risk in the Danger Zone** — count and list.
- **Portfolio SEAM gap** — weighted average gap across all stakeholders, trend line over last 90 days.

### 7.6 Entitlement and monetization

- Free tier: Matrix, List, Gaps, Risk Score, Notes, Export (watermarked), up to N stakeholders per project (N = 20 proposed).
- Premium tier: Unlimited stakeholders, all AI outputs, unwatermarked export, Portfolio Intelligence.
- Billing via RevenueCat on iOS and Android; Stripe on web.
- Server-enforced limits on export count and AI call count per billing period.

-----

## 8. UX and visual design

### 8.1 Design system — “Cartographic Night”

- **Palette:** deep navy background, teal / amber / violet / red accents (semantic, not decorative).
- **Typography:** DM Mono (numerics, scores, labels), DM Sans (body, headings).
- **Motif:** Connective Cartography — network diagrams, orbital systems, influence-mapping imagery in empty states and onboarding.
- **Tokens:** semantic CSS variables so light and dark themes share the same component code.

### 8.2 Mobile-specific patterns

- `@gorhom/bottom-sheet` for detail view with three snap points.
- `react-native-reanimated` + `react-native-gesture-handler` for 60fps drag, pinch, and sheet physics.
- `expo-haptics` for tactile feedback on drag, SEAM change, save.
- `react-native-view-shot` + `expo-sharing` for export.
- Landscape: the bottom sheet becomes a right-side drawer; matrix rescales.

### 8.3 Web-specific patterns

- Hover states on nodes (tooltip preview with name + risk).
- Side panel instead of bottom sheet for detail view at viewport ≥ 1024px.
- Keyboard shortcuts: `N` new stakeholder, `/` search, `G` gaps view, `L` list view, `M` matrix view.

-----

## 9. Technical architecture

### 9.1 Client stack

**Mobile:** Expo SDK (managed workflow), EAS Build + EAS Submit, React Native, TypeScript, Zustand (state), react-native-reanimated, react-native-gesture-handler, @gorhom/bottom-sheet, expo-haptics, expo-sqlite (for write queue), react-native-view-shot, expo-sharing.

**Web:** Vite, React, TypeScript, Zustand, shared component library where feasible (pure logic + design tokens), separate layout layer.

**Shared:** `packages/shared` — domain types, Zod schemas, Formula B, SEAM enum, band mapping, colour mapping. Consumed by mobile, web, and API.

### 9.2 Backend

- **Supabase** — Postgres (primary data), Auth (magic link + OAuth), Storage (export artifacts, user avatars), Row-Level Security (user_id-scoped on all tables).
- **Render** — Node.js service for:
  - AI orchestration (LLM calls, caching, rate limiting, cost controls).
  - RevenueCat / Stripe webhooks.
  - Export rendering if server-side rendering is chosen for PDF.
- **LLM provider** — Anthropic Claude via server-side API. API key never touches the client. (Open question — confirm in §13.)

### 9.3 Data flow

1. Client issues mutation (create/update stakeholder).
1. Zod validates at client boundary.
1. Write goes to Supabase (direct) or Render (for anything needing secrets).
1. Server re-runs Zod, re-runs Formula B, persists.
1. Realtime channel pushes update to all authenticated sessions for that user.
1. Premium AI calls go through Render, which checks entitlement, rate limits, hits LLM, caches result keyed by `inputs_hash`.

### 9.4 Offline strategy (v1)

**Online-optimistic with queue-and-retry.**

- UI mutations apply instantly against local Zustand state.
- Mutations are queued in a small `expo-sqlite` outbox (mobile) / IndexedDB (web).
- Queue flushes on reconnect, FIFO, with server-side idempotency keys (`client_mutation_id`).
- Conflict policy for v1: **last-write-wins**, server timestamp authoritative. Acceptable because v1 is single-user.
- Banner when offline: *“Working offline — changes will sync when you reconnect.”*
- Read cache: last-seen stakeholders and notes for the active project are retained locally so the matrix still renders offline.

Not implementing WatermelonDB or a full sync engine in v1 — the engineering cost outweighs the benefit for a single-user app where the primary offline use case is a 60-minute meeting, not a 6-hour flight.

### 9.5 Security

- Supabase RLS policies on every table, keyed to `auth.uid()`.
- No service-role keys on clients, ever.
- `risk_score` API-stripped on input.
- Secrets in Doppler, injected into Render and EAS.
- Sentry for error monitoring, with PII scrubbing.
- Screenshot masking on iOS / Android for authenticated screens (optional setting, on by default for Premium users).
- Snyk on CI for dependency scanning.

### 9.6 CI/CD

- GitHub Actions pipeline: lint → type-check → Jest (logic) → Maestro (mobile flows, smoke suite only for v1) → build.
- EAS Build for iOS and Android.
- TestFlight (iOS internal + external beta) and Google Play Internal Testing as release gates.
- Web deploys via Render or Vercel (to confirm).

-----

## 10. Non-functional requirements

|Attribute                    |Target                                         |
|-----------------------------|-----------------------------------------------|
|Matrix interaction frame rate|60fps on iPhone 12 and equivalent Android      |
|Risk badge update latency    |<16ms (single frame) — pure local compute      |
|Cold start (mobile)          |<2.5s on iPhone 12                             |
|AI brief generation          |<8s P95 end-to-end                             |
|Crash-free session rate      |≥99.5%                                         |
|Offline write durability     |100% — no queued mutation ever silently dropped|
|API availability             |99.5% monthly                                  |

-----

## 11. Analytics and instrumentation

Track, at minimum:

- Stakeholder created / updated / deleted (project scope, not PII).
- SEAM state changes (from/to).
- Risk band transitions.
- AI brief requested, delivered, copied, messaged.
- Export triggered (format, result).
- Offline queue flush events (count, duration, errors).
- Premium conversion funnel: paywall seen → trial started → subscribed.

Tooling: PostHog or Amplitude (decide in §13).

-----

## 12. Release plan

### Phase 1 — Foundation hardening (weeks 1–3)

- Shared package consolidation: types, Zod, Formula B, SEAM constants.
- Supabase migration for `risk_score` column, RLS audit.
- API input stripping for `risk_score`.
- Write queue implementation (mobile + web).
- Sentry, Doppler, Snyk wired.
- GitHub Actions skeleton (lint + type-check + Jest).

### Phase 2 — Mobile + Web UI parity (weeks 4–7)

- Four wireframes integrated on mobile: Matrix, List, Gaps, Detail.
- Web parity for the same four screens.
- Risk badge (live-updating, pre-save) on both platforms.
- Animated matrix node sizing on both platforms.
- Export flow end-to-end.

### Phase 3 — Premium AI (weeks 8–11)

- Salience Score computation (deterministic + LLM-augmented).
- Drift forecast pipeline.
- Brief generation + caching.
- Confidence scoring.
- Portfolio Intelligence screen.
- RevenueCat + Stripe entitlement plumbing.

### Phase 4 — Auth expansion + beta (weeks 12–13)

- Apple Sign-In and Google Sign-In.
- Closed beta via TestFlight + Google Play Internal Testing.
- Web beta via invite-only URL.

### Phase 5 — Public release (week 14+)

- Public App Store + Play Store submission.
- Web go-live.
- Go/no-go gate: defined go/no-go criteria met (see §14).

-----

## 13. Open questions (resolve before Phase 3)

1. **LLM provider** — Anthropic Claude is the assumed default. Confirm vs OpenAI or a multi-provider setup.
1. **Analytics vendor** — PostHog (self-hostable, generous free tier) vs Amplitude (richer product analytics) vs Mixpanel.
1. **Web hosting** — Render (same env as backend) vs Vercel (better DX for Vite + React).
1. **Tone personalization** — do we let users pick a tone preset for message drafts (formal / warm / direct), or infer from their own notes? v1 proposal: preset, three options.
1. **Free tier limit** — 20 stakeholders per project proposed. Validate against beta cohort before lock.
1. **Export watermarking** — confirm acceptable watermark copy and placement on free tier.

-----

## 14. Go / no-go criteria for public release

Must all be true:

- Crash-free sessions ≥99.5% across 7-day rolling window in closed beta.
- All P0 Maestro smoke tests passing on iOS and Android.
- RLS audit signed off — no cross-user data leakage observed in penetration test.
- RevenueCat + Stripe webhook round-trip verified in production sandbox.
- AI brief P95 latency <8s sustained over 1,000-call sample.
- Offline write queue: zero dropped mutations across 500-test run.
- App Store Review Guidelines compliance checklist complete (especially §3.1.1 for subscriptions and §5.1 for data privacy).
- Sentry error budget healthy (no unresolved critical errors older than 48h).

-----

## 15. Principles (editorial)

These are the principles Amol has already landed on. They are load-bearing for the PRD.

1. **Comprehension before tooling.** Understanding the existing codebase architecture takes priority over adding new infrastructure.
1. **Targeted testing over coverage metrics.** Critical paths only — no coverage chasing.
1. **Minimal viable CI/CD first.** Start lean, expand when pain shows up.
1. **Avoid redundancy in the data model.** SEAM gap on node colour removes the need for a Power/Dynamism axis. Sentiment charts were rejected because they duplicate SEAM.
1. **Decouple implicit couplings in UX.** Notes and interaction dates are independent. Risk Score and Salience are independent. Each dimension is editable alone.
1. **Feature freeze before professionalization.** Scope stabilizes before tests and tooling expand around it.
1. **Ground every AI output in something a consultant can defend in a steering committee.** No opaque scores. Every premium number has contributing signals the user can inspect.

-----

*End of document.*