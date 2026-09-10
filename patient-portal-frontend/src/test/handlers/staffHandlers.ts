import { rest } from "msw";
import { API_BASE_URL } from "../../api/client";

export const syntheticPatient = {
  id: "20000000-0000-4000-8000-000000000001",
  name: "Amara Njeri",
  email: "amara@example.test",
  address: "Synthetic district",
  dateOfBirth: "1988-02-14",
  mrn: "MRN-1047-62",
  phone: "+254700000001",
  gender: "Female",
  preferredLanguage: "English",
  status: "ACTIVE",
  version: 1,
};
export const syntheticTask = {
  id: "30000000-0000-4000-8000-000000000001",
  assignee_id: "10000000-0000-4000-8000-000000000009",
  patient_id: syntheticPatient.id,
  title: "Review synthetic discharge plan",
  priority: "URGENT",
  status: "OPEN",
  updated_at: "2026-09-10T07:00:00Z",
  due_at: "2026-09-10T08:00:00Z",
  escalated: false,
  queue_role: "CLINICIAN",
};

const json =
  (body: unknown, status = 200) =>
  (_: unknown, res: any, ctx: any) =>
    res(ctx.status(status), ctx.json(body));
export const staffHandlers = [
  rest.get(`${API_BASE_URL}/api/patients`, json([syntheticPatient])),
  rest.get(`${API_BASE_URL}/api/patients/duplicates`, json([])),
  rest.get(`${API_BASE_URL}/api/patients/:id`, json(syntheticPatient)),
  rest.get(
    `${API_BASE_URL}/api/staff-dashboard/queues/:role`,
    json([syntheticTask]),
  ),
  rest.get(
    `${API_BASE_URL}/api/staff-dashboard/tasks/:id/history`,
    json([
      {
        id: "31000000-0000-4000-8000-000000000001",
        task_id: syntheticTask.id,
        actor_id: "SYSTEM",
        action: "CREATED",
        comment: "",
        occurred_at: "2026-09-10T07:00:00Z",
      },
    ]),
  ),
  rest.post(
    `${API_BASE_URL}/api/staff-dashboard/tasks/:id/claim`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/staff-dashboard/tasks/:id/complete`,
    json({ ...syntheticTask, status: "COMPLETED" }),
  ),
  rest.post(
    `${API_BASE_URL}/api/staff-dashboard/tasks/:id/comments`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/staff-dashboard/tasks/:id/handoff`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.get(
    `${API_BASE_URL}/api/appointments/schedules`,
    json([
      {
        id: "60000000-0000-4000-8000-000000000001",
        doctorId: "10000000-0000-4000-8000-000000000001",
        workDate: "2026-09-12",
        startsAt: "08:00:00",
        endsAt: "16:00:00",
        location: "Synthetic Clinic",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/appointments`,
    json([
      {
        id: "61000000-0000-4000-8000-000000000001",
        patientId: syntheticPatient.id,
        doctorId: "10000000-0000-4000-8000-000000000001",
        startsAt: "2026-09-12T09:30:00",
        endsAt: "2026-09-12T10:00:00",
        reason: "Synthetic follow-up",
        status: "SCHEDULED",
        updatedAt: "2026-09-10T07:00:00Z",
      },
    ]),
  ),
  rest.post(`${API_BASE_URL}/api/appointments`, async (req, res, ctx) => {
    const body = await req.json<Record<string, unknown>>();
    return res(
      ctx.status(201),
      ctx.json({
        id: "61000000-0000-4000-8000-000000000002",
        ...body,
        endsAt: "2026-09-12T10:00:00",
        status: "SCHEDULED",
        updatedAt: "2026-09-10T07:00:00Z",
      }),
    );
  }),
  rest.get(
    `${API_BASE_URL}/api/ehr/encounters/:id`,
    json([
      {
        id: "70000000-0000-4000-8000-000000000001",
        clinicianId: "10000000-0000-4000-8000-000000000001",
        startedAt: "2026-09-09T08:00:00Z",
        status: "FINISHED",
        reason: "Synthetic review",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/histories/:id`,
    json([
      {
        id: "71000000-0000-4000-8000-000000000001",
        summary: "Synthetic hypertension follow-up",
        allergies: ["Penicillin"],
        chronicConditions: ["Hypertension"],
        createdAt: "2026-09-09T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/diagnoses/:id`,
    json([
      {
        id: "72000000-0000-4000-8000-000000000001",
        code: "SYN-1",
        description: "Synthetic diagnosis",
        diagnosedOn: "2026-09-09",
        clinicianId: "10000000-0000-4000-8000-000000000001",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/prescriptions/:id`,
    json([
      {
        id: "73000000-0000-4000-8000-000000000001",
        medication: "Synthetic medicine",
        dosage: "10 mg",
        instructions: "Once daily",
        status: "ACTIVE",
        createdAt: "2026-09-09T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/lab-results/:id`,
    json([
      {
        id: "74000000-0000-4000-8000-000000000001",
        testName: "Synthetic panel",
        resultSummary: "Within synthetic range",
        source: "Example laboratory",
        collectedOn: "2026-09-09",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/notes/:id`,
    json([
      {
        id: "75000000-0000-4000-8000-000000000001",
        encounterId: "70000000-0000-4000-8000-000000000001",
        clinicianId: "10000000-0000-4000-8000-000000000001",
        body: "Synthetic signed note",
        status: "SIGNED",
        signedAt: "2026-09-09T09:00:00Z",
        createdAt: "2026-09-09T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/vaccinations/:id`,
    json([
      {
        id: "76000000-0000-4000-8000-000000000001",
        vaccine: "Synthetic vaccine",
        administeredOn: "2026-01-10",
        lotNumber: "SYN-LOT",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/ehr/safety/alerts/patient/:id`,
    json([
      {
        id: "77000000-0000-4000-8000-000000000001",
        patient_id: syntheticPatient.id,
        assignee_id: "10000000-0000-4000-8000-000000000001",
        summary: "Synthetic allergy alert",
        status: "OPEN",
      },
    ]),
  ),
  rest.post(
    `${API_BASE_URL}/api/ehr/safety/alerts/:id/acknowledge`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/ehr/safety/prescriptions/:id/discontinue`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(`${API_BASE_URL}/api/ehr/notes/:id/sign`, json({})),
];
