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

ALTER TABLE appointment ADD COLUMN appointment_type VARCHAR(50) NOT NULL DEFAULT 'GENERAL';
ALTER TABLE appointment ADD COLUMN location_id UUID;
ALTER TABLE appointment ADD COLUMN room_id UUID;
ALTER TABLE appointment ADD COLUMN recurrence_group_id UUID;
ALTER TABLE appointment ADD COLUMN checked_in_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE appointment ADD COLUMN checked_out_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE appointment ADD COLUMN no_show_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE appointment_waitlist ADD COLUMN status VARCHAR(24) NOT NULL DEFAULT 'WAITING';
ALTER TABLE appointment_waitlist ADD COLUMN offered_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE appointment_waitlist ADD COLUMN promoted_appointment_id UUID;
ALTER TABLE appointment_waitlist ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE appointment_resource (
    id UUID PRIMARY KEY,
    resource_type VARCHAR(24) NOT NULL,
    name VARCHAR(255) NOT NULL,
    location_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE appointment_resource_slot (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    resource_id UUID NOT NULL,
    slot_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_resource_slot_appointment FOREIGN KEY(appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_slot_resource FOREIGN KEY(resource_id) REFERENCES appointment_resource(id),
    CONSTRAINT ux_resource_slot UNIQUE(resource_id, slot_time)
);
CREATE INDEX ix_waitlist_promotion
    ON appointment_waitlist(doctor_id, preferred_date, status, created_at);

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
