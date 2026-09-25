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
