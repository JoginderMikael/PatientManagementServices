import { apiRequest } from "../../api/client";
import {
  AuditEvent,
  BillingAccount,
  ComplianceCase,
  CoverageVerification,
  DiseaseCohort,
  HealthTrend,
  InsuranceClaim,
  Invoice,
  MedicationCharge,
  MedicationStock,
  NotificationMessage,
  PrivacyReview,
  RegulatoryReport,
  SupplyItem,
} from "./types";

const value = (row: Record<string, unknown>, camel: string, snake: string) =>
  row[camel] ?? row[snake];
const text = (v: unknown) => (typeof v === "string" ? v : "");
const num = (v: unknown) => (typeof v === "number" ? v : Number(v) || 0);
const bool = (v: unknown) => v === true;
const options = (token: string, signal?: AbortSignal) => ({ token, signal });

export const normalizeInvoice = (r: Record<string, unknown>): Invoice => ({
  id: text(r.id),
  patientId: text(value(r, "patientId", "patient_id")),
  reference: text(r.reference),
  amount: num(r.amount),
  paid: num(r.paid),
  currency: text(r.currency),
  createdAt: text(value(r, "createdAt", "created_at")),
});
export const normalizeReview = (r: Record<string, unknown>): PrivacyReview => ({
  id: text(r.id),
  patientId: text(value(r, "patientId", "patient_id")),
  subject: text(r.subject),
  kind: text(r.kind),
  expiresAt: text(value(r, "expiresAt", "expires_at")),
  revoked: bool(r.revoked),
  createdBy: text(value(r, "createdBy", "created_by")),
  createdAt: text(value(r, "createdAt", "created_at")),
  evidence: text(r.evidence),
});
export const normalizeCase = (r: Record<string, unknown>): ComplianceCase => ({
  id: text(r.id),
  kind: text(r.kind),
  status: text(r.status),
  owner: text(r.owner),
  evidence: text(r.evidence),
  dueAt: text(value(r, "dueAt", "due_at")),
  createdAt: text(value(r, "createdAt", "created_at")),
  updatedAt: text(value(r, "updatedAt", "updated_at")),
  legalHold: bool(value(r, "legalHold", "legal_hold")),
});

export const listBillingAccounts = (token: string, signal?: AbortSignal) =>
  apiRequest<BillingAccount[]>("/api/billing/accounts", options(token, signal));
export const createBillingAccount = (
  patientId: string,
  key: string,
  token: string,
) =>
  apiRequest<BillingAccount>("/api/billing/accounts", {
    method: "POST",
    body: { patientId },
    idempotencyKey: key,
    token,
  });
export const listPatientInvoices = async (
  patientId: string,
  token: string,
  signal?: AbortSignal,
) =>
  (
    await apiRequest<Record<string, unknown>[]>(
      `/api/billing/invoices/patient/${encodeURIComponent(patientId)}`,
      options(token, signal),
    )
  ).map(normalizeInvoice);
export const createInvoice = async (
  body: {
    patientId: string;
    reference: string;
    amount: number;
    currency: string;
  },
  token: string,
) =>
  normalizeInvoice(
    await apiRequest<Record<string, unknown>>("/api/billing/invoices", {
      method: "POST",
      body,
      token,
    }),
  );
export const postPayment = (
  invoiceId: string,
  body: { reference: string; amount: number; kind: "PAYMENT" | "REMITTANCE" },
  token: string,
) =>
  apiRequest<Record<string, unknown>>(
    `/api/billing/invoices/${invoiceId}/postings`,
    { method: "POST", body, token },
  );

export const listClaims = (token: string, signal?: AbortSignal) =>
  apiRequest<InsuranceClaim[]>("/api/insurance/claims", options(token, signal));
export const listCoverage = (token: string, signal?: AbortSignal) =>
  apiRequest<CoverageVerification[]>(
    "/api/insurance/coverage-verifications",
    options(token, signal),
  );
export const adjudicateClaim = (
  id: string,
  status: "APPROVED" | "DENIED",
  approvedAmount: number,
  token: string,
) =>
  apiRequest<InsuranceClaim>(`/api/insurance/claims/${id}/adjudicate`, {
    method: "POST",
    body: { status, approvedAmount },
    token,
  });
export const reconcileClaim = (id: string, token: string) =>
  apiRequest<Record<string, unknown>>(`/api/insurance/claims/${id}/reconcile`, {
    method: "POST",
    token,
  });

export const listMedications = (token: string, signal?: AbortSignal) =>
  apiRequest<MedicationStock[]>(
    "/api/inventory-pharmacy/medications",
    options(token, signal),
  );
export const listSupplies = (token: string, signal?: AbortSignal) =>
  apiRequest<SupplyItem[]>(
    "/api/inventory-pharmacy/supplies",
    options(token, signal),
  );
