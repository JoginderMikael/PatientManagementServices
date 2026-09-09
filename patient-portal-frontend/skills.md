# Agentic skills for the patient-management frontend

Status: working agreement for AI agents and human contributors  
Last reviewed: 2026-09-09

## Purpose

This file defines the specialized capabilities an implementation agent should apply
when working in `patient-portal-frontend`. It is a project playbook, not permission
to change backend contracts, production infrastructure, compliance policy, or real
patient data. The agent must use synthetic data and preserve the security boundaries
described in `ARCHITECTURE.txt` and `API.txt`.

The goal is a restrained, credible hospital/patient-management frontend. Correct
identity, patient context, workflow state, accessibility and privacy matter more
than visual novelty.

## Global operating rules

Every agent working here must:

1. Read `ARCHITECTURE.txt`, `API.txt`, `package.json`, the relevant feature code,
   and existing tests before editing.
2. Inspect the corresponding backend controller, DTO, model and security annotation
   before assuming an API shape or role.
3. Route browser requests through the API gateway; never connect directly to a
   database or silently bypass authorization.
4. Use synthetic patients, identifiers, credentials and clinical data in fixtures,
   screenshots and tests.
5. Never log or persist bearer tokens, record-download contents, demographics,
   diagnoses, medications, labs, invoices or other PHI.
6. Make the smallest coherent change that completes the requested workflow.
7. Preserve unrelated work in a dirty working tree. Do not reset or overwrite it.
8. Treat 401, 403, 404, 409, 410 and 503 as distinct product states.
9. Add or update tests in proportion to the change and run typecheck/test/build.
10. Update `API.txt` when a contract changes and `ARCHITECTURE.txt` when a structural
    decision changes.
11. Call out backend gaps instead of hiding them with unsafe browser behavior.
12. Report assumptions, files changed, verification performed and remaining risk.

## Skill selection matrix

| Work requested | Primary skill | Supporting skills |
| --- | --- | --- |
| Add a page or workflow | Feature implementation | Clinical UX, API integration, testing |
| Add/update API calls | API contract integration | Auth/security, testing, documentation |
| Change navigation/layout | Frontend architecture | Design system, accessibility |
| Build clinical forms | Forms and validation | Clinical UX, accessibility, API integration |
| Handle login/roles | Authentication and session safety | Security/privacy, testing |
| Add dashboard/table | Clinical UX | Data state, accessibility, performance |
| Fix a frontend bug | Diagnosis and repair | Relevant domain skill, regression testing |
| Improve styling | Design system | Accessibility, responsive design |
| Prepare a release | Quality and release | Security/privacy, accessibility, documentation |

## Skill 1: Frontend architecture stewardship

### Use when

Adding routes, providers, shared state, feature boundaries, cross-cutting components,
dependencies, build configuration or substantial directory structure.

### Responsibilities

- Keep route composition in `src/app` and domain behavior in `src/features`.
- Separate server state, session state, URL state and local UI state.
- Prefer lazy route boundaries for staff specialties not needed by every user.
- Keep the API boundary centralized and prevent direct ad hoc `fetch` calls from
  deeply nested components.
- Prevent feature-to-feature circular imports. Shared domain-neutral components go
  in `components`; domain components remain inside their feature.
- Evaluate new dependencies for maintenance, accessibility, bundle size, security
  and whether the existing stack already solves the problem.
- Record consequential decisions in the architecture document or a short ADR.

### Required output

- A comprehensible directory/route change.
- A short explanation of ownership and state flow.
- Tests proving providers and routes compose correctly.
- No unused abstraction created only for hypothetical future needs.

### Guardrails

- Do not introduce micro-frontends for this initial application.
- Do not put all server data in a global mutable store.
- Do not mix multiple design systems or request libraries.
- Do not move authorization enforcement into the browser.

## Skill 2: Clinical product and workflow design

### Use when

Designing patient banners, queues, clinical charts, medication actions, appointment
flows, safety alerts, task state, billing state or other hospital workflows.

