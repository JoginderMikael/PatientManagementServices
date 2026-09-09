-- Development-only identity bindings for the synthetic PATIENT users seeded by
-- auth-service V1001. Production identity binding remains an administrative flow.
INSERT INTO portal_identity (subject, patient_id)
VALUES
    ('10000000-0000-4000-8000-000000000003', '10000000-0000-4000-8000-000000000003'),
    ('10000000-0000-4000-8000-000000000004', '10000000-0000-4000-8000-000000000004')
ON CONFLICT DO NOTHING;
