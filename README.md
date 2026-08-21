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

### 6. Infrastructure
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