### Responsibilities

- Identify the user role, patient context, entry condition, happy path, alternate
  state, safety consequence and completion evidence.
- Make the patient identity and lifecycle status persistent on staff patient pages.
- Show allergies, active alerts, signed-note state, overdue work and safety conflicts
  with text and iconography, not color alone.
- Require deliberate confirmation for destructive or consequential operations.
- Keep workflow status names aligned with the backend; never invent success states.
- Prefer focused task pages over generic dashboards containing unrelated metrics.
- Make timestamps, time zone, currency and accountable actor understandable.

### Workflow review questions

- Which patient/resource will change?
- Is the actor allowed by role and patient/facility context?
- Can the operation be safely retried?
- What does a 409 mean for this specific workflow?
- Does a warning need acknowledgement rather than dismissal?
- What evidence should the user see after completion?
- Could a stale tab cause action on the wrong patient?

### Guardrails

- Do not label a payer transmission, notification, record release or payment as
  complete unless the backend result establishes that state.
- Do not use optimistic UI for note signing, dispensing, task ownership, claims,
  legal holds, consent/revocation or patient merges.
- Do not hide clinical safety conflicts behind generic toast messages.

## Skill 3: API contract integration

### Use when

Adding queries, mutations, downloads, generated types, request headers or response
normalization.

### Procedure

1. Locate the operation in `API.txt`.
2. Verify it against the current controller, DTO, response model and live OpenAPI
   document when the service can run.
3. Confirm the gateway path, method, authentication, role, query/path parameters,
   content type, status and empty-body behavior.
4. Define or regenerate strict TypeScript input/output types.
5. Add one feature-level API function. It accepts typed domain input and returns a
   typed result rather than exposing raw `Response` objects.
6. Normalize error responses through the shared API error layer.
7. Add request-level tests for success and relevant error statuses.

### API client expectations

- Set the gateway base URL from environment configuration.
- Attach bearer credentials centrally.
- Use `application/fhir+json` for FHIR.
- Decode JSON, text and empty responses intentionally.
- Generate idempotency keys using a cryptographically strong UUID source.
- Reuse the same idempotency key only for a retry of the same logical payload.
- Abort obsolete reads during navigation or patient-context changes.
- Never blindly retry mutations.

### Dynamic response rule

Endpoints marked `dynamic` in `API.txt` return database-shaped maps. Narrow them at
runtime and keep their TypeScript types local/provisional. Prefer requesting an
explicit backend response DTO before using such a shape across multiple screens.

### Guardrails

- Do not guess field names from a mockup.
- Do not call service ports directly from browser code.
- Do not make a failed request appear successful using cached demo data.
- Do not send fields the API does not accept.

## Skill 4: Authentication, identity and session safety

### Use when

Implementing login, logout, session restoration, route guards, role-aware navigation,
break-glass UI or authorization error handling.

### Responsibilities

- Authenticate with `/auth/login` and validate restored credentials.
- Keep the token in the safest available storage consistent with the approved
  deployment design; prefer an HttpOnly-cookie BFF for production.
- Parse JWT claims defensively for presentation, never for enforcement.
- Centralize session lifecycle and role-to-navigation mapping.
- Clear all query caches and patient context on logout, identity change and 401.
- Preserve the session on 403 and show a stable access-denied page.
- Prevent redirect loops and avoid putting tokens/PHI in redirect URLs.
- Make emergency access explicit, time-bounded and visibly active.

### Tests

- Valid login and invalid login.
- Expired/restored token rejection.
- Each role sees only relevant navigation.
- Direct route entry is guarded.
- Backend 403 wins even if the frontend predicted permission.
- Logout and 401 purge cached patient data.

### Guardrails

- Never hard-code development credentials.
- Never put access tokens in localStorage without an explicitly documented and
  approved exception.
- Never auto-request or silently activate break-glass access.

## Skill 5: Server-state and concurrency management

### Use when

