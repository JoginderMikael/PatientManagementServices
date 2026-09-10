import { apiRequest } from '../../api/client';
import { Appointment, AppointmentCommand, ChartData, ClinicalAlert, ClinicalNote, Diagnosis, DoctorSchedule, Encounter, HandoffCommand, LabResult, MedicalHistory, Patient, PatientCommand, Prescription, StaffTask, TaskHistory, Vaccination } from './types';

const value = (row: Record<string, unknown>, camel: string, snake: string) => row[camel] ?? row[snake];
const text = (v: unknown) => typeof v === 'string' ? v : '';

export function normalizeTask(row: Record<string, unknown>): StaffTask {
  return { id: text(value(row,'id','id')), assigneeId: text(value(row,'assigneeId','assignee_id')), patientId: text(value(row,'patientId','patient_id')), title: text(value(row,'title','title')), priority: text(value(row,'priority','priority')), status: text(value(row,'status','status')), updatedAt: text(value(row,'updatedAt','updated_at')), dueAt: text(value(row,'dueAt','due_at')), escalated: Boolean(value(row,'escalated','escalated')), queueRole: text(value(row,'queueRole','queue_role')) };
}
export function normalizeHistory(row: Record<string, unknown>): TaskHistory {
  return { id:text(row.id), taskId:text(value(row,'taskId','task_id')), actorId:text(value(row,'actorId','actor_id')), action:text(row.action), comment:text(row.comment), occurredAt:text(value(row,'occurredAt','occurred_at')) };
}

export const listPatients = (token:string, signal?:AbortSignal) => apiRequest<Patient[]>('/api/patients',{token,signal});
export const getPatient = (id:string, token:string, signal?:AbortSignal) => apiRequest<Patient>(`/api/patients/${encodeURIComponent(id)}`,{token,signal});
export const findDuplicates = (query:string, token:string, signal?:AbortSignal) => apiRequest<Patient[]>(`/api/patients/duplicates?name=${encodeURIComponent(query)}`,{token,signal});
export const createPatient = (body:PatientCommand, token:string, idempotencyKey:string) => apiRequest<Patient,PatientCommand>('/api/patients',{method:'POST',body,token,idempotencyKey});
export const updatePatient = (id:string, body:PatientCommand, token:string) => apiRequest<Patient,PatientCommand>(`/api/patients/${encodeURIComponent(id)}`,{method:'PUT',body,token});
export const changePatientStatus = (id:string,status:string,token:string) => apiRequest<Patient>(`/api/patients/${encodeURIComponent(id)}/status/${encodeURIComponent(status)}`,{method:'POST',token});

export async function getQueue(role:string,limit:number,token:string,signal?:AbortSignal){ return (await apiRequest<Record<string,unknown>[]>(`/api/staff-dashboard/queues/${encodeURIComponent(role)}?limit=${limit}`,{token,signal})).map(normalizeTask); }
export const claimTask=(id:string,token:string)=>apiRequest<void>(`/api/staff-dashboard/tasks/${id}/claim`,{method:'POST',token,responseType:'empty'});
export const completeTask=(id:string,token:string)=>apiRequest<StaffTask>(`/api/staff-dashboard/tasks/${id}/complete`,{method:'POST',token});
export const handoffTask=(id:string,body:HandoffCommand,token:string)=>apiRequest<void,HandoffCommand>(`/api/staff-dashboard/tasks/${id}/handoff`,{method:'POST',body,token,responseType:'empty'});
export const commentTask=(id:string,text:string,token:string)=>apiRequest<void>(`/api/staff-dashboard/tasks/${id}/comments`,{method:'POST',body:{text},token,responseType:'empty'});
export async function getTaskHistory(id:string,token:string,signal?:AbortSignal){return (await apiRequest<Record<string,unknown>[]>(`/api/staff-dashboard/tasks/${id}/history`,{token,signal})).map(normalizeHistory);}

export const listSchedules=(token:string,signal?:AbortSignal)=>apiRequest<DoctorSchedule[]>('/api/appointments/schedules',{token,signal});
export const listAppointments=(token:string,patientId?:string,signal?:AbortSignal)=>apiRequest<Appointment[]>(`/api/appointments${patientId?`?patientId=${encodeURIComponent(patientId)}`:''}`,{token,signal});
export const bookAppointment=(body:AppointmentCommand,token:string)=>apiRequest<Appointment,AppointmentCommand>('/api/appointments',{method:'POST',body,token});

export const getAlerts=async(patientId:string,token:string,signal?:AbortSignal):Promise<ClinicalAlert[]> => (await apiRequest<Record<string,unknown>[]>(`/api/ehr/safety/alerts/patient/${encodeURIComponent(patientId)}`,{token,signal})).map(row=>({id:text(row.id),patientId:text(value(row,'patientId','patient_id')),assigneeId:text(value(row,'assigneeId','assignee_id')),summary:text(row.summary),status:text(row.status),createdAt:text(value(row,'createdAt','created_at'))}));

export async function getChart(patientId:string,token:string,signal?:AbortSignal):Promise<ChartData>{
  const id=encodeURIComponent(patientId); const options={token,signal};
  const [encounters,histories,diagnoses,prescriptions,labs,notes,vaccinations,rawAlerts]=await Promise.all([
    apiRequest<Encounter[]>(`/api/ehr/encounters/${id}`,options),apiRequest<MedicalHistory[]>(`/api/ehr/histories/${id}`,options),apiRequest<Diagnosis[]>(`/api/ehr/diagnoses/${id}`,options),apiRequest<Prescription[]>(`/api/ehr/prescriptions/${id}`,options),apiRequest<LabResult[]>(`/api/ehr/lab-results/${id}`,options),apiRequest<ClinicalNote[]>(`/api/ehr/notes/${id}`,options),apiRequest<Vaccination[]>(`/api/ehr/vaccinations/${id}`,options),apiRequest<Record<string,unknown>[]>(`/api/ehr/safety/alerts/patient/${id}`,options)
  ]);
  const alerts=rawAlerts.map(row=>({id:text(row.id),patientId:text(value(row,'patientId','patient_id')),assigneeId:text(value(row,'assigneeId','assignee_id')),summary:text(row.summary),status:text(row.status),createdAt:text(value(row,'createdAt','created_at'))} as ClinicalAlert));
  return {encounters,histories,diagnoses,prescriptions,labs,notes,vaccinations,alerts};
}
export const signNote=(id:string,token:string)=>apiRequest<ClinicalNote>(`/api/ehr/notes/${id}/sign`,{method:'POST',token});
export const discontinuePrescription=(id:string,token:string)=>apiRequest<void>(`/api/ehr/safety/prescriptions/${id}/discontinue`,{method:'POST',token,responseType:'empty'});
export const acknowledgeAlert=(id:string,token:string)=>apiRequest<void>(`/api/ehr/safety/alerts/${id}/acknowledge`,{method:'POST',token,responseType:'empty'});
