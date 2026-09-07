CREATE TABLE workflow_lock (id INTEGER PRIMARY KEY);
INSERT INTO workflow_lock VALUES (1);
CREATE TABLE supply_item (
id UUID PRIMARY KEY,
name VARCHAR(4096) NOT NULL,
quantity_on_hand INTEGER NOT NULL,
reorder_threshold INTEGER NOT NULL,
unit_cost NUMERIC(19,2) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE medication_stock (
id UUID PRIMARY KEY,
name VARCHAR(4096) NOT NULL,
ndc_code VARCHAR(4096) NOT NULL,
quantity_on_hand INTEGER NOT NULL,
unit_cost NUMERIC(19,2) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE pharmacy_prescription (
id UUID PRIMARY KEY,
ehr_prescription_id UUID NOT NULL,
patient_id UUID NOT NULL,
medication VARCHAR(4096) NOT NULL,
quantity INTEGER NOT NULL,
status VARCHAR(30) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_pharmacy_prescription_ehr_prescription_id ON pharmacy_prescription (ehr_prescription_id);
CREATE INDEX ix_pharmacy_prescription_patient_id ON pharmacy_prescription (patient_id);
CREATE TABLE medication_charge (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
prescription_id UUID NOT NULL,
amount NUMERIC(19,2) NOT NULL,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_medication_charge_patient_id ON medication_charge (patient_id);

ALTER TABLE medication_stock ADD CONSTRAINT ck_stock CHECK(quantity_on_hand>=0);
ALTER TABLE pharmacy_prescription ADD CONSTRAINT uq_ehr_prescription UNIQUE(ehr_prescription_id);
ALTER TABLE medication_charge ADD CONSTRAINT uq_prescription_charge UNIQUE(prescription_id);
CREATE TABLE medication_batch (id UUID PRIMARY KEY, medication_id UUID NOT NULL REFERENCES medication_stock(id), lot VARCHAR(100) NOT NULL, expires_on DATE NOT NULL, quantity INTEGER NOT NULL CHECK(quantity>=0), UNIQUE(medication_id,lot));
CREATE INDEX ix_batch_fefo ON medication_batch(medication_id,expires_on);
CREATE TABLE stock_movement (id UUID PRIMARY KEY, medication_id UUID NOT NULL REFERENCES medication_stock(id), batch_id UUID REFERENCES medication_batch(id), prescription_id UUID REFERENCES pharmacy_prescription(id), delta INTEGER NOT NULL, reason VARCHAR(200) NOT NULL, reference VARCHAR(200) NOT NULL UNIQUE, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE dispensing_review (prescription_id UUID PRIMARY KEY REFERENCES pharmacy_prescription(id), medication_id UUID NOT NULL REFERENCES medication_stock(id), reviewer VARCHAR(200) NOT NULL, reviewed_at TIMESTAMP WITH TIME ZONE NOT NULL, outcome VARCHAR(20) NOT NULL, reason VARCHAR(1000) NOT NULL);
CREATE TABLE supply_movement (id UUID PRIMARY KEY, supply_id UUID NOT NULL REFERENCES supply_item(id), delta INTEGER NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL);
ALTER TABLE supply_item ADD CONSTRAINT ck_supply_quantity CHECK(quantity_on_hand>=0);
ALTER TABLE pharmacy_prescription ADD CONSTRAINT ck_rx_quantity CHECK(quantity>0);
ALTER TABLE medication_charge ADD CONSTRAINT fk_charge_prescription FOREIGN KEY(prescription_id) REFERENCES pharmacy_prescription(id);
ALTER TABLE medication_charge ADD CONSTRAINT ck_charge_amount CHECK(amount>=0);
ALTER TABLE medication_stock ADD CONSTRAINT ck_medication_cost CHECK(unit_cost>=0);
