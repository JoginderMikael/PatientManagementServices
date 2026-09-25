# Event schemas

The platform's durable integration events use UTF-8 JSON envelopes and versioned Kafka topics. Producers must write the domain change and its outbox row in the same database transaction. Consumers must use `eventId` as an idempotency key because delivery is at least once.

## Version 1 envelope

```json
{
  "eventId": "5c93c0ad-7792-4f09-8b0c-f5ddc741f00e",
  "schemaVersion": 1,
  "eventType": "PATIENT_REGISTERED",
  "occurredAt": "2026-09-04T12:00:00Z",
  "source": "patient-service",
  "aggregateType": "Patient",
  "aggregateId": "018f4bb4-53aa-7ee2-9c73-c04d65d7c987",
  "patientId": "018f4bb4-53aa-7ee2-9c73-c04d65d7c987",
  "actorId": "user-subject-or-system",
  "actorRole": "ROLE_RECEPTIONIST",
  "correlationId": "request-or-trace-id",
  "payload": {}
}
```

The envelope contains operational identifiers only. Sensitive clinical data, names, email addresses, phone numbers, and tokens must not be published. Event-specific data belongs in `payload` and must follow the same rule.

## Topics

| Topic | Producer | Version 1 events | Consumers |
|---|---|---|---|
| `patient.events.v1` | patient-service outbox publisher | `PATIENT_REGISTERED`, `PATIENT_UPDATED`, `PATIENT_STATUS_CHANGED`, `PATIENT_MERGED`, `PATIENT_UNMERGED`, `PATIENT_ARCHIVED` | billing, audit, analytics |
| `notification.requests.v1` | workflow services or integrations | `NOTIFICATION_REQUESTED` | notification |
| `audit.events.v1` | automatic HTTP audit filters | `HTTP_<METHOD>` | audit |
| `billing.events.v1` | reserved for billing | Future billing lifecycle events | audit accepts v1 |
| `appointment.events.v1` | appointment-service reliable outbox | Booking, rescheduling, cancellation, arrival, completion, no-show and waitlist promotion | audit accepts v1 |
| `ehr.events.v1` | EHR reliable outbox | Structured clinical-resource creation/amendment metadata without clinical payloads | audit accepts v1 |
| `notification.events.v1` | notification reliable outbox | Notification queue and delivery lifecycle metadata | audit accepts v1 |

## Compatibility policy

- A topic suffix is the major schema version. Breaking field or semantic changes require a new topic such as `.v2` and a migration period with dual publishing or consumption.
- Within a major version, producers may add optional fields. Consumers must ignore unknown fields and tolerate absent optional fields.
- Required envelope fields and existing field meanings cannot change within a major version.
- Consumers reject or ignore unsupported major versions and must not guess how to interpret them.
- Failed outbox publications remain pending and are retried. A row is marked published only after Kafka acknowledges the send.
- Consumers must be idempotent. The billing consumer additionally relies on database uniqueness for patient and command keys; the audit consumer persists `sourceEventId` uniquely.

Dead-letter, replay, retention, backfill, compatibility and recovery procedures are defined in
[Reliable messaging operations](reliable-messaging.md).
