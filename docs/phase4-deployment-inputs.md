# Phase 4 deployment inputs

The empty cells below are intentional placeholders requested by the owner on
2026-09-08. Completing the repository implementation does not activate these
integrations or approve production use. Fill in the values and attach acceptance
evidence before enabling the corresponding capability. Do not put secrets here;
record secret-manager references instead.

## Partner interfaces

| Required input | HL7 v2 | X12 | DICOM/DICOMweb |
| --- | --- | --- | --- |
| Accountable owner | | | |
| Partner / provider | | | |
| Version / implementation guide | | | |
| Message types / transactions / modalities | | | |
| Patient and facility identifier mappings | | | |
| Test endpoint | | | |
| Production endpoint | | | |
| Transport / TLS identity / secret reference | | | |
| ACK, retry, duplicate and quarantine policy | | | |
| Partner acceptance evidence | | | |
| Activation approval and date | | | |

## Patient and facility authorization

| Required input | Owner-supplied value |
| --- | --- |
| Policy owner and approved policy version | |
| Authoritative subject-to-patient relationship source | |
| Facility membership and patient assignment source | |
| Permitted purposes of use by role | |
| Patient / proxy identity and consent signature verification | |
| Cross-facility access and emergency exceptions | |
| Legacy API patient-context enforcement acceptance | |
| Revocation propagation and emergency review SLA | |

## Retention, archival and documents

| Required input | Owner-supplied value |
| --- | --- |
| Jurisdiction and approved records policy | |
| Clinical-record retention period and start event | |
| Audit-evidence retention period and start event | |
| Backup retention period | |
| Integration-document retention period and start event | |
| Archive destination and encryption-key reference | |
| Hold authority and storage-wide hold enforcement | |
| Independent destruction approver | |
| Document store, size/type limits and malware scanner | |
| Signature verification provider | |
| Archive retrieval / authorization acceptance evidence | |

Copy approved durations into `ops/recovery/retention-policy.example.json` as a
deployment-specific policy. Its null durations and disabled automatic deletion
are deliberate. Filling in this table does not implement storage integrations.

## Recovery and security acceptance

| Required input | Owner-supplied value |
| --- | --- |
| Recovery owner | |
| RPO by service | |
| RTO by service | |
| Encrypted backup schedule and offsite destination | |
| Key recovery, object storage and message checkpoint manifest | |
| Production-scale restore report and acceptance date | |
| TLS/mTLS identities and rotation policy | |
| Separate migration and runtime database role references | |
| External immutable audit-anchor destination | |
| Penetration-test provider, scope and approved window | |
| Security finding remediation / independent retest evidence | |
| Production release approver and approval date | |

See [the implementation record](phase4-compliance.md) for current enforcement
boundaries, commands and acceptance criteria. Empty entries remain release gates.
