# Phase 3 workflows

Phase 3 extends the repository's **workflow APIs**. Insurance, pharmacy, portal,
and staff services now use Flyway-managed PostgreSQL storage. H2 is test-only in
those services. New local database ports are 5007, 5008, 5009, and 5010 respectively.
Each database has its own volume and credentials.

All paths below require bearer authentication. Through the gateway, prefix them
with `/api`, including the new `/api/billing/**` route. Downstream HTTP calls
forward the bearer token and use three-second connect/five-second read timeouts.

## Revenue cycle

1. `POST /billing/invoices`: `patientId`, unique `reference`, positive two-decimal
   `amount`, and account `currency`. Invoice creation and account balance changes
   commit together. Accounts currently use USD, matching the existing model;
   incompatible invoice currencies are rejected, never combined.
2. `POST /insurance/policies` registers a policy. Record an actual payer response
   at `/insurance/policies/{id}/eligibility-evidence` with `serviceCode`,
   `validUntil`, `payerReference`, and `insurancePercent` (0–100).
   `/insurance/coverage-verifications` returns `PENDING` without matching current
   evidence; it no longer fabricates verified 80% coverage. One evidence record
   per policy is supported; replacement supersedes its previous service code.
3. `/insurance/claims` checks policy ownership, invoice ownership, and amount.
   There is one claim per invoice; changed retries conflict. `/{id}/adjudicate`
   accepts `APPROVED` or `DENIED` and `approvedAmount`, preserving the original
   submitted amount. Denial requires zero approval.
4. `/insurance/claims/{id}/remittances` records a unique payer `reference` and
   `paidAmount` matching the approved amount. The claim becomes `PAID`.
   `/{id}/reconcile` posts the remittance to billing; retries return the same
   posting. Claim payment and invoice posting are separate states. Retry
   reconciliation if billing is unavailable.
5. `/billing/invoices/{id}/postings`: unique `reference`, positive `amount`, and
   `kind` (`PAYMENT` or `REMITTANCE`). Invoice row locks prevent concurrent
   overpayment. Invoice/account balances and immutable postings commit together.

Administrators and billing staff own these operations. Pharmacists may create
medication invoices but cannot access payment postings or account APIs. These
workflows record staff-verified external results. They do not submit X12, call a
clearinghouse, charge a card, or store card credentials. Provider selection,
configuration, and acceptance remain deployment requirements.

## Pharmacy and inventory

* Create medications with zero stock at `/inventory-pharmacy/medications`.
  Receive batches at `/inventory-pharmacy/batches`: `medicationId`, `lot`, future
  `expiresOn`, positive `quantity`, unique receipt `reference`. Receipt updates
  batches, total stock, and movements atomically.
* `/inventory-pharmacy/prescriptions` receives an order with a unique
  `ehrPrescriptionId`. Changed retries conflict.
* `/inventory-pharmacy/prescriptions/{id}/review` records `medicationId`,
  `allergiesChecked`, `interactionsChecked`, `doseChecked`, and `reason`, with the
  authenticated reviewer. All checks must pass for approval.
* `/{id}/dispense` requires approval and rechecks the current EHR patient's
  prescription, medication, active status, allergies, and configured interactions.
  An unavailable EHR blocks dispensing. Unexpired batches are consumed in expiry
  order. Stock, movements, completion, and calculated charges commit together.
  Retries do not dispense again; competing dispensations cannot oversell stock.
* `/{id}/bill` with `currency` creates an idempotent invoice from the calculated
  charge. Retry billing after a failure without repeating dispensing.
* Medication movement history is at `/inventory-pharmacy/medications/{id}/movements`.
  Supply updates also record quantity deltas, exposed at
  `/inventory-pharmacy/supplies/{id}/movements`.

Only administrators and pharmacists have pharmacy access. The EHR recheck is
point-in-time, not a distributed lock against later EHR changes. Pharmacist review
remains necessary. Controlled-substance regulation, purchasing, returns, and
clinical terminology normalization remain beyond this workflow slice.

## Portal self-service and proxy access

After verifying identity, an administrator binds a stable JWT subject using
`POST /portal/identities` (`subject`, `patientId`). Knowing a patient UUID never
grants access. Ownership is not derived from email or caller-supplied claims.

* Owners grant `RECORDS`, `APPOINTMENTS`, or `PAYMENTS` through
  `/portal/patients/{id}/proxies`: `proxySubject`, `scope`, future `expiresAt`.
  Each scope has a separate grant. Revoke with
  `DELETE /portal/patients/{patient}/proxies/{grant}`. Renewal retains history.
  Proxies cannot delegate access.
