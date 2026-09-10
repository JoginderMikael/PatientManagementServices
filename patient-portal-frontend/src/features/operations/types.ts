export interface BillingAccount {
  id: string;
  patientId: string;
  status: string;
  balance: number;
  currency: string;
  updatedAt: string;
}
export interface Invoice {
  id: string;
  patientId: string;
  reference: string;
  amount: number;
  paid: number;
  currency: string;
  createdAt: string;
}
export interface InsuranceClaim {
  id: string;
  patientId: string;
  policyId: string;
  invoiceId: string;
  amount: number;
  status: string;
  updatedAt: string;
}
export interface CoverageVerification {
  id: string;
  policyId: string;
  serviceCode: string;
  status: string;
  insuranceResponsibility: number;
  patientResponsibility: number;
  verifiedAt: string;
}
export interface MedicationStock {
  id: string;
  name: string;
  ndcCode: string;
  quantityOnHand: number;
  unitCost: number;
  updatedAt: string;
}
export interface SupplyItem {
  id: string;
  name: string;
  quantityOnHand: number;
  reorderThreshold: number;
  unitCost: number;
  updatedAt: string;
}
export interface MedicationCharge {
  id: string;
  patientId: string;
  prescriptionId: string;
  amount: number;
  status: string;
  createdAt: string;
}
export interface NotificationMessage {
  id: string;
  recipientId: string;
  channel: string;
  destination: string;
  template: string;
  body: string;
  status: string;
  attempts: number;
  createdAt: string;
  sentAt?: string;
  lastError?: string;
}
export interface PrivacyReview {
  id: string;
  patientId: string;
  subject: string;
  kind: string;
  expiresAt: string;
  revoked: boolean;
  createdBy: string;
  createdAt: string;
  evidence: string;
}
export interface ComplianceCase {
  id: string;
  kind: string;
  status: string;
  owner: string;
  evidence: string;
  dueAt: string;
  createdAt: string;
  updatedAt: string;
  legalHold: boolean;
}
export interface AuditEvent {
  id: string;
  actorId: string;
  actorRole: string;
  action: string;
  patientId?: string;
  resourceType: string;
  resourceId?: string;
  sourceService: string;
  outcome: string;
  reason?: string;
  endpoint?: string;
  occurredAt: string;
  eventHash: string;
}
export interface HealthTrend {
  id: string;
  metric: string;
  segment: string;
  periodStart?: string;
  periodEnd?: string;
  value: number;
  createdAt: string;
}
export interface DiseaseCohort {
  id: string;
  condition: string;
  riskLevel: string;
  patientCount: number;
  createdAt: string;
}
export interface RegulatoryReport {
  id: string;
  reportType: string;
  periodStart?: string;
  periodEnd?: string;
  status: string;
  createdAt: string;
}
