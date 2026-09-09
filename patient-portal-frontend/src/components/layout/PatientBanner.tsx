import { StatusBadge } from '../data/StatusBadge';

export interface PatientContextSummary {
  id: string;
  name: string;
  mrn: string;
  dateOfBirth: string;
  lifecycleStatus: string;
  accessState: string;
  allergySummary?: string;
}

export function PatientBanner({ patient, onChangePatient }: { patient: PatientContextSummary; onChangePatient?(): void }) {
  return <section className="patient-banner" aria-label="Current patient context">
    <div><strong className="patient-name">{patient.name}</strong><span>{patient.mrn} · {patient.accessState}</span></div>
    <dl><div><dt>Date of birth</dt><dd>{patient.dateOfBirth}</dd></div><div><dt>Lifecycle</dt><dd><StatusBadge tone="success">{patient.lifecycleStatus}</StatusBadge></dd></div>{patient.allergySummary && <div><dt>Allergies</dt><dd className="danger-text">{patient.allergySummary}</dd></div>}</dl>
    {onChangePatient && <button className="button button--secondary button--small" type="button" onClick={onChangePatient}>Change patient</button>}
  </section>;
}
