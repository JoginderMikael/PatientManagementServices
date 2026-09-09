-- Run as the audit database migration owner, with the runtime role provided by psql:
-- psql -v runtime_role=pms_audit_runtime -v ON_ERROR_STOP=1 -f audit-immutability.sql
-- The runtime role must NOT own tables, inherit the migration role, or be superuser.
BEGIN;
REVOKE UPDATE, DELETE, TRUNCATE ON audit_event, compliance_history FROM :"runtime_role";
GRANT SELECT, INSERT ON audit_event, compliance_history TO :"runtime_role";
CREATE OR REPLACE FUNCTION reject_evidence_mutation() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
  RAISE EXCEPTION 'Evidence is append-only';
END;
$$;
CREATE TRIGGER audit_event_immutable BEFORE UPDATE OR DELETE OR TRUNCATE ON audit_event
FOR EACH STATEMENT EXECUTE FUNCTION reject_evidence_mutation();
CREATE TRIGGER compliance_history_immutable BEFORE UPDATE OR DELETE OR TRUNCATE ON compliance_history
FOR EACH STATEMENT EXECUTE FUNCTION reject_evidence_mutation();
COMMIT;
