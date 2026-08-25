# Patient Management Services

A microservices-based application for managing patient data, billing, and analytics, featuring secure authentication and inter-service communication.

## Microservices Overview

The project consists of the following microservices:

### 1. API Gateway
- **Role**: Entry point for all external requests.
- **Responsibilities**:
    - Routing requests to appropriate microservices.
    - Centralized JWT validation using a custom gateway filter.
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
    - CRUD operations for patient records.
    - Synchronous communication with the **Billing Service** via **gRPC** for billing status checks.
    - Asynchronous event publishing to **Kafka** for patient-related activities.

### 4. Billing Service
- **Role**: Financial management related to patients.
- **Responsibilities**:
    - Processing and managing patient billing information.
    - Providing high-performance **gRPC** endpoints for real-time inter-service queries.

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
    - Creating virtual consultation rooms and capturing digital consent forms.

### 7. Electronic Health Records (EHR) Service
- **Role**: Clinical record management.
- **Responsibilities**:
    - Storing medical histories, diagnoses, prescriptions, lab results, and vaccination records.
    - Importing external laboratory results from LIS-style payloads.

### 8. Insurance & Claims Service
- **Role**: Insurance administration.
- **Responsibilities**:
    - Managing patient insurance policies.
    - Verifying coverage and processing insurance claims for billing workflows.

### 9. Notification Service
- **Role**: Centralized patient and staff communication hub.
- **Responsibilities**:
    - Queuing email, SMS, and push notifications.
    - Supporting appointment reminders, billing alerts, and MFA code delivery.

### 10. Inventory & Pharmacy Service
- **Role**: Supply and medication inventory control.
- **Responsibilities**:
    - Tracking medical supplies and medication stock.
    - Receiving EHR prescriptions, dispensing medication, and creating medication charges.

### 11. Audit & Compliance Service
- **Role**: Compliance-grade access logging.
- **Responsibilities**:
    - Capturing who accessed or changed patient records, when, and why.
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

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
