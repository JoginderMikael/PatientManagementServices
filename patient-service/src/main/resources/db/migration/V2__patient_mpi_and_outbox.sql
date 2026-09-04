ALTER TABLE patient ADD COLUMN mrn VARCHAR(64);
ALTER TABLE patient ADD COLUMN phone VARCHAR(40);
ALTER TABLE patient ADD COLUMN gender VARCHAR(40);
ALTER TABLE patient ADD COLUMN preferred_language VARCHAR(20);
ALTER TABLE patient ADD COLUMN status VARCHAR(24) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE patient ADD COLUMN merged_into_patient_id UUID;
ALTER TABLE patient ADD COLUMN registration_key VARCHAR(100);
ALTER TABLE patient ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE patient ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE patient ADD COLUMN updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP;

UPDATE patient SET mrn = 'MRN-' || REPLACE(CAST(id AS VARCHAR), '-', '') WHERE mrn IS NULL;
ALTER TABLE patient ALTER COLUMN mrn SET NOT NULL;
ALTER TABLE patient ADD CONSTRAINT uk_patient_mrn UNIQUE (mrn);
ALTER TABLE patient ADD CONSTRAINT uk_patient_registration_key UNIQUE (registration_key);

CREATE TABLE patient_identifier (
    patient_id UUID NOT NULL,
    identifier_system VARCHAR(255) NOT NULL,
    identifier_value VARCHAR(255) NOT NULL,
    PRIMARY KEY (patient_id, identifier_system),
    CONSTRAINT fk_patient_identifier_patient FOREIGN KEY (patient_id) REFERENCES patient(id),
    CONSTRAINT uk_patient_identifier UNIQUE (identifier_system, identifier_value)
);

CREATE TABLE patient_merge_history (
    id UUID PRIMARY KEY,
    source_patient_id UUID NOT NULL,
    target_patient_id UUID NOT NULL,
    merged_at TIMESTAMP WITH TIME ZONE NOT NULL,
    unmerged_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_merge_source FOREIGN KEY (source_patient_id) REFERENCES patient(id),
    CONSTRAINT fk_merge_target FOREIGN KEY (target_patient_id) REFERENCES patient(id)
);

CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_key VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_patient_outbox_pending ON outbox_event (published_at, created_at);
