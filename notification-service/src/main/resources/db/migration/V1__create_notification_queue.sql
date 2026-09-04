CREATE TABLE notification_message (
    id UUID PRIMARY KEY, recipient_id UUID NOT NULL, channel VARCHAR(20) NOT NULL,
    destination VARCHAR(255) NOT NULL, template VARCHAR(255) NOT NULL, body TEXT NOT NULL,
    status VARCHAR(24) NOT NULL, correlation_id UUID, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL, sent_at TIMESTAMP WITH TIME ZONE,
    attempts INTEGER NOT NULL DEFAULT 0, provider_message_id VARCHAR(255), last_error TEXT,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_notification_dispatch ON notification_message(status,next_attempt_at);
CREATE INDEX idx_notification_recipient ON notification_message(recipient_id,created_at);
