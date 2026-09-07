CREATE TABLE invoice (
 id UUID PRIMARY KEY, patient_id UUID NOT NULL, reference VARCHAR(200) NOT NULL UNIQUE,
 amount NUMERIC(19,2) NOT NULL CHECK(amount>0), currency VARCHAR(3) NOT NULL, paid NUMERIC(19,2) NOT NULL DEFAULT 0 CHECK(paid>=0 AND paid<=amount), created_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE INDEX ix_invoice_patient ON invoice(patient_id);
CREATE TABLE payment_posting (id UUID PRIMARY KEY, invoice_id UUID NOT NULL REFERENCES invoice(id), reference VARCHAR(200) NOT NULL UNIQUE, amount NUMERIC(19,2) NOT NULL CHECK(amount>0), kind VARCHAR(30) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE revenue_creation_lock (id INTEGER PRIMARY KEY);
INSERT INTO revenue_creation_lock VALUES (1);
