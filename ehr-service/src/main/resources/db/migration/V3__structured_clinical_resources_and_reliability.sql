CREATE TABLE reliable_event_outbox (
    id UUID PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    event_key VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    replay_count INTEGER NOT NULL DEFAULT 0,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    claimed_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE,
    dead_lettered_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT
);
CREATE INDEX ix_reliable_outbox_dispatch
    ON reliable_event_outbox(status, available_at, created_at);
CREATE INDEX ix_reliable_outbox_dead_letter
    ON reliable_event_outbox(status, dead_lettered_at);

CREATE TABLE clinical_terminology_code (
    id UUID PRIMARY KEY,
    system_uri VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    display VARCHAR(255) NOT NULL,
    resource_types VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ux_clinical_terminology UNIQUE(system_uri, code)
);

CREATE TABLE clinical_resource (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    encounter_id UUID,
    resource_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    code_system VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    display VARCHAR(255) NOT NULL,
    effective_at TIMESTAMP WITH TIME ZONE NOT NULL,
    current_version INTEGER NOT NULL,
    created_by VARCHAR(200) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_clinical_resource_encounter FOREIGN KEY(encounter_id) REFERENCES encounter(id)
);
CREATE INDEX ix_clinical_resource_patient_type
    ON clinical_resource(patient_id, resource_type, effective_at);

CREATE TABLE clinical_resource_version (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    payload TEXT NOT NULL,
    amendment_reason VARCHAR(1000),
    recorded_by VARCHAR(200) NOT NULL,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    previous_hash VARCHAR(64),
    content_hash VARCHAR(64) NOT NULL,
    CONSTRAINT fk_resource_version FOREIGN KEY(resource_id) REFERENCES clinical_resource(id),
    CONSTRAINT ux_resource_version UNIQUE(resource_id, version_number)
);

CREATE TABLE clinical_provenance (
    id UUID PRIMARY KEY,
    resource_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    action VARCHAR(30) NOT NULL,
    actor_id VARCHAR(200) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    CONSTRAINT fk_provenance_resource FOREIGN KEY(resource_id) REFERENCES clinical_resource(id)
);
CREATE INDEX ix_clinical_provenance_resource
    ON clinical_provenance(resource_id, version_number);

CREATE TABLE reliable_event_inbox (
    id UUID PRIMARY KEY,
    consumer_name VARCHAR(100) NOT NULL,
    event_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    status VARCHAR(24) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    claimed_at TIMESTAMP WITH TIME ZONE,
    processed_at TIMESTAMP WITH TIME ZONE,
    last_error TEXT,
    CONSTRAINT ux_reliable_inbox_event UNIQUE(consumer_name, event_id)
);
CREATE INDEX ix_reliable_inbox_operations
    ON reliable_event_inbox(status, received_at);
