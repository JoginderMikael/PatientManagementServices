CREATE TABLE workflow_lock (id INTEGER PRIMARY KEY);
INSERT INTO workflow_lock VALUES (1);
CREATE TABLE insurance_policy (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
provider_name VARCHAR(200) NOT NULL,
member_number VARCHAR(200) NOT NULL,
plan_name VARCHAR(200) NOT NULL,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_insurance_policy_patient_id ON insurance_policy (patient_id);
CREATE TABLE coverage_verification (
id UUID PRIMARY KEY,
policy_id UUID NOT NULL,
service_code VARCHAR(4096) NOT NULL,
status VARCHAR(30) NOT NULL,
insurance_responsibility NUMERIC(19,2) NOT NULL,
patient_responsibility NUMERIC(19,2) NOT NULL,
verified_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE claim (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
policy_id UUID NOT NULL,
invoice_id UUID NOT NULL,
amount NUMERIC(19,2) NOT NULL,
status VARCHAR(30) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_claim_patient_id ON claim (patient_id);
CREATE INDEX ix_claim_invoice_id ON claim (invoice_id);

ALTER TABLE insurance_policy ADD CONSTRAINT uq_policy_member UNIQUE(provider_name, member_number, patient_id);
ALTER TABLE claim ADD CONSTRAINT uq_claim_invoice UNIQUE(invoice_id);
CREATE TABLE eligibility_evidence (policy_id UUID PRIMARY KEY REFERENCES insurance_policy(id), service_code VARCHAR(100) NOT NULL, valid_until DATE NOT NULL, payer_reference VARCHAR(200) NOT NULL, insurance_percent NUMERIC(5,2) NOT NULL CHECK(insurance_percent BETWEEN 0 AND 100));
CREATE TABLE claim_adjudication (claim_id UUID PRIMARY KEY REFERENCES claim(id), approved_amount NUMERIC(19,2) NOT NULL CHECK(approved_amount>=0));
CREATE TABLE remittance (id UUID PRIMARY KEY, claim_id UUID NOT NULL UNIQUE REFERENCES claim(id), reference VARCHAR(200) NOT NULL UNIQUE, paid_amount NUMERIC(19,2) NOT NULL CHECK(paid_amount>0), received_at TIMESTAMP WITH TIME ZONE NOT NULL);
ALTER TABLE coverage_verification ADD CONSTRAINT fk_coverage_policy FOREIGN KEY(policy_id) REFERENCES insurance_policy(id);
ALTER TABLE claim ADD CONSTRAINT fk_claim_policy FOREIGN KEY(policy_id) REFERENCES insurance_policy(id);
ALTER TABLE claim ADD CONSTRAINT ck_claim_amount CHECK(amount>0);
ALTER TABLE claim ADD CONSTRAINT ck_claim_status CHECK(status IN ('SUBMITTED','APPROVED','DENIED','PAID'));