* `/portal/patients/{id}/overview` is owner/admin-only because it combines scopes.
  Proxies read `/portal/patients/{id}/requests?type=RECORDS` (or `APPOINTMENTS`,
  `PAYMENTS`) for their permitted scope.
* `/portal/appointment-requests` creates a staff-triaged request. Patients cancel
  pending requests at `/{id}/cancel`. Administrators resolve them at
  `/{id}/resolve` with `SCHEDULED` or `DECLINED` and `appointmentReference`. Staff
  book the actual slot through appointment-service; the portal retains its
  reference.
* `/portal/record-requests` creates a review request. An administrator releases
  reviewed text at `/{id}/release` with `content`. `/{id}/download` rechecks current
  ownership/proxy authorization and returns an attachment with `Cache-Control:
  no-store`. This is reviewed record release, not FHIR export.
* `/portal/payments` accepts positive `amount`, `patientId`, `invoiceId`, and a
  required `Idempotency-Key`. It records a submitted request. After verifying an
  external receipt, an administrator calls `/{id}/settle` with `providerReference`.
  The service checks invoice ownership and posts to billing before marking it
  settled. Failed downstream requests remain retryable.

Portal bindings, grants, requests, releases, downloads, and settlements retain
actor history. New services also emit the existing PHI-minimized HTTP audit
envelope. Its inherited Kafka publisher is best-effort; compliance-grade audit
delivery remains later hardening work.

## Staff queues and clinical safety

Queue roles are `RECEPTIONIST`, `NURSE`, `CLINICIAN`, `BILLING_STAFF`, `PHARMACIST`,
`LAB_STAFF`, and `ADMIN`. `/staff-dashboard/queues/{role}` requires that role or
administrator access and returns at most 200 tasks ordered by due time. Daily
dashboards require the matching subject or administrator. Charts and rounds
remain restricted to clinical roles.

Tasks accept `ROUTINE`, `HIGH`, and `URGENT`, with operational due defaults of 24
hours, one hour, and 15 minutes. These are workflow defaults, not treatment rules.
Claiming uses the authenticated UUID subject. Completion, comments, and handoff
require the assignee and queue role, or administrator. `/{id}/handoff` records
`assigneeId`, `queueRole`, `dueAt`, and `reason`; `/{id}/history` retains transitions.

The scheduler scans every 60 seconds, escalates each overdue cycle once, and
routes clinical work to clinicians and nonclinical work to administration.
Completed work does not escalate. Handoff sets a new due cycle. Configure the
scan with `app.tasks.escalation-delay-ms`.

EHR prescribing blocks normalized exact matches to recorded allergies, duplicate
active medications, and configured interaction pairs from `/ehr/safety/rules`.
Only administrators maintain rules; no unvalidated clinical catalog is bundled.
`/ehr/safety/prescriptions/{id}/discontinue` ends an active prescription. Closed
encounters reject new linked entries.

`/ehr/safety/alerts` records `patientId`, `assigneeId`, unique `sourceReference`,
and `summary`. `/{id}/escalate` creates an idempotent urgent staff task. The assigned
clinician or administrator acknowledges with `/{id}/acknowledge`. EHR
acknowledgement and staff completion are explicit, separate actions.

## Verification and limits

Run `mvn -B verify` from the root with Java 21+, Maven 3.9+, and Docker. It runs
workflow, authorization, concurrency, and PostgreSQL migration tests, then starts
six packaged services on random ports with isolated PostgreSQL databases.
`Phase3WorkflowIT` exercises the revenue/pharmacy/portal/clinical flows over signed
HTTP requests and cleans up its processes and container afterward.

The E2E suite seeds an active queue and issues 100 invoice/queue reads with eight
concurrent clients. Unexpected responses or p95 >= 2,000 ms fail the run. Configure
`phase3.load.requests` and `phase3.load.p95Millis` for larger workloads. Results
are in `integration-tests/target/phase3-performance.json`, alongside service logs
and Failsafe reports; CI uploads these artifacts.

This local regression workload is not a capacity certification. The four new
domains serialize mutations with a database workflow lock; payment postings use
invoice locks. Patient reads are indexed and queues bounded, but several legacy
administrative lists remain unpaginated. Production capacity, representative
large datasets, multi-instance stress, external-provider acceptance, and the
Phase 4/5 release gates still require validation.
