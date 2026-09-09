import { FormEvent, useId } from 'react';
import { Alert } from '../feedback/Alert';
import { Field, Select, TextArea } from '../forms/FormControls';
import { Dialog } from './Dialog';

export function BreakGlassDialog({ open, patientLabel, onClose, onConfirm }: { open: boolean; patientLabel: string; onClose(): void; onConfirm(input: { reason: string; evidence: string; durationMinutes: number }): void }) {
  const prefix = useId();
  function submit() {
    const form = document.getElementById(`${prefix}-form`) as HTMLFormElement | null;
    form?.requestSubmit();
  }
  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    onConfirm({ reason: String(data.get('reason') ?? ''), evidence: String(data.get('evidence') ?? ''), durationMinutes: Number(data.get('duration') ?? 15) });
  }
  return <Dialog open={open} title="Request emergency access" description={`Request time-limited access to ${patientLabel}.`} confirmLabel="Request access" danger onClose={onClose} onConfirm={submit}>
    <Alert title="Emergency access is audited" tone="warning">Use only when necessary for immediate treatment. An independent privacy review will follow.</Alert>
    <form id={`${prefix}-form`} onSubmit={onSubmit}>
      <Field id={`${prefix}-reason`} label="Clinical reason" required><TextArea name="reason" required /></Field>
      <Field id={`${prefix}-evidence`} label="Evidence reference" required help="Use an approved identifier; do not paste record contents."><TextArea name="evidence" required /></Field>
      <Field id={`${prefix}-duration`} label="Duration" required><Select name="duration" required><option value="15">15 minutes</option><option value="30">30 minutes</option><option value="60">60 minutes</option></Select></Field>
    </form>
  </Dialog>;
}
