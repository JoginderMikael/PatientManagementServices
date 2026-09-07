CREATE TABLE workflow_lock (id INTEGER PRIMARY KEY);
INSERT INTO workflow_lock VALUES (1);
CREATE TABLE portal_appointment_request (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
preferred_specialty VARCHAR(4096) NOT NULL,
reason VARCHAR(4096) NOT NULL,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_portal_appointment_request_patient_id ON portal_appointment_request (patient_id);
CREATE TABLE record_access_request (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
record_type VARCHAR(4096) NOT NULL,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_record_access_request_patient_id ON record_access_request (patient_id);
CREATE TABLE portal_payment (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
invoice_id UUID NOT NULL,
amount NUMERIC(19,2) NOT NULL,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_portal_payment_patient_id ON portal_payment (patient_id);
CREATE INDEX ix_portal_payment_invoice_id ON portal_payment (invoice_id);

CREATE TABLE portal_identity (subject VARCHAR(200) PRIMARY KEY, patient_id UUID NOT NULL UNIQUE);
CREATE TABLE proxy_grant (id UUID PRIMARY KEY, patient_id UUID NOT NULL, proxy_subject VARCHAR(200) NOT NULL, scope VARCHAR(30) NOT NULL, expires_at TIMESTAMP WITH TIME ZONE NOT NULL, revoked BOOLEAN NOT NULL DEFAULT FALSE, granted_by VARCHAR(200) NOT NULL, UNIQUE(patient_id,proxy_subject,scope));
CREATE INDEX ix_proxy_access ON proxy_grant(patient_id,proxy_subject,scope);
CREATE TABLE record_release (request_id UUID PRIMARY KEY REFERENCES record_access_request(id), content TEXT NOT NULL, released_by VARCHAR(200) NOT NULL, released_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE portal_payment_reference (reference VARCHAR(200) PRIMARY KEY, payment_id UUID NOT NULL UNIQUE REFERENCES portal_payment(id));
CREATE TABLE portal_history (id UUID PRIMARY KEY, resource_id UUID NOT NULL, actor VARCHAR(200) NOT NULL, action VARCHAR(100) NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE portal_settlement (payment_id UUID PRIMARY KEY REFERENCES portal_payment(id), provider_reference VARCHAR(200) NOT NULL UNIQUE, posting_id UUID NOT NULL UNIQUE);
CREATE TABLE portal_appointment_resolution (request_id UUID PRIMARY KEY REFERENCES portal_appointment_request(id), appointment_reference VARCHAR(200) NOT NULL);
ALTER TABLE portal_payment ADD CONSTRAINT ck_portal_payment CHECK(amount>0);