export const listMedicationCharges = (token: string, signal?: AbortSignal) =>
  apiRequest<MedicationCharge[]>(
    "/api/inventory-pharmacy/medication-charges",
    options(token, signal),
  );
export const reviewPrescription = (
  id: string,
  body: {
    medicationId: string;
    allergiesChecked: boolean;
    interactionsChecked: boolean;
    doseChecked: boolean;
    reason: string;
  },
  token: string,
) =>
  apiRequest<void>(`/api/inventory-pharmacy/prescriptions/${id}/review`, {
    method: "POST",
    body,
    token,
    responseType: "empty",
  });
export const dispensePrescription = (id: string, token: string) =>
  apiRequest<Record<string, unknown>>(
    `/api/inventory-pharmacy/prescriptions/${id}/dispense`,
    { method: "POST", token },
  );
export const receiveBatch = (
  body: {
    medicationId: string;
    lot: string;
    expiresOn: string;
    quantity: number;
    reference: string;
  },
  token: string,
) =>
  apiRequest<Record<string, unknown>>("/api/inventory-pharmacy/batches", {
    method: "POST",
    body,
    token,
  });
export const upsertSupply = (
  body: {
    name: string;
    quantityOnHand: number;
    reorderThreshold: number;
    unitCost: number;
  },
  token: string,
) =>
  apiRequest<SupplyItem>("/api/inventory-pharmacy/supplies", {
    method: "POST",
    body,
    token,
  });

export const listNotifications = (
  recipientId: string,
  token: string,
  signal?: AbortSignal,
) =>
  apiRequest<NotificationMessage[]>(
    `/api/notifications${recipientId ? `?recipientId=${encodeURIComponent(recipientId)}` : ""}`,
    options(token, signal),
  );
export const sendNotification = (
  body: {
    recipientId: string;
    channel: string;
    destination: string;
    template: string;
    body: string;
  },
  token: string,
) =>
  apiRequest<NotificationMessage>("/api/notifications", {
    method: "POST",
    body,
    token,
  });

export const listPrivacyReviews = async (token: string, signal?: AbortSignal) =>
  (
    await apiRequest<Record<string, unknown>[]>(
      "/api/compliance/privacy/break-glass/reviews",
      options(token, signal),
    )
  ).map(normalizeReview);
export const reviewEmergencyGrant = (
  id: string,
  evidenceReference: string,
  token: string,
) =>
  apiRequest<void>(`/api/compliance/privacy/break-glass/${id}/review`, {
    method: "POST",
    body: { evidenceReference },
    token,
    responseType: "empty",
  });
export const listComplianceCases = async (
  overdue: boolean,
  token: string,
  signal?: AbortSignal,
) =>
  (
    await apiRequest<Record<string, unknown>[]>(
      `/api/compliance/cases?overdue=${overdue}`,
      options(token, signal),
    )
  ).map(normalizeCase);
export const createComplianceCase = (
  body: {
    kind: string;
    owner: string;
    evidenceReference: string;
    dueAt: string;
  },
  token: string,
) =>
  apiRequest<{ id: string }>("/api/compliance/cases", {
    method: "POST",
    body,
    token,
  });
export const transitionComplianceCase = (
  id: string,
  body: { expectedStatus: string; status: string; evidenceReference: string },
  token: string,
) =>
  apiRequest<void>(`/api/compliance/cases/${id}/transitions`, {
    method: "POST",
    body,
    token,
    responseType: "empty",
  });
export const setComplianceHold = (
  id: string,
  enabled: boolean,
  evidenceReference: string,
  token: string,
) =>
  apiRequest<void>(`/api/compliance/cases/${id}/hold`, {
    method: "POST",
    body: { enabled, evidenceReference },
    token,
    responseType: "empty",
  });

export const listAuditEvents = (
  patientId: string,
  actorId: string,
  token: string,
  signal?: AbortSignal,
) => {
  const q = new URLSearchParams();
  if (patientId) q.set("patientId", patientId);
  if (actorId) q.set("actorId", actorId);
  return apiRequest<AuditEvent[]>(
    `/api/audit/events${q.size ? `?${q}` : ""}`,
    options(token, signal),
  );
};
export const listTrends = (token: string, signal?: AbortSignal) =>
  apiRequest<HealthTrend[]>(
    "/api/analytics/health-trends",
    options(token, signal),
  );
export const listCohorts = (token: string, signal?: AbortSignal) =>
  apiRequest<DiseaseCohort[]>(
    "/api/analytics/chronic-disease-cohorts",
    options(token, signal),
  );
export const listReports = (token: string, signal?: AbortSignal) =>
  apiRequest<RegulatoryReport[]>(
    "/api/analytics/regulatory-reports",
    options(token, signal),
  );
export const createReport = (
  body: { reportType: string; periodStart?: string; periodEnd?: string },
  token: string,
) =>
  apiRequest<RegulatoryReport>("/api/analytics/regulatory-reports", {
    method: "POST",
    body,
    token,
  });
