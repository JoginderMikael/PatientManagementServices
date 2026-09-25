# Security Vulnerability Report - Patient Management Services
**Date:** 2026-09-13
**Scope:** Project-wide static analysis and manual review.

## Executive Summary
The security audit of the Patient Management Services project revealed several vulnerabilities ranging from critical to low severity. The most significant issues involve insecure credential management and weakened security configurations in the microservices.

## Vulnerabilities

### 1. Insecure Credential Management (CRITICAL)
- **Description:** The `.env` file contains hardcoded secrets and default passwords for all microservices and databases.
    - `JWT_SECRET` is hardcoded.
    - Multiple database passwords are set to `password`.
    - `GRAFANA_ADMIN_PASSWORD` is set to `password`.
- **Impact:** An attacker with access to the source code or the deployment environment can compromise all databases and forge authentication tokens.
- **Remediation:** 
    - Use a secure secret management system (e.g., AWS Secrets Manager, HashiCorp Vault).
    - Generate unique, cryptographically strong passwords for each service.
    - Ensure `.env` is never committed to version control (currently it is present in the project root).

### 2. CSRF Protection Disabled (HIGH)
- **Description:** Cross-Site Request Forgery (CSRF) protection is explicitly disabled in `SecurityConfig.java` across all Spring Boot microservices.
- **Impact:** If the application uses session-based authentication (or if JWTs are stored in cookies), users could be vulnerable to CSRF attacks, allowing attackers to perform actions on their behalf.
- **Remediation:** Enable CSRF protection. If the API is purely stateless and uses `Authorization` headers (not cookies), the risk is mitigated, but defense-in-depth suggests enabling it or strictly verifying the `Origin`/`Referer` headers.

### 3. Insecure Hibernate DDL Configuration (HIGH)
- **Description:** In `.env`, `SPRING_JPA_HIBERNATE_DDL_AUTO` is set to `update`.
- **Impact:** In a production environment, this can lead to accidental schema changes, data loss, or corruption if the application starts with a different version of the entity models.
- **Remediation:** Set `spring.jpa.hibernate.ddl-auto` to `validate` or `none` in production. Use a migration tool like Flyway (which is already present) for all schema changes.

### 4. Overly Permissive API Access (MEDIUM)
- **Description:** The `auth-service` has broad `permitAll()` rules in its `SecurityConfig.java` for several endpoints, and some actuator endpoints might be exposed depending on environment variables.
- **Impact:** Increases the attack surface.
- **Remediation:** Strictly limit `permitAll()` to only the necessary login/public endpoints. Ensure all management endpoints are authenticated and restricted to `ADMIN` roles.

### 5. Missing Security Headers (MEDIUM)
- **Description:** While Spring Security provides some default headers, the current configuration does not explicitly define a Content Security Policy (CSP) or other advanced security headers.
- **Impact:** Increased risk of XSS and clickjacking attacks.
- **Remediation:** Configure `X-Content-Type-Options`, `X-Frame-Options`, and a strict `Content-Security-Policy`.

### 6. Incomplete Security Scanning (LOW)
- **Description:** The `ops/security/scan.py` script only fails on HIGH and CRITICAL findings and requires a local Maven repository and Docker to run.
- **Impact:** Potential medium/low vulnerabilities may go unnoticed, and the scan is difficult to run in restricted environments.
- **Remediation:** Integrate security scanning (SAST/DAST) into the CI/CD pipeline and include all severity levels in the report.

## Conclusion
The project implements modern security standards like BCrypt and JWT, but these are undermined by insecure configuration and secret management. Immediate action should be taken to rotate all secrets and harden the Spring Security configurations.