Implementing caching, refresh, query keys, pagination, mutation invalidation,
background loading or conflicting updates.

### Responsibilities

- Use identity- and patient-scoped query keys.
- Keep sensitive cache lifetimes short and persistence disabled.
- Render stale/background-refresh state without blanking usable screens.
- Cancel or ignore old responses after patient selection changes.
- Refetch authoritative data after important mutations.
- Interpret 409 as workflow/concurrency information and present a review path.
- Clamp UI query limits to backend limits; do not imply unavailable pagination.

### Guardrails

- Never share patient query data across authenticated identities.
- Never use a patient list response as authority after a merge/status mutation.
- Never auto-replay non-idempotent requests.
- Do not implement infinite scrolling for a backend that returns a fixed unpaged
  list without an explicit ordering/cursor contract.

## Skill 6: Forms, validation and safe mutation design

### Use when

Building registration, appointment, clinical, billing, insurance, pharmacy,
compliance or profile forms.

### Responsibilities

- Mirror backend required fields and constraints without pretending client checks
  replace server validation.
- Use explicit labels, descriptions, units and examples.
- Use appropriate controls for dates, times, UUID-backed selections, enums, money
  and multiline clinical text.
- Provide an error summary linked to fields and retain user-entered values after a
  rejected submission.
- Disable duplicate submission while pending.
- Review patient/resource, action and consequence before sensitive mutations.
- Clear a form only after confirmed success or explicit cancel/reset.

### Domain-specific rules

- Currency uses exactly three uppercase letters where required.
- Money uses decimal strings in form state, validated then serialized safely.
- Appointment duration is 5-480 minutes and start is future/present.
- Batch expiry is future and quantity is positive.
- Claim adjudication permits APPROVED or DENIED; DENIED requires zero approval.
- Proxy scope is one of RECORDS, APPOINTMENTS or PAYMENTS.
- Compliance transitions must follow the current case graph.

### Guardrails

- Never ask users to type opaque UUIDs when a permitted lookup can provide a safe
  selection control.
- Never preselect a destructive action.
- Never accept a hidden patient ID left over from a previous context.

## Skill 7: Clinical design system implementation

### Use when

Creating shared layout, typography, spacing, colors, status badges, tables, dialogs,
alerts, forms or responsive components.

### Responsibilities

- Define semantic tokens: surface, text, border, focus, info, success, warning,
  danger and clinical accent.
- Provide compact and comfortable density where helpful, with accessible defaults.
- Build a small primitive set before producing one-off component styles.
- Use semantic HTML first; add ARIA only where semantics are insufficient.
- Ensure statuses remain understandable without color.
- Keep focus management correct in dialogs and after mutations/navigation.
- Verify narrow mobile, tablet rounding and desktop staff layouts.

### Minimum primitives

`AppShell`, `PageHeader`, `PatientBanner`, `Button`, `TextField`, `Select`,
`TextArea`, `Checkbox`, `DateField`, `Table`, `StatusBadge`, `Alert`, `Dialog`,
`EmptyState`, `ErrorState`, `Skeleton`, `Tabs` and `DefinitionList`.

### Guardrails

- No inline-style sprawl like the current demo dashboard.
- No color-only risk or status indicators.
- No animation required to understand workflow state.
- No tiny icon-only action without an accessible name and visible discoverability.

## Skill 8: Accessibility engineering

### Use when

Adding or reviewing any interactive page or component. This skill is mandatory for
forms, tables, dialogs, tabs, menus, alerts and downloads.

### Responsibilities

- Target WCAG 2.2 AA.
- Verify keyboard order, visible focus, skip link and landmarks.
- Associate labels, help, units and errors with their inputs.
- Use captions/headers for data tables.
- Announce asynchronous results through appropriately restrained live regions.
- Restore focus after dialog close and focus the new page heading after navigation.
- Respect zoom, text resize, forced colors and reduced motion.
- Test status components for non-color comprehension.

### Evidence

