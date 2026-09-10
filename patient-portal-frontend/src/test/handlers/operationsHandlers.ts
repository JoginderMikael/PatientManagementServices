import { rest } from "msw";
import { API_BASE_URL } from "../../api/client";

const patient = "20000000-0000-4000-8000-000000000001";
const json =
  (body: unknown, status = 200) =>
  (_: unknown, res: any, ctx: any) =>
    res(ctx.status(status), ctx.json(body));
export const operationsHandlers = [
  rest.get(
    `${API_BASE_URL}/api/billing/accounts`,
    json([
      {
        id: "b1",
        patientId: patient,
        status: "ACTIVE",
        balance: 125.5,
        currency: "USD",
        updatedAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/billing/invoices/patient/:id`,
    json([
      {
        id: "i1",
        patient_id: patient,
        reference: "INV-SYN-1",
        amount: 200,
        paid: 75,
        currency: "USD",
        created_at: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.post(
    `${API_BASE_URL}/api/billing/accounts`,
    json(
      {
        id: "b2",
        patientId: patient,
        status: "ACTIVE",
        balance: 0,
        currency: "USD",
      },
      201,
    ),
  ),
  rest.post(
    `${API_BASE_URL}/api/billing/invoices`,
    json(
      {
        id: "i2",
        patient_id: patient,
        reference: "INV-SYN-2",
        amount: 10,
        paid: 0,
        currency: "USD",
      },
      200,
    ),
  ),
  rest.post(
    `${API_BASE_URL}/api/billing/invoices/:id/postings`,
    json({ id: "p1" }),
  ),
  rest.get(
    `${API_BASE_URL}/api/insurance/claims`,
    json([
      {
        id: "c1",
        patientId: patient,
        policyId: "policy1",
        invoiceId: "i1",
        amount: 200,
        status: "SUBMITTED",
        updatedAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/insurance/coverage-verifications`,
    json([
      {
        id: "v1",
        policyId: "policy1",
        serviceCode: "SYN",
        status: "VERIFIED",
        insuranceResponsibility: 160,
        patientResponsibility: 40,
        verifiedAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.post(`${API_BASE_URL}/api/insurance/claims/:id/adjudicate`, json({})),
  rest.post(`${API_BASE_URL}/api/insurance/claims/:id/reconcile`, json({})),
  rest.get(
    `${API_BASE_URL}/api/inventory-pharmacy/medications`,
    json([
      {
        id: "m1",
        name: "Synthetic medicine",
        ndcCode: "SYN-001",
        quantityOnHand: 20,
        unitCost: 2.5,
        updatedAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/inventory-pharmacy/supplies`,
    json([
      {
        id: "s1",
        name: "Synthetic gloves",
        quantityOnHand: 4,
        reorderThreshold: 10,
        unitCost: 1,
        updatedAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/inventory-pharmacy/medication-charges`,
    json([
      {
        id: "mc1",
        patientId: patient,
        prescriptionId: "rx1",
        amount: 25,
        status: "POSTED",
        createdAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.post(`${API_BASE_URL}/api/inventory-pharmacy/supplies`, json({})),
  rest.post(`${API_BASE_URL}/api/inventory-pharmacy/batches`, json({})),
  rest.post(
    `${API_BASE_URL}/api/inventory-pharmacy/prescriptions/:id/review`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/inventory-pharmacy/prescriptions/:id/dispense`,
    json({ status: "DISPENSED" }),
  ),
  rest.get(
    `${API_BASE_URL}/api/notifications`,
    json([
      {
        id: "n1",
        recipientId: patient,
        channel: "EMAIL",
        destination: "synthetic@example.test",
        template: "OPERATIONAL",
        body: "Synthetic message",
        status: "SENT",
        attempts: 1,
        createdAt: "2026-09-10T08:00:00Z",
        sentAt: "2026-09-10T08:01:00Z",
      },
    ]),
  ),
  rest.post(
    `${API_BASE_URL}/api/notifications`,
    json({ id: "n2", status: "QUEUED" }, 202),
  ),
  rest.get(
    `${API_BASE_URL}/api/compliance/cases`,
    json([
      {
        id: "case1",
        kind: "ACCESS_REVIEW",
        status: "OPEN",
        owner: "Privacy team",
        evidence: "CASE-1",
        due_at: "2026-09-20T08:00:00Z",
        created_at: "2026-09-10T08:00:00Z",
        updated_at: "2026-09-10T08:00:00Z",
        legal_hold: false,
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/compliance/privacy/break-glass/reviews`,
    json([
      {
        id: "g1",
        patient_id: patient,
        subject: "clinician-1",
        kind: "BREAK_GLASS",
        expires_at: "2026-09-10T09:00:00Z",
        revoked: false,
        created_by: "clinician-1",
        created_at: "2026-09-10T08:00:00Z",
        evidence: "EM-1",
      },
    ]),
  ),
  rest.post(`${API_BASE_URL}/api/compliance/cases`, json({ id: "case2" })),
  rest.post(
    `${API_BASE_URL}/api/compliance/cases/:id/transitions`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.post(`${API_BASE_URL}/api/compliance/cases/:id/hold`, (_req, res, ctx) =>
    res(ctx.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/compliance/privacy/break-glass/:id/review`,
    (_req, res, ctx) => res(ctx.status(200)),
  ),
  rest.get(
    `${API_BASE_URL}/api/audit/events`,
    json([
      {
        id: "a1",
        actorId: "actor-1",
        actorRole: "CLINICIAN",
        action: "PATIENT_READ",
        patientId: patient,
        resourceType: "Patient",
        resourceId: patient,
        sourceService: "patient-service",
        outcome: "SUCCESS",
        occurredAt: "2026-09-10T08:00:00Z",
        eventHash: "1234567890abcdef",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/analytics/health-trends`,
    json([
      {
        id: "t1",
        metric: "visits",
        segment: "adult",
        periodStart: "2026-09-01",
        periodEnd: "2026-09-30",
        value: 42,
        createdAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/analytics/chronic-disease-cohorts`,
    json([
      {
        id: "co1",
        condition: "Synthetic condition",
        riskLevel: "HIGH",
        patientCount: 3,
        createdAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.get(
    `${API_BASE_URL}/api/analytics/regulatory-reports`,
    json([
      {
        id: "r1",
        reportType: "MONTHLY",
        periodStart: "2026-08-01",
        periodEnd: "2026-08-31",
        status: "READY",
        createdAt: "2026-09-10T08:00:00Z",
      },
    ]),
  ),
  rest.post(
    `${API_BASE_URL}/api/analytics/regulatory-reports`,
    json(
      {
        id: "r2",
        reportType: "MONTHLY",
        status: "QUEUED",
        createdAt: "2026-09-10T08:00:00Z",
      },
      201,
    ),
  ),
];
