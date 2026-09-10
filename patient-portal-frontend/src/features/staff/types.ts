export interface Patient {
  id: string;
  name: string;
  email: string;
  address: string;
  dateOfBirth: string;
  mrn: string;
  phone?: string;
  gender?: string;
  preferredLanguage?: string;
  status: string;
  mergedIntoPatientId?: string;
  version: number;
}

export interface PatientCommand {
  name: string;
  email: string;
  address: string;
  dateOfBirth: string;
  registeredDate?: string;
  phone?: string;
  gender?: string;
  preferredLanguage?: string;
}

export interface StaffTask {
  id: string;
  assigneeId: string;
  patientId: string;
  title: string;
  priority: string;
  status: string;
  updatedAt: string;
  dueAt: string;
  escalated: boolean;
  queueRole: string;
}
export interface TaskHistory {
  id: string;
  taskId: string;
  actorId: string;
  action: string;
  comment: string;
  occurredAt: string;
}
export interface HandoffCommand {
  assigneeId: string;
  queueRole: string;
  dueAt: string;
  reason: string;
}

export interface DoctorSchedule {
  id: string;
  doctorId: string;
  workDate: string;
  startsAt: string;
  endsAt: string;
  location: string;
}
export interface Appointment {
  id: string;
  patientId: string;
  doctorId: string;
  startsAt: string;
  endsAt: string;
  reason: string;
  status: string;
  cancellationReason?: string;
  updatedAt: string;
}
export interface AppointmentCommand {
  patientId: string;
  doctorId: string;
  startsAt: string;
  reason: string;
  durationMinutes: number;
}

export interface Encounter {
  id: string;
  clinicianId: string;
  startedAt: string;
  endedAt?: string;
  status: string;
  reason: string;
}
export interface MedicalHistory {
  id: string;
  summary: string;
  allergies: string[];
  chronicConditions: string[];
  createdAt: string;
}
export interface Diagnosis {
  id: string;
  code: string;
  description: string;
  diagnosedOn: string;
  clinicianId: string;
}
export interface Prescription {
  id: string;
  medication: string;
  dosage: string;
  instructions: string;
  status: string;
  createdAt: string;
}
export interface LabResult {
  id: string;
  testName: string;
  resultSummary: string;
  source: string;
  collectedOn: string;
}
export interface ClinicalNote {
  id: string;
  encounterId: string;
  clinicianId: string;
  body: string;
  status: string;
  signedAt?: string;
  createdAt: string;
}
export interface Vaccination {
  id: string;
  vaccine: string;
  administeredOn: string;
  lotNumber: string;
}
export interface ClinicalAlert {
  id: string;
  patientId: string;
  assigneeId: string;
  summary: string;
  status: string;
  createdAt?: string;
}
export interface ChartData {
  encounters: Encounter[];
  histories: MedicalHistory[];
  diagnoses: Diagnosis[];
  prescriptions: Prescription[];
  labs: LabResult[];
  notes: ClinicalNote[];
  vaccinations: Vaccination[];
  alerts: ClinicalAlert[];
}
