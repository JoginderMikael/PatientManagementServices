CREATE TABLE clinical_safety_lock (id INTEGER PRIMARY KEY);
INSERT INTO clinical_safety_lock VALUES (1);
CREATE TABLE medication_safety_rule (id UUID PRIMARY KEY, medication VARCHAR(200) NOT NULL, interacting_medication VARCHAR(200) NOT NULL, reason VARCHAR(1000) NOT NULL, UNIQUE(medication,interacting_medication));
CREATE TABLE clinical_alert (id UUID PRIMARY KEY, patient_id UUID NOT NULL, assignee_id UUID NOT NULL, source_reference VARCHAR(200) NOT NULL UNIQUE, summary VARCHAR(2000) NOT NULL, status VARCHAR(30) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL, acknowledged_by VARCHAR(200), acknowledged_at TIMESTAMP WITH TIME ZONE);
CREATE INDEX ix_clinical_alert_patient ON clinical_alert(patient_id,status);
