# Phase 4: implemented controls and release gates

Updated 2026-09-08. This is an engineering implementation record, not a claim of
regulatory certification or approval for production patient data.

Owner-supplied partner details, access policy, retention periods and recovery
targets are intentionally blank in [deployment inputs](phase4-deployment-inputs.md).
The owner will fill these in later. They remain deployment gates rather than
invented defaults or enabled placeholder integrations.

**Status:** repository baseline complete within the scope below. Partner adapters,
platform-wide authorization and production storage integrations still require the
owner's inputs and subsequent implementation/acceptance.

## FHIR API

Gateway base: `/api/fhir`; direct patient-service base: `/fhir`.
`GET /metadata` advertises FHIR R4 4.0.1 JSON and only `Patient` read.
`GET /Patient/{uuid}` returns an active patient's demographics, MRN, resource
version, ETag, Last-Modified, and `Cache-Control: no-store`. Archived/merged
patients return 410; missing patients return 404 after authorization. Application
errors use OperationOutcome. Security/framework failures may have generic bodies.
There is no search, write, history, bulk export, US Core, SMART launch, or clinical
resource conformance claim. Unknown administrative gender labels are omitted.

ADMIN and CLINICIAN roles can use this API, but neither bypasses its privacy check.
The patient service forwards the verified bearer token to the compliance service;
the subject cannot be overridden in request data. Privacy denial is 403;
unavailability or malformed decisions produce 503. Decisions are never cached.
Set `app.privacy.base-url` (Spring environment `APP_PRIVACY_BASE_URL`) when the
compliance service has a different address. Use authenticated TLS between services
in production; the default address is the Compose service name on port 4015.

`Phase4FhirTest` exercises real HTTP decision transport, bearer forwarding, mapping,
role denial, invalid identifiers, denied decisions and malformed responses. It
emits synthetic fixtures. `python ops/interoperability/validate_fhir.py` verifies a
SHA-256-pinned official HL7 validator, loads R4 definitions, validates those fixtures,
and fails on error/fatal outcomes. Terminology-server calls are disabled. Package
definitions are downloaded on first use. Validation of these three fixtures does
not establish conformance to every possible input or an implementation guide.