- Automated accessibility test for reusable components/pages.
- Keyboard walkthrough notes for consequential flows.
- Manual screen-reader spot check for new interaction patterns.

## Skill 9: Security and privacy review

### Use when

Handling identity, PHI, downloads, external URLs, telemetry, storage, HTML content,
third-party libraries or any new data exposure.

### Threat checklist

- Credential theft through XSS or unsafe storage.
- PHI leaks through logs, URLs, analytics, browser persistence or error tools.
- Cross-patient cache reuse or stale patient context.
- Role escalation through hidden controls or editable identifiers.
- Unsafe rendering of clinical text or release content.
- Reverse-tabnabbing/open redirects from consultation links.
- CSRF if cookie authentication is introduced.
- Clickjacking and permissive content/security headers.
- Dependency compromise and unnecessary third-party scripts.

### Required behavior

- Escape text by default and sanitize only with an approved policy when rich text is
  genuinely required.
- Redact request/response data from telemetry.
- Display external destination/provider before navigating.
- Use `noopener noreferrer` on external links.
- Disable caching for record downloads and other highly sensitive responses.
- Keep security decisions server-side and fail closed on unknown access state.

### Guardrails

- Do not weaken gateway/service security for frontend convenience.
- Do not use production patient data for development.
- Do not claim HIPAA/regulatory compliance based on UI behavior or unit tests.

## Skill 10: Frontend testing and synthetic-data engineering

### Use when

Adding behavior, fixing defects, changing API contracts or preparing a release.

### Test layers

Component tests verify rendering, interaction, accessibility and validation.
Integration tests use request interception to verify full feature behavior.
End-to-end tests use a synthetic deployment for gateway/auth/workflow boundaries.

### Responsibilities

- Prefer user-observable assertions over implementation details.
- Build typed factories with clearly fictional names and identifiers.
- Model latency, empty results and important error codes in request handlers.
- Add a regression test before or alongside a bug fix when practical.
- Avoid brittle full-page snapshots; assert critical content and state.
- Control time/timezone for appointment and expiry tests.
- Verify no request fires with the previous patient ID after context switches.

### Minimum verification before handoff

  npm test -- --watchAll=false
  npm run build

Also run lint/typecheck when dedicated scripts are added. Report skipped checks and
the reason. A backend failure unrelated to the frontend should be reported, not
silently treated as a frontend pass.

## Skill 11: Performance and resilience

### Use when

Building high-volume tables, route bundles, repeated polling, charts, record views
or diagnosing slow/unstable UI behavior.

### Responsibilities

- Measure before optimizing.
- Lazy-load specialty route bundles.
- Avoid fetching every service for the initial shell.
- Debounce user-driven duplicate searches while preserving explicit submit.
- Bound refresh/polling and pause it when the tab is hidden where appropriate.
- Use stable keys and memoization only for demonstrated render cost.
- Show partial failure per panel when composed dashboards call multiple services.
- Ensure retries are bounded, cancellable and limited to safe reads.

### Guardrails

- Do not cache sensitive data persistently to improve perceived speed.
- Do not hide slow operations with false completion.
- Do not render thousands of rows when the backend offers no safe pagination plan;
  establish the server contract first.

## Skill 12: API and architecture documentation maintenance

### Use when

Changing routes, dependencies, feature ownership, API operations, schemas, roles,
headers, status codes or security behavior.

### Responsibilities

- Keep `API.txt` synchronized with controller/OpenAPI reality.
- Keep `ARCHITECTURE.txt` focused on durable decisions rather than implementation
  history.
- Update the frontend README with runnable setup instructions when tooling changes.
- Explain provisional/dynamic contracts and known backend gaps.
- Include dates and evidence for generated contract snapshots.
- Avoid copying secrets, real endpoint credentials or patient examples into docs.

### Contract drift procedure

1. Export live OpenAPI specifications through the gateway.
2. Diff paths and schemas against the prior snapshot.
3. Classify additive, compatible and breaking changes.
4. Regenerate types and update mocks.
5. Run affected tests and document migration needs.

