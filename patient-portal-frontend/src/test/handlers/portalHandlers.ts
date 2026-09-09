import { rest } from "msw";
import { API_BASE_URL } from "../../api/client";

export const syntheticPatientId = "10000000-0000-4000-8000-000000000001";

export const syntheticOverview = {
  patientId: syntheticPatientId,
  accountStatus: "ACTIVE",
  appointments: [
    {
      id: "20000000-0000-4000-8000-000000000001",
      patientId: syntheticPatientId,
      preferredSpecialty: "General medicine",
      reason: "Synthetic wellness visit",
      status: "REQUESTED",
      createdAt: "2026-09-08T08:00:00Z",
    },
  ],
  recordRequests: [
    {
      id: "30000000-0000-4000-8000-000000000001",
      patientId: syntheticPatientId,
      recordType: "LAB_RESULTS",
      status: "FULFILLED",
      createdAt: "2026-09-07T08:00:00Z",
    },
  ],
  payments: [
    {
      id: "40000000-0000-4000-8000-000000000001",
      patientId: syntheticPatientId,
      invoiceId: "50000000-0000-4000-8000-000000000001",
      amount: 1250,
      status: "SUBMITTED",
      createdAt: "2026-09-06T08:00:00Z",
    },
  ],
};

export const portalHandlers = [
  rest.get(
    `${API_BASE_URL}/api/portal/patients/:patientId/overview`,
    (_request, response, context) => response(context.json(syntheticOverview)),
  ),
  rest.post(
    `${API_BASE_URL}/api/portal/appointment-requests`,
    async (request, response, context) => {
      const body = await request.json<Record<string, string>>();
      return response(
        context.status(201),
        context.json({
          id: "20000000-0000-4000-8000-000000000002",
          ...body,
          status: "REQUESTED",
          createdAt: "2026-09-09T08:00:00Z",
        }),
      );
    },
  ),
  rest.post(
    `${API_BASE_URL}/api/portal/appointment-requests/:id/cancel`,
    (_request, response, context) => response(context.status(200)),
  ),
  rest.post(
    `${API_BASE_URL}/api/portal/record-requests`,
    async (request, response, context) => {
      const body = await request.json<Record<string, string>>();
      return response(
        context.status(201),
        context.json({
          id: "30000000-0000-4000-8000-000000000002",
          ...body,
          status: "READY_FOR_REVIEW",
          createdAt: "2026-09-09T08:00:00Z",
        }),
      );
    },
  ),
  rest.get(
    `${API_BASE_URL}/api/portal/record-requests/:id/download`,
    (_request, response, context) =>
      response(
        context.set("Content-Type", "text/plain"),
        context.set("Cache-Control", "no-store"),
        context.body("Synthetic record release"),
      ),
  ),
  rest.post(
    `${API_BASE_URL}/api/portal/payments`,
    async (request, response, context) => {
      const body = await request.json<Record<string, unknown>>();
      return response(
        context.status(201),
        context.json({
          id: "40000000-0000-4000-8000-000000000002",
          ...body,
          status: "SUBMITTED",
          createdAt: "2026-09-09T08:00:00Z",
        }),
      );
    },
  ),
];