References: [FHIR R4 HTTP](https://hl7.org/fhir/R4/http.html),
[CapabilityStatement](https://hl7.org/fhir/R4/capabilitystatement.html),
[validator release](https://github.com/hapifhir/org.hl7.fhir.core/releases/tag/6.10.4).

## Privacy and emergency access

Gateway base: `/api/compliance/privacy`; direct base: `/compliance/privacy`.
The compliance database owns durable grants. The packaged compliance service
requires PostgreSQL (H2 is test-only).
Configure its datasource credentials through the existing environment settings.
A grant covers exactly one subject,
one patient, and `FHIR_PATIENT_READ`. Consent is not inferred from a staff role.
Officer-recorded evidence must be independently verified outside this API; the
service records references, not signatures or patient identity verification.

| Endpoint | Role | Body/behavior |
| --- | --- | --- |
| POST `/consents` | PRIVACY_OFFICER | `patientId`, `subject`, `expiresAt`, `evidenceReference`; returns `id`; max duration 365 days |
| POST `/break-glass` | CLINICIAN | `patientId`, `expiresAt`, `evidenceReference`; subject is the caller; max duration 1 hour |
| POST `/grants/{id}/revoke` | PRIVACY_OFFICER | Revokes this grant; other active grants remain valid |
| GET `/break-glass/reviews` | PRIVACY_OFFICER | Oldest 100 unreviewed emergency grants, including expired grants |
| POST `/break-glass/{id}/review` | PRIVACY_OFFICER | `evidenceReference`; independent reviewer required; revokes grant |
| POST `/decisions/{patientId}` | ADMIN, CLINICIAN | Optional `emergencyId` query parameter; returns `allowed` |

FHIR clients must explicitly send `X-Break-Glass-Id` to use an emergency grant.
Every grant, revocation, emergency review and permit/deny decision appends audit
evidence in the same transaction. Emergency access remains in the review queue
after expiry; review cannot be self-approved or overwritten. The officer role must
be provisioned through trusted IAM administration; no new public role assignment
endpoint is introduced. A decision is point-in-time: an already authorized request
may finish while a revocation is being committed.

**Coverage boundary:** these grants enforce the new FHIR Patient read API only.
Existing patient/EHR/billing and other legacy APIs retain their existing policies;
they must not be exposed as alternative external FHIR access paths. Platform-wide
patient/facility/purpose ABAC and patient-authored consent/proxy signatures remain
required follow-up work. This is a material production release gate.

## Incident, review, retention and recovery cases

Gateway base: `/api/compliance/cases`; direct base: `/compliance/cases`.
All endpoints require PRIVACY_OFFICER. Evidence strings are opaque references to
an access-controlled case repository, not clinical details, credentials, public
URLs or notification content. No external email or regulator notification is sent.

POST the collection with `kind`, `owner`, `evidenceReference`, and future `dueAt`.
Kinds: INCIDENT, ACCESS_REVIEW, RESTORE_DRILL, RETENTION_REVIEW, DOCUMENT_REVIEW.
GET lists the latest 100 cases; `?overdue=true` lists the oldest 100 overdue open
cases. GET `/{id}/history` returns actor/time/evidence history.
POST `/{id}/transitions` with `expectedStatus`, `status`, `evidenceReference`.
Stale or invalid transitions return 409. Updates serialize with row locks.

Incident sequence:

```
OPEN -> INVESTIGATING -> CONTAINED -> ASSESSED
  -> NOTIFICATION_REQUIRED -> NOTIFIED -> REMEDIATING -> CLOSED
  OR NO_NOTIFICATION_REQUIRED -> REMEDIATING -> CLOSED
```

Other cases use OPEN -> INVESTIGATING -> REMEDIATING -> CLOSED. Evidence is
required at every step. POST `/{id}/hold` with `enabled`, `evidenceReference` to
record a hold/release. A held case cannot close. These are case-level holds; they
do not automatically freeze data in other services, storage or backup systems.

Incident operating procedure:

1. Open a case and assign an accountable owner and jurisdiction-appropriate due
   time. Preserve evidence in controlled storage; record acquisition hashes and
   chain of custody there. Start a separate legal hold on affected storage.
2. Record containment evidence, investigate scope and affected individuals, and
   have the privacy/legal owner assess reportability. The API does not calculate
   statutory deadlines or make legal determinations.
3. Record the notification decision and rationale reference. For required notices,
   retain independently verified delivery receipts before recording NOTIFIED.
4. Record remediation, verify fixes, release holds only with authority, and close
   with acceptance evidence. Review overdue cases routinely; no alert delivery
   provider or automatic escalation is claimed here.

Access reviews use the same case history to retain reviewer decisions, IAM exports,
revocation tickets and verification evidence. Closing a case does not itself revoke
IAM roles. Emergency-grant review does revoke the specific emergency grant.

## Audit hardening

New appends lock `audit_chain_head` before checking source-event idempotency and
committing the next hash. Late occurred-at timestamps no longer choose a different
predecessor. The new canonical hash uses length-prefixed fields and includes role,
reason, endpoint and correlation metadata as well as the previous payload fields.
New rows record hash_version=2; legacy rows retain version 1. Version 2 normalizes
timestamps to database microsecond precision and hashes the stored null/empty
representation. Historical hashes are retained; V3 anchors the new head to the latest legacy event
(occurred_at/id ordering). Existing legacy forks are not repaired or re-certified.
The migration boundary must be retained when independently verifying legacy hashes.
The hash chain is not a signature and cannot defeat a privileged database owner.
Deploy V3 with old audit writers stopped and the input queue paused/drained under
the deployment runbook. Do not run old and new append implementations concurrently:
old instances do not participate in the new head lock. Preserve a backup and legacy
head evidence before migration, then resume consumers after the new writer passes
an append/read check. Rollback requires the matching database recovery procedure.

`ops/security/audit-immutability.sql` installs PostgreSQL mutation-rejection triggers
and runtime-role privileges for audit events and case history. Apply once as the
migration owner after provisioning a separate non-owner runtime role; select the
audit database/schema before running it. It is deliberately not an automatic
Flyway migration because deployments must identify the actual runtime role.
The synthetic recovery drill verifies the script and restores its triggers.
Keep migration credentials separate, externally anchor/export evidence to immutable
storage, and test privileged tampering in the deployment security assessment.

## Recovery, retention and archival

Run `python ops/recovery/restore_drill.py` with Docker available. It creates a
uniquely named PostgreSQL 16 container with no network and no published port or
existing volume. It applies production SQL migrations from all eleven database
services into synthetic schemas, adds nonempty Unicode/binary/null/FK fixtures,
backs up with custom-format pg_dump and restores into a fresh database using
`pg_restore --exit-on-error --single-transaction`. It compares all table data,
column/index/constraint/trigger definitions and rejects a truncated archive.
It writes `integration-tests/target/phase4-restore.json` and removes only its own
container and anonymous volumes. No existing deployment database is contacted.

This drill validates schema backup/restore mechanics. Most clinical tables are
empty. It does not validate production volume, encryption keys, object storage,
Kafka offsets, cross-service consistency, failover, RPO or RTO. Production drill:

1. Approve RPO/RTO and backup schedules per service. Encrypt backups, isolate
   backup credentials, preserve required keys and test access to offsite copies.
2. Restore into isolated infrastructure with outbound integrations disabled.
   Include database roles, object versions, approved evidence archives and
   messaging replay/checkpoint procedures in the recovery manifest.
3. Validate manifests/checksums, record counts, referential integrity, patient
   merges, balances, current consents/revocations, held records and audit anchors.
   Exercise login and authorized/denied workflow reads using synthetic probes.
4. Measure backup age, restore duration and application recovery duration against
   approved targets. Store the signed report in a RESTORE_DRILL case. Restore
   traffic only through the deployment change process after acceptance.

`ops/recovery/retention-policy.example.json` leaves durations unset and automatic
deletion disabled. The records/legal owner must supply rules by jurisdiction,
record type and lifecycle. RETENTION_REVIEW cases can retain approval and archive
verification evidence. Archive manifests must include record identifiers, hashes,
policy versions, hold state and controlled destination references. Verify retrieval
and authorization before removing an online copy. No automatic clinical deletion,
archive mover, encrypted backup scheduler or organization-wide hold enforcement is
implemented. These require deployment policy and storage integration.

## Interoperability partner decisions

No partner endpoint, licensed payer implementation guide, PACS, or HL7 interface
agreement was supplied. HL7 v2, X12 and DICOM transports are not enabled. Existing
portal workflows deliver staff-reviewed record content; DOCUMENT_REVIEW cases
support evidence tracking but are not a binary document store or malware scanner.

| Interface | Required before implementation/activation | Acceptance evidence |
| --- | --- | --- |
| HL7 v2 | Sender/receiver, version, message/event types, identifiers, transport and ACK policy | Synthetic ADT/order/result fixtures, negative ACK, replay, duplicate, quarantine and recovery tests |
| X12 | Payer/clearinghouse, licensed guide, transactions, credentials, claim ownership mappings | Partner-certified eligibility/claim/remittance exchanges, control totals, rejects and idempotent reconciliation |
| DICOM | PACS/DICOMweb provider, study ownership, allowed modalities, identifiers and credentials | Query/retrieve authorization, wrong-patient denial, large-object limits and provider acceptance |
| Documents | Storage, retention/hold policy, allowed formats, scanner and signature provider | Quarantine until verified clean, size/type/hash validation, patient-bound access, revocation and retrieval tests |

Never interpret transport receipt or a manually recorded result as partner
acceptance. Do not add a parser that silently treats unsupported messages as valid.

## Security assessment and penetration-test readiness

After packaging, run `python ops/security/scan.py`. The pinned CycloneDX Maven
plugin generates `sbom.json` from the resolved reactor dependency graph, including
test dependencies. A digest-pinned Trivy image scans it into
`dependency-findings.json`, then scans source configuration and secrets into
`findings.json`. Either high/critical finding set fails the command.
Reports stay in `integration-tests/target/security`. Maven uses its existing cache
(override with MAVEN_REPOSITORY); initial plugin/dependency and vulnerability
database acquisition requires network. Vulnerability coverage is the Maven reactor;
frontend dependency and operating-system image scans are separate release checks.
Source scanning uses a temporary copy with generated directories pruned before
Docker traversal; the copy is removed after the scan. Previous reports are cleared
before each run, and a missing/empty SBOM fails verification.
This is source/dependency analysis, not an operating-system container-image scan.
Local `.env`, IDE metadata, logs, generated Maven/CDK output and node_modules are excluded; CI separately
rejects tracked `.env` files. The security workflow retains evidence for 14 days.
No vulnerability exceptions are silently accepted. See
[Trivy filesystem scanning](https://trivy.dev/docs/latest/target/filesystem/).

Before a penetration test, provision a synthetic deployment, record approved
targets/time window and rules of engagement, and provide role-separated accounts.
Assess token validation, role escalation, patient/facility isolation, consent and
proxy revocation, emergency misuse, replay/concurrency, injection, SSRF, upload
handling, unauthenticated service ports, logs and PHI exposure, rate limits,
dependency/container findings, and backup/evidence tampering. Record reproducible
findings with owners, severity, remediation deadlines and independent retests.
No penetration test or clean security certification is claimed by unit tests or
by adding a scan workflow. Plaintext internal transports, best-effort legacy HTTP
audit delivery and platform-wide ABAC remain explicit assessment/release gates.

## Completion hardening (2026-09-08)

- Privacy responses must contain a JSON boolean `allowed`. Missing, null or
  incorrectly typed decisions return 503; explicit false returns 403. Both paths
  deny access without returning patient data. Tests also verify that explicit
  emergency-grant IDs and bearer tokens reach the decision service unchanged.
- All thirteen Java application Dockerfiles declare runtime UID/GID 10001:10001.
  The application JAR stays root-owned and readable; Java temporary files use
  `/tmp`. Deployments mounting writable application directories must provision
  permissions for that UID rather than reverting to root.
- The Maven parent uses Spring Boot 3.5.16, Spring Cloud 2025.0.3, Springdoc
  2.8.17 and gRPC 1.75.0, with Netty 4.1.136.Final, Tomcat 10.1.59 and PostgreSQL
  JDBC 42.7.12 overrides.
  Spring Kafka follows the Boot dependency BOM rather than the old 3.3.0 pin.
  This addresses the previous scan's outdated dependency baseline; scan results,
  not version numbers alone, determine the security gate.
- Gateway regression tests load the actual route configuration and check that
  anonymous FHIR/compliance requests are denied before forwarding.

Compatibility references: [Boot 3.5 requirements](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
and [Cloud 2025.0.3 release](https://spring.io/blog/2026/06/11/spring-cloud-2025-0-3-aka-northfields-has-been-released/).
Cloud 2025.0 has ended open-source support; production requires a supported
  maintenance arrangement or a separately tested migration to a supported release train.

Verification commands (from the repository root):

```shell
mvn -B verify
python ops/recovery/restore_drill.py
python ops/interoperability/validate_fhir.py
python ops/security/scan.py
```

Use Java 21 or newer, Maven 3.9+, Python and Docker. The first run needs network
access for dependencies and validator/scanner definitions. Security findings must
remain visible; blank deployment inputs do not waive the security gate.

Verified on 2026-09-08: the full 16-module Maven reactor passed `verify`, including
11 PostgreSQL migration tests and both isolated HTTP workflow tests. The separate
gateway regression run passed 2 tests; FHIR contract coverage passed 6 tests and
compliance coverage passed 6 tests. The synthetic eleven-service recovery drill
and official FHIR validation of all 3 fixtures passed. Nine opt-in tests against
an existing deployment remained skipped by design; no live deployment was tested.

The final security command exited 0: the 306-component resolved Maven SBOM had
zero HIGH/CRITICAL vulnerabilities, and source scanning had zero HIGH/CRITICAL
secret or configuration findings across 13 recognized configuration files.
Three security-gate regression tests passed. The vulnerability database was updated
2026-09-08 07:08 UTC. These results do not cover lower-severity findings, frontend
dependency vulnerabilities, operating-system images or a penetration test.
