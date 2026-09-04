CREATE TABLE audit_event (
    id UUID PRIMARY KEY, source_event_id UUID NOT NULL UNIQUE, actor_id VARCHAR(255) NOT NULL,
    actor_role VARCHAR(255) NOT NULL, action VARCHAR(255) NOT NULL, patient_id UUID,
    resource_type VARCHAR(255) NOT NULL, resource_id UUID, source_service VARCHAR(255) NOT NULL,
    outcome VARCHAR(255) NOT NULL, reason VARCHAR(255), endpoint VARCHAR(1000), request_id VARCHAR(255),
    correlation_id VARCHAR(255), occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    previous_hash VARCHAR(64) NOT NULL, event_hash VARCHAR(64) NOT NULL
);
CREATE INDEX idx_audit_patient_time ON audit_event(patient_id,occurred_at);
CREATE INDEX idx_audit_actor_time ON audit_event(actor_id,occurred_at);
