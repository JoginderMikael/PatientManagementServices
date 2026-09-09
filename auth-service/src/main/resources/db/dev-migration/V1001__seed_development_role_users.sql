-- Synthetic development-only identities. The shared plaintext password is
-- documented in api-requests/auth-service/login.http and must never be reused
-- outside a local test environment.
INSERT INTO users (id, email, password, role)
VALUES
    ('10000000-0000-4000-8000-000000000001', 'admin.one@example.test',        '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'ADMIN'),
    ('10000000-0000-4000-8000-000000000002', 'admin.two@example.test',        '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'ADMIN'),
    ('10000000-0000-4000-8000-000000000003', 'patient.one@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PATIENT'),
    ('10000000-0000-4000-8000-000000000004', 'patient.two@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PATIENT'),
    ('10000000-0000-4000-8000-000000000005', 'clinician.one@example.test',    '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'CLINICIAN'),
    ('10000000-0000-4000-8000-000000000006', 'clinician.two@example.test',    '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'CLINICIAN'),
    ('10000000-0000-4000-8000-000000000007', 'nurse.one@example.test',        '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'NURSE'),
    ('10000000-0000-4000-8000-000000000008', 'nurse.two@example.test',        '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'NURSE'),
    ('10000000-0000-4000-8000-000000000009', 'registration.one@example.test', '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'REGISTRATION_STAFF'),
    ('10000000-0000-4000-8000-000000000010', 'registration.two@example.test', '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'REGISTRATION_STAFF'),
    ('10000000-0000-4000-8000-000000000011', 'receptionist.one@example.test', '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'RECEPTIONIST'),
    ('10000000-0000-4000-8000-000000000012', 'receptionist.two@example.test', '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'RECEPTIONIST'),
    ('10000000-0000-4000-8000-000000000013', 'billing.one@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'BILLING_STAFF'),
    ('10000000-0000-4000-8000-000000000014', 'billing.two@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'BILLING_STAFF'),
    ('10000000-0000-4000-8000-000000000015', 'pharmacist.one@example.test',   '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PHARMACIST'),
    ('10000000-0000-4000-8000-000000000016', 'pharmacist.two@example.test',   '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PHARMACIST'),
    ('10000000-0000-4000-8000-000000000017', 'lab.one@example.test',          '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'LAB_STAFF'),
    ('10000000-0000-4000-8000-000000000018', 'lab.two@example.test',          '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'LAB_STAFF'),
    ('10000000-0000-4000-8000-000000000019', 'privacy.one@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PRIVACY_OFFICER'),
    ('10000000-0000-4000-8000-000000000020', 'privacy.two@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'PRIVACY_OFFICER'),
    ('10000000-0000-4000-8000-000000000021', 'auditor.one@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'AUDITOR'),
    ('10000000-0000-4000-8000-000000000022', 'auditor.two@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'AUDITOR'),
    ('10000000-0000-4000-8000-000000000023', 'analyst.one@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'ANALYST'),
    ('10000000-0000-4000-8000-000000000024', 'analyst.two@example.test',      '$2a$12$MxU8v2xUFI8ag/u8UySiTuhsUJTs4yBi465pCzf/3YDmE/lbqS/4W', 'ANALYST')
ON CONFLICT DO NOTHING;
