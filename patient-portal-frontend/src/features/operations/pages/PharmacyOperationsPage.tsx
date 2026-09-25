import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import {
  Checkbox,
  Field,
  TextArea,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import {
  dispensePrescription,
  listMedicationCharges,
  listMedications,
  reviewPrescription,
} from "../api";
import {
  formatMoney,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";
export function PharmacyOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [form, setForm] = useState({
    prescriptionId: "",
    medicationId: "",
    allergiesChecked: false,
    interactionsChecked: false,
    doseChecked: false,
    reason: "",
  });
  const meds = useQuery({
    queryKey: ["operations", "pharmacy", "medications"],
    queryFn: ({ signal }) => listMedications(session!.token, signal),
  });
  const charges = useQuery({
    queryKey: ["operations", "pharmacy", "charges"],
    queryFn: ({ signal }) => listMedicationCharges(session!.token, signal),
  });
  const refresh = () =>
    client.invalidateQueries({ queryKey: ["operations", "pharmacy"] });
  const review = useMutation({
    mutationFn: () =>
      reviewPrescription(
        form.prescriptionId,
        {
          medicationId: form.medicationId,
          allergiesChecked: form.allergiesChecked,
          interactionsChecked: form.interactionsChecked,
          doseChecked: form.doseChecked,
          reason: form.reason,
        },
        session!.token,
      ),
    onSuccess: refresh,
    onError: refresh,
  });
  const dispense = useMutation({
    mutationFn: () => dispensePrescription(form.prescriptionId, session!.token),
    onSuccess: refresh,
    onError: refresh,
  });
  const submit = (e: FormEvent) => {
    e.preventDefault();
    review.mutate();
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Pharmacy"
        description="Record all three medication-safety checks before requesting authoritative dispensing."
      />
      <StaffSection
        title="Safety review and dispensing"
        description="A completed browser form is not approval: the service verifies EHR state, reviewed medication, unexpired batch stock and concurrency."
      >
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field id="rx-id" label="Pharmacy prescription UUID" required>
              <TextField
                required
                value={form.prescriptionId}
                onChange={(e) =>
                  setForm({ ...form, prescriptionId: e.target.value })
                }
              />
            </Field>
            <Field id="rx-medication" label="Medication UUID" required>
              <TextField
                required
                value={form.medicationId}
                onChange={(e) =>
                  setForm({ ...form, medicationId: e.target.value })
                }
              />
            </Field>
          </div>
          <Field id="rx-allergies" label="Allergy check">
            <Checkbox
              checked={form.allergiesChecked}
              onChange={(e) =>
                setForm({ ...form, allergiesChecked: e.target.checked })
              }
            >
              Allergies checked against the active record
            </Checkbox>
          </Field>
          <Field id="rx-interactions" label="Interaction check">
            <Checkbox
              checked={form.interactionsChecked}
              onChange={(e) =>
                setForm({ ...form, interactionsChecked: e.target.checked })
              }
            >
              Medication interactions checked
            </Checkbox>
          </Field>
          <Field id="rx-dose" label="Dose check">
            <Checkbox
              checked={form.doseChecked}
              onChange={(e) =>
                setForm({ ...form, doseChecked: e.target.checked })
              }
            >
              Dose and instructions checked
            </Checkbox>
          </Field>
          <Field id="rx-reason" label="Review evidence/reason" required>
            <TextArea
              required
              value={form.reason}
              onChange={(e) => setForm({ ...form, reason: e.target.value })}
            />
          </Field>
          <div className="task-actions">
            <Button
              type="submit"
              disabled={
                review.isPending ||
                !form.allergiesChecked ||
                !form.interactionsChecked ||
                !form.doseChecked
              }
            >
              Record safety approval
            </Button>
            <ConfirmAction
              triggerLabel="Dispense"
              title="Dispense medication"
              description={`Dispense prescription ${form.prescriptionId || "not selected"} only after reviewing current safety and stock state.`}
              confirmLabel="Confirm dispense"
              onConfirm={() => dispense.mutate()}
            />
          </div>
        </form>
        <MutationStatus
          error={review.error || dispense.error}
          success={
            review.isSuccess
              ? "Safety review confirmed. Dispensing remains a separate controlled action."
              : dispense.isSuccess
                ? "Dispensing confirmed by the server."
                : undefined
          }
          conflict="Dispensing was blocked by current safety, prescription or stock state. Nothing was completed optimistically."
        />
      </StaffSection>
      <StaffSection title="Medication stock">
        <QueryState
          loading={meds.isLoading}
          error={meds.error}
          empty={!meds.data?.length}
          onRetry={() => meds.refetch()}
        >
          <Table
            caption="Medication stock"
            headers={["Medication", "NDC", "On hand", "Unit cost"]}
          >
            {meds.data?.map((m) => (
              <tr key={m.id}>
                <td>{m.name}</td>
                <td>{m.ndcCode}</td>
                <td>{m.quantityOnHand}</td>
                <td>{formatMoney(m.unitCost)}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection title="Medication charges">
        <QueryState
          loading={charges.isLoading}
          error={charges.error}
          empty={!charges.data?.length}
          onRetry={() => charges.refetch()}
        >
          <Table
            caption="Medication charges"
            headers={["Patient", "Prescription", "Amount", "Status"]}
          >
            {charges.data?.map((c) => (
              <tr key={c.id}>
                <td>{c.patientId}</td>
                <td>{c.prescriptionId}</td>
                <td>{formatMoney(c.amount)}</td>
                <td>
                  <StatusBadge tone={statusTone(c.status)}>
                    {c.status}
                  </StatusBadge>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