## Skill 13: Diagnosis and repair

### Use when

The user asks why a frontend behavior fails, tests break, a request is rejected, or
the build no longer works.

### Procedure

1. Reproduce the smallest failing behavior.
2. Capture the exact route, role, patient context, request and status without
   exposing credentials or PHI.
3. Determine whether the fault lies in UI state, request construction, gateway
   routing, backend contract, authorization or environment.
4. Trace from user action to component, feature API function, shared client and
   controller contract.
5. State the cause with evidence before changing code.
6. Implement only when the request authorizes a fix.
7. Add a focused regression test and verify adjacent behavior.

### Guardrails

- Do not “fix” a 403 by exposing controls or disabling security.
- Do not convert all errors into 200/success state.
- Do not wipe caches/build outputs unrelated to the defect without checking scope.

## Skill 14: Quality and release readiness

### Use when

The user asks whether the frontend is complete, production-ready or releasable.

### Review gates

- Required workflows work against a synthetic integrated deployment.
- No hard-coded demo patient data remains in runtime code.
- Environment configuration is explicit and secrets are absent from bundles.
- Typecheck, unit/integration tests and production build pass from a clean checkout.
- Route-role tests and cross-patient denial tests pass.
- Automated and manual accessibility checks pass for critical workflows.
- Dependency and built-image vulnerability scans meet the approved threshold.
- CSP/security headers, cache policy and error telemetry are verified in deployment.
- Source maps follow the approved exposure policy.
- Performance budgets and supported browser/device matrix are recorded.
- API contract export matches the deployed backend revision.
- Independent privacy/security/release approval is recorded where required.

### Reporting vocabulary

Use precise conclusions:

- `implemented`: code exists, but verification or deployment may remain.
- `verified locally`: named checks passed in the current workspace.
- `integration verified`: synthetic gateway-to-service behavior passed.
- `release candidate`: engineering gates pass; external approvals may remain.
- `production approved`: only when the named authority has approved the deployment.

Never use “complete” without stating the scope and remaining gates.

## Multi-skill execution sequence

For a typical new feature, apply skills in this order:

1. Clinical product and workflow design defines the real user outcome.
2. API contract integration verifies that the backend can support it.
3. Frontend architecture stewardship places it in the correct boundary.
4. Authentication/session and security/privacy define access and data handling.
5. Forms/state/design-system skills implement the interaction.
6. Accessibility engineering validates the interaction model.
7. Testing builds regression evidence.
8. Documentation maintenance records contract/architecture changes.
9. Quality/release readiness verifies the deliverable.

Parallel work is appropriate only when file ownership is disjoint and contracts are
already stable. For example, an API-contract audit and visual component exploration
may run independently. Two agents should not simultaneously rewrite the router,
shared API client, authentication provider or the same feature directory.

## Handoff template

Every implementation handoff should include:

```text
Outcome:
  What user-visible capability now works.

Scope:
  Routes, roles and patient context affected.

Files changed:
  Concise list of relevant files.

API contracts:
  Operations used or changed; dynamic/provisional shapes noted.

Safety/privacy:
  Credential, PHI, cache, confirmation and error behavior.

Verification:
  Exact commands and results.

Remaining gaps:
  Backend dependencies, skipped checks and release gates.
```

## Initial agent backlog

The highest-value starter tasks for this frontend are:

1. Remove the hard-coded `John Doe` dashboard and establish synthetic request mocks.
2. Add application providers, routing, error boundary and token-based design system.
3. Implement the shared typed API client and normalized errors.
4. Implement login/logout/session expiration without persistent PHI caches.
5. Build the minimal patient portal overview using `/api/portal/...` endpoints.
6. Add appointment-request, record-request/download and payment workflows.
7. Add the staff shell, patient context banner and work queue.
8. Add patient registry and appointment-list workflows.
9. Add read-oriented clinical chart tabs before enabling high-risk mutations.
10. Establish CI for tests, production build, accessibility and dependency scanning.

