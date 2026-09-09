export interface PortalAppointmentRequest {
  id: string;
  patientId: string;
  preferredSpecialty: string;
  reason: string;
  status: string;
  createdAt: string;
}

export interface RecordAccessRequest {
  id: string;
  patientId: string;
  recordType: string;
  status: string;
  createdAt: string;
}

export interface PortalPayment {
  id: string;
  patientId: string;
  invoiceId: string;
  amount: number;
  status: string;
  createdAt: string;
}

export interface PortalOverview {
  patientId: string;
  accountStatus: string;
  appointments: PortalAppointmentRequest[];
  recordRequests: RecordAccessRequest[];
  payments: PortalPayment[];
}

export interface AppointmentCommand {
  patientId: string;
  preferredSpecialty: string;
  reason: string;
}

export interface RecordCommand {
  patientId: string;
  recordType: string;
}

export interface PaymentCommand {
  patientId: string;
  invoiceId: string;
  amount: number;
}
