# Reliable messaging operations

## Scope

The shared `reliability-core` module provides the database-backed outbox and inbox used by the data-owning services. It replaces direct fire-and-forget audit publishing and gives domain workflows a transactionally consistent event path.

Every participating service owns its own `reliable_event_outbox` and `reliable_event_inbox` tables. There is no cross-service database.

## Delivery model

1. The domain mutation and outbox insert execute on the same service datasource and transaction.
2. A scheduled publisher atomically claims eligible records.
3. Kafka acknowledgement changes the record to `PUBLISHED`.
4. Failures use exponential backoff. The configured maximum moves the record to `DEAD_LETTER`.
5. A consumer claims `(consumer_name,event_id)` in its local inbox.
6. The handler and `PROCESSED` transition execute in one local transaction.
7. Duplicate deliveries observe the existing inbox row and do not run the handler again.
8. A failed handler is recorded as `FAILED` and may be retried by Kafka delivery.

This gives at-least-once transport and effectively-once local handling when all consumer-side writes use the same datasource transaction.

## Operator API

Every participating service exposes ADMIN-only endpoints:

- `GET /internal/reliability/outbox?status=DEAD_LETTER&offset=0&limit=50`
- `POST /internal/reliability/outbox/{eventId}/replay`
- `POST /internal/reliability/outbox/reconcile`
- `GET /internal/reliability/inbox?status=FAILED&offset=0&limit=50`
- `POST /internal/reliability/inbox/reconcile`

The reconciliation calls recover publisher or consumer claims whose leases expired after a process crash. They do not fabricate missing domain events.

Operational alerts should cover:

- oldest pending outbox age;
- dead-letter count and rate;
- failed inbox count and rate;
- expired-claim recoveries;
- publish latency and attempt count;
- Kafka consumer lag.

## Poison messages and replay

Malformed envelopes that cannot expose a valid UUID `eventId` cannot be claimed by the inbox. Kafka listener error handling must route them to a topic-specific dead-letter topic while preserving topic, partition, offset, headers, exception class, and payload hash. Raw PHI-bearing payloads must not be copied into logs or alert labels.

For a valid envelope whose handler fails:

1. inspect the inbox failure and correlated service logs;
2. remediate code, configuration, or reference data;
3. replay the original Kafka record from the dead-letter topic;
4. verify the inbox reaches `PROCESSED`;
5. reconcile the expected domain projection.

For an outbox dead letter:

1. inspect `lastError` and attempts;
2. verify topic availability and schema compatibility;
3. use the outbox replay endpoint;
4. verify Kafka acknowledgement and downstream inbox completion.

Replay endpoints are ADMIN-only and must be audited.

## Retention

Defaults are configuration, not legal-retention decisions:

- published outbox rows: 30 days (`app.reliability.retention-days`);
- pending, failed, and dead-letter outbox rows: retained until resolved;
- inbox metadata: retained for at least the maximum Kafka replay horizon and backup recovery window;
- Kafka domain topics: recommended minimum 30 days;
- Kafka dead-letter topics: recommended minimum 90 days;
- immutable audit evidence: governed by the approved compliance retention schedule, not by outbox cleanup.

Before reducing retention, verify that the new period exceeds maximum outage, restore, backfill, and partner-replay windows.

## Schema compatibility

- The topic suffix is the major schema version.
- Breaking field or semantic changes require a new major topic and a dual-publish/dual-consume migration.
- Additive fields inside one major version must be optional.
- Consumers ignore unknown fields and reject unsupported major versions.
- Every event has a UUID `eventId`, integer `schemaVersion`, `eventType`, `occurredAt`, `source`, aggregate identifiers, and a PHI-minimized payload.
- Contract tests must run old consumer fixtures against new producer envelopes before rollout.

## Backfill procedure

1. Define the exact aggregate range, event type, source revision, and expected count.
2. Take a database backup and record Kafka offsets.
3. Generate new event IDs and mark envelopes with `backfillId` and the original effective timestamp.
4. Insert backfill events into the source service outbox in bounded batches.
5. Pause between batches and compare published, processed, failed, and projection counts.
6. Stop on any unexplained mismatch.
7. Record the final counts and reconciliation evidence.

Never copy production clinical payloads into a lower environment for backfill testing.

## Recovery procedure

After restoring a service database:

1. keep consumers stopped;
2. verify Flyway and outbox/inbox schema versions;
3. reconcile expired `PROCESSING` claims;
4. compare the restore timestamp with Kafka retention and consumer offsets;
5. replay only the missing offset range;
6. compare source aggregate counts with every restored projection;
7. resume consumers gradually and monitor lag, failures, and duplicate suppression.

If Kafka history no longer covers the recovery point, use an approved source-owned backfill instead of editing downstream databases.

## Known boundaries

The repository does not configure a production Kafka dead-letter publishing recoverer, schema registry, or vendor monitoring backend yet. The database mechanisms and operating contracts are in place; deployment must supply broker retention, dead-letter topics, alert routing, credentials, and capacity targets.

