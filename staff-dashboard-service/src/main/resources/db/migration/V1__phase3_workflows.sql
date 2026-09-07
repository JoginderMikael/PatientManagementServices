CREATE TABLE workflow_lock (id INTEGER PRIMARY KEY);
INSERT INTO workflow_lock VALUES (1);
CREATE TABLE clinical_round (
id UUID PRIMARY KEY,
staff_id UUID NOT NULL,
patient_id UUID NOT NULL,
unit VARCHAR(4096) NOT NULL,
notes VARCHAR(4096) NOT NULL,
status VARCHAR(30) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_clinical_round_patient_id ON clinical_round (patient_id);
CREATE TABLE patient_chart_summary (
id UUID PRIMARY KEY,
patient_id UUID NOT NULL,
summary VARCHAR(4096) NOT NULL,
risk_level VARCHAR(4096) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_patient_chart_summary_patient_id ON patient_chart_summary (patient_id);
CREATE TABLE staff_task (
id UUID PRIMARY KEY,
assignee_id UUID NOT NULL,
patient_id UUID NOT NULL,
title VARCHAR(4096) NOT NULL,
priority VARCHAR(30) NOT NULL,
status VARCHAR(30) NOT NULL,
updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX ix_staff_task_assignee_id ON staff_task (assignee_id);
CREATE INDEX ix_staff_task_patient_id ON staff_task (patient_id);

CREATE TABLE task_workflow (task_id UUID PRIMARY KEY REFERENCES staff_task(id), queue_role VARCHAR(30) NOT NULL, due_at TIMESTAMP WITH TIME ZONE NOT NULL, escalation_role VARCHAR(30) NOT NULL, escalated BOOLEAN NOT NULL DEFAULT FALSE);
CREATE INDEX ix_task_due ON task_workflow(escalated,due_at);
CREATE INDEX ix_task_queue ON task_workflow(queue_role);
CREATE TABLE task_history (id UUID PRIMARY KEY, task_id UUID NOT NULL REFERENCES staff_task(id), actor VARCHAR(200) NOT NULL, action VARCHAR(50) NOT NULL, comment VARCHAR(2000) NOT NULL, occurred_at TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE clinical_task_source (reference VARCHAR(200) PRIMARY KEY, task_id UUID NOT NULL UNIQUE REFERENCES staff_task(id));
ALTER TABLE staff_task ADD CONSTRAINT ck_task_priority CHECK(priority IN ('ROUTINE','HIGH','URGENT'));
ALTER TABLE staff_task ADD CONSTRAINT ck_task_status CHECK(status IN ('OPEN','IN_PROGRESS','ESCALATED','COMPLETED'));
