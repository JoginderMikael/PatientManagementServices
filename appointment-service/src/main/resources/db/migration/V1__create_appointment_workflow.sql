CREATE TABLE doctor_schedule (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    work_date DATE NOT NULL,
    starts_at TIME NOT NULL,
    ends_at TIME NOT NULL,
    location VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_doctor_schedule UNIQUE (doctor_id, work_date)
);

CREATE TABLE appointment (
    id UUID PRIMARY KEY,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(24) NOT NULL,
    cancellation_reason VARCHAR(255),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_appointment_interval CHECK (ends_at > starts_at)
);
CREATE INDEX idx_appointment_patient ON appointment (patient_id, starts_at);
CREATE INDEX idx_appointment_doctor ON appointment (doctor_id, starts_at);

CREATE TABLE appointment_slot (
    id UUID PRIMARY KEY,
    appointment_id UUID NOT NULL,
    doctor_id UUID NOT NULL,
    slot_time TIMESTAMP NOT NULL,
    CONSTRAINT fk_appointment_slot FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
    CONSTRAINT uk_doctor_appointment_slot UNIQUE (doctor_id, slot_time)
);

CREATE TABLE appointment_waitlist (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, doctor_id UUID NOT NULL,
    preferred_date DATE NOT NULL, reason VARCHAR(255) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE virtual_consultation (
    id UUID PRIMARY KEY, appointment_id UUID NOT NULL, provider VARCHAR(255) NOT NULL,
    join_url VARCHAR(1000) NOT NULL, status VARCHAR(64) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_virtual_appointment FOREIGN KEY (appointment_id) REFERENCES appointment(id)
);
CREATE TABLE appointment_consent (
    id UUID PRIMARY KEY, appointment_id UUID NOT NULL, patient_id UUID NOT NULL,
    form_type VARCHAR(255) NOT NULL, signature TEXT NOT NULL, signed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_consent_appointment FOREIGN KEY (appointment_id) REFERENCES appointment(id)
);
