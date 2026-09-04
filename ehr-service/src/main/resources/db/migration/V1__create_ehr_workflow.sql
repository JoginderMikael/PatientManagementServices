CREATE TABLE encounter (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, clinician_id UUID NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL, ended_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(255) NOT NULL, reason VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_encounter_patient ON encounter(patient_id, started_at);

CREATE TABLE medical_history (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, summary TEXT NOT NULL,
    allergies TEXT NOT NULL, chronic_conditions TEXT NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE TABLE diagnosis (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, encounter_id UUID, clinician_id UUID NOT NULL,
    code VARCHAR(255) NOT NULL, description VARCHAR(255) NOT NULL, diagnosed_on DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_diagnosis_encounter FOREIGN KEY(encounter_id) REFERENCES encounter(id)
);
CREATE TABLE prescription (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, encounter_id UUID, clinician_id UUID NOT NULL,
    medication VARCHAR(255) NOT NULL, dosage VARCHAR(255) NOT NULL, instructions TEXT NOT NULL,
    status VARCHAR(255) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_prescription_encounter FOREIGN KEY(encounter_id) REFERENCES encounter(id)
);
CREATE TABLE lab_result (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, encounter_id UUID, test_name VARCHAR(255) NOT NULL,
    result_summary TEXT NOT NULL, source VARCHAR(255) NOT NULL, collected_on DATE NOT NULL,
    imported_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_lab_encounter FOREIGN KEY(encounter_id) REFERENCES encounter(id)
);
CREATE TABLE clinical_note (
    id UUID PRIMARY KEY, encounter_id UUID NOT NULL, patient_id UUID NOT NULL, clinician_id UUID NOT NULL,
    body TEXT NOT NULL, status VARCHAR(255) NOT NULL, signed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_note_encounter FOREIGN KEY(encounter_id) REFERENCES encounter(id)
);
CREATE TABLE vaccination_record (
    id UUID PRIMARY KEY, patient_id UUID NOT NULL, vaccine VARCHAR(255) NOT NULL,
    administered_on DATE NOT NULL, lot_number VARCHAR(255) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_diagnosis_patient ON diagnosis(patient_id);
CREATE INDEX idx_prescription_patient ON prescription(patient_id);
CREATE INDEX idx_lab_patient ON lab_result(patient_id);
CREATE INDEX idx_note_patient ON clinical_note(patient_id, created_at);
