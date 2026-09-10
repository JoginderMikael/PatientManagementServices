ALTER TABLE portal_identity ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE portal_identity ADD COLUMN patient_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE portal_identity ADD CONSTRAINT ck_portal_identity_status
    CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'FAILED', 'REVOKED'));
ALTER TABLE portal_identity ADD CONSTRAINT ck_portal_identity_patient_status
    CHECK (patient_status IN ('ACTIVE', 'INACTIVE', 'MERGED', 'ARCHIVED'));
