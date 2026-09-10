-- Keep local PATIENT login identities connected to real MPI patient records.
INSERT INTO patient (id, mrn, name, email, address, date_of_birth, registered_date, status)
SELECT '10000000-0000-4000-8000-000000000003',
       'MRN-DEV-PORTAL-0001',
       'Development Patient One',
       'patient.one@example.test',
       'Synthetic development address',
       '1990-01-01',
       CURRENT_DATE,
       'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE id = '10000000-0000-4000-8000-000000000003'
       OR email = 'patient.one@example.test'
       OR mrn = 'MRN-DEV-PORTAL-0001'
);

INSERT INTO patient (id, mrn, name, email, address, date_of_birth, registered_date, status)
SELECT '10000000-0000-4000-8000-000000000004',
       'MRN-DEV-PORTAL-0002',
       'Development Patient Two',
       'patient.two@example.test',
       'Synthetic development address',
       '1992-02-02',
       CURRENT_DATE,
       'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM patient
    WHERE id = '10000000-0000-4000-8000-000000000004'
       OR email = 'patient.two@example.test'
       OR mrn = 'MRN-DEV-PORTAL-0002'
);
