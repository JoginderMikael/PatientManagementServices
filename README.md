# Patient Management Services

A microservices-based application for managing patient data, billing, and analytics, featuring secure authentication and inter-service communication.

## Microservices Overview

The project consists of the following microservices:

### 1. API Gateway
- **Role**: Entry point for all external requests.
- **Responsibilities**:
    - Routing requests to appropriate microservices.
    - JWT validation through Spring Security's OAuth2 resource-server support.
    - Load balancing and edge security.

### 2. Auth Service
- **Role**: Identity and Access Management.
- **Responsibilities**:
    - User registration and authentication.
    - Issuing secure JSON Web Tokens (JWT) for authorized users.
    - Managing user credentials and roles.

### 3. Patient Service
- **Role**: Core business logic for patient management.
- **Responsibilities**:
    - MPI-oriented registration, duplicate detection, lifecycle state, and merge/unmerge workflows.
    - Durable asynchronous billing-account provisioning through a transactional outbox.
    - Minimal-PHI, versioned patient event publishing to **Kafka**.

### 4. Billing Service
- **Role**: Financial management related to patients.
- **Responsibilities**:
    - Persisting one idempotently created billing account per patient.
    - Providing secured REST and **gRPC** account endpoints and consuming patient registration events.

### 5. Analytics Service
- **Role**: Data processing and reporting.
- **Responsibilities**:
    - Consuming patient events from **Kafka**.
    - Aggregating data for health analytics and reporting.
    - Managing population health trends, chronic disease cohorts, and regulatory report generation.

### 6. Appointment & Scheduling Service
- **Role**: Scheduling and telemedicine coordination.
- **Responsibilities**:
    - Managing doctor schedules, patient bookings, cancellations, and waitlists.
    - Enforcing overlapping-booking conflicts with database-backed slot reservations.
    - Creating virtual consultation rooms and capturing digital consent forms.

### 7. Electronic Health Records (EHR) Service
- **Role**: Clinical record management.
- **Responsibilities**:
    - Persisting encounters, medical histories, diagnoses, prescriptions, lab results, signed notes, and vaccination records.
    - Importing external laboratory results from LIS-style payloads.

### 8. Insurance & Claims Service
- **Role**: Insurance administration.
- **Responsibilities**:
    - Managing patient insurance policies.
    - Verifying coverage and processing insurance claims for billing workflows.

### 9. Notification Service
- **Role**: Centralized patient and staff communication hub.
- **Responsibilities**:
    - Persisting and dispatching email, SMS, and push notifications through a configurable provider.
    - Retrying failed deliveries with backoff and dead-letter status.
    - Supporting appointment reminders, billing alerts, and MFA code delivery.

### 10. Inventory & Pharmacy Service
- **Role**: Supply and medication inventory control.
- **Responsibilities**:
    - Tracking medical supplies and medication stock.
    - Receiving EHR prescriptions, dispensing medication, and creating medication charges.

### 11. Audit & Compliance Service
- **Role**: Compliance-grade access logging.
- **Responsibilities**:
    - Automatically capturing who accessed or changed Phase 2 core records, when, and why.
    - Persisting idempotent, hash-chained audit records from versioned events.
    - Searching patient-specific audit trails for HIPAA-oriented review.

### 12. Patient Portal Service
- **Role**: Patient-facing workflow API.
- **Responsibilities**:
    - Allowing patients to request appointments, request records, and submit bill payments.
    - Providing a patient portal overview.

### 13. Staff Dashboard Service
- **Role**: Clinical staff workflow API.
- **Responsibilities**:
    - Managing daily rounds, patient chart summaries, and staff tasks.

### 14. Infrastructure
- **Role**: Deployment and environment configuration.
- **Responsibilities**:
    - Cloud infrastructure management (likely using AWS CDK).
    - Shared deployment scripts and configurations.

## Technology Stack
- **Framework**: Spring Boot (Java)
- **Communication**: REST API, gRPC
- **Messaging**: Apache Kafka
- **Security**: Spring Security, JWT
- **Infrastructure**: AWS CDK, Maven

## Local Development

Requirements: Java 21 or newer, Maven 3.9 or newer, Docker, and Docker Compose.

1. Copy `.env.example` to `.env`.
2. Replace every placeholder. Generate `JWT_SECRET` with `openssl rand -base64 32` or an equivalent cryptographically secure generator.
3. Validate the resolved configuration with `docker compose config --quiet`.
4. Build the application images with `docker compose --progress plain build`.
5. Start the environment and wait for health checks with `docker compose up -d --wait --wait-timeout 300`.

Compose waits for Kafka and application health checks before starting dependent
services. Check `docker compose ps -a` for an unhealthy or exited container and
`docker compose logs --tail 100 <service>` for its error. The first image build
downloads Maven dependencies and can take several minutes. To reduce concurrent
build load on smaller Docker installations, use `docker compose --parallel 2 build`.
Database volumes survive rebuilds; changing a database password in `.env` does
not update credentials already stored in an existing PostgreSQL volume.

The local stack exposes the patient-management frontend on port `8085`, the gateway on port `4004`, Prometheus on `9090`, and Grafana on `3000`. The frontend serves the React production build and proxies `/auth` and `/api` requests to the gateway over the internal Compose network. Override its host port with `FRONTEND_PORT` when needed. PostgreSQL is exposed for local tooling on ports `5000` (patient), `5001` (auth), `5002` (billing), `5003` (appointment), `5004` (EHR), `5005` (notification), and `5006` (audit). API documentation and Prometheus endpoints are public only because the Compose defaults explicitly enable them; non-local defaults require an authenticated `ADMIN` token.

Run the complete build with:

```shell
mvn clean verify
```

Phase 3 adds durable insurance, pharmacy, patient portal, staff queues, clinical
safety, and revenue-ledger workflows. See [Phase 3 workflow instructions](docs/phase3-workflows.md)
for endpoints, proxy identity setup, settlement steps, permissions, and operating
limits. The four additional PostgreSQL databases use local ports 5007–5010.
`mvn verify` now also starts an isolated six-service PostgreSQL E2E environment
and runs a bounded concurrent read workload; Docker must be running.

Live end-to-end tests remain opt-in and expect the Compose environment to be running:

```shell
mvn -pl integration-tests -DrunLiveIntegrationTests=true test
```

Production security, secret, TLS/mTLS, and observability decisions are documented in [`docs/production-foundation.md`](docs/production-foundation.md). Versioned event envelopes, topics, PHI constraints, and compatibility rules are documented in [`docs/event-schemas.md`](docs/event-schemas.md).

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
## Phase 4 compliance and interoperability

FHIR Patient read, scoped consent and emergency access, compliance case workflows,
and recovery/security verification are documented in
[docs/phase4-compliance.md](docs/phase4-compliance.md), including production release
gates and unsupported partner interfaces.

Owner-supplied partner, authorization, retention and recovery decisions have blank
fields in [Phase 4 deployment inputs](docs/phase4-deployment-inputs.md).
