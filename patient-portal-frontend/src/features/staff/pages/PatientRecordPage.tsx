import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useLocation, useOutletContext } from "react-router-dom";
import { queryKeys } from "../../../api/queryKeys";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { DefinitionList } from "../../../components/data/DefinitionList";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import {
  Field,
  Select,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import {
  acknowledgeAlert,
  changePatientStatus,
  discontinuePrescription,
  getChart,
  signNote,
  updatePatient,
} from "../api";
import { MutationStatus, QueryState, StaffSection } from "../components";
import { Patient, PatientCommand } from "../types";

const fmt = (v?: string) =>
  v
    ? new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: v.includes("T") ? "short" : undefined,
      }).format(new Date(v))
    : "—";
const tabs = [
  ["summary", "Summary"],
  ["demographics", "Demographics"],
  ["encounters", "Encounters & history"],
  ["medications", "Diagnoses & medications"],
  ["labs", "Labs"],
  ["notes", "Notes"],
  ["vaccinations", "Vaccinations"],
] as const;
export function PatientRecordPage() {
  const { patient } = useOutletContext<{ patient: Patient }>();
  const { session } = useAuth();
  const client = useQueryClient();
  const slug = useLocation().pathname.split("/").pop() ?? "summary";
  const clinical =
    session!.user.role === "ADMIN" || session!.user.role === "CLINICIAN";
  const chart = useQuery({
    queryKey: queryKeys.patientResource(patient.id, "chart"),
    queryFn: ({ signal }) => getChart(patient.id, session!.token, signal),
    enabled: clinical,
  });
  const refresh = () =>
    client.invalidateQueries({
      queryKey: queryKeys.patientResource(patient.id, "chart"),
    });
  const [edit, setEdit] = useState(false);
  const [form, setForm] = useState<PatientCommand>({
    name: patient.name,
    email: patient.email,
    address: patient.address,
    dateOfBirth: patient.dateOfBirth,
    phone: patient.phone,
    gender: patient.gender,
    preferredLanguage: patient.preferredLanguage,
  });
  const demographic = useMutation({
    mutationFn: () => updatePatient(patient.id, form, session!.token),
    onSuccess: () => {
      setEdit(false);
      client.invalidateQueries({ queryKey: queryKeys.patient(patient.id) });
    },
  });
  const workflow = useMutation({
    mutationFn: async ({
      action,
      id,
    }: {
      action: "sign" | "discontinue" | "acknowledge" | "status";
      id: string;
    }) => {
      if (action === "sign") await signNote(id, session!.token);
      else if (action === "discontinue")
        await discontinuePrescription(id, session!.token);
      else if (action === "acknowledge")
        await acknowledgeAlert(id, session!.token);
      else await changePatientStatus(patient.id, id, session!.token);
    },
    onSuccess: (_, v) => {
      if (v.action === "status")
        client.invalidateQueries({ queryKey: queryKeys.patient(patient.id) });
      else refresh();
    },
    onError: refresh,
  });
  function save(e: FormEvent) {
    e.preventDefault();
    demographic.mutate();
  }
  const nav = (
    <nav className="record-tabs" aria-label="Patient record sections">
      {tabs
        .filter(([id]) => clinical || ["summary", "demographics"].includes(id))
        .map(([id, label]) => (
          <Link
            key={id}
            className={
              slug === id ? "record-tab record-tab--active" : "record-tab"
            }
            to={`/staff/patients/${patient.id}/${id}`}
          >
            {label}
          </Link>
        ))}
    </nav>
  );
  if (!clinical && !["summary", "demographics"].includes(slug))
    return (
      <Alert tone="danger" title="Clinical record access denied">
        Your role may confirm demographics but cannot read this clinical
        section.
      </Alert>
    );
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Patient record"
        title={tabs.find(([id]) => id === slug)?.[1] ?? "Summary"}
        description="Identity remains fixed in the banner while this patient record is open."
      />
      {nav}
      <MutationStatus
        error={workflow.error}
        success={
          workflow.isSuccess
            ? "The server confirmed the clinical workflow update."
            : undefined
        }
      />
      {slug === "summary" && !clinical ? (
        <StaffSection
          title="Registration summary"
          description="Clinical details are withheld from registration roles."
        >
          <DefinitionList
            items={[
              { term: "Legal name", description: patient.name },
              { term: "MRN", description: patient.mrn },
              { term: "Date of birth", description: patient.dateOfBirth },
              { term: "Lifecycle", description: patient.status },
            ]}
          />
          <p>
            <Link
              className="button button--secondary"
              to={`/staff/patients/${patient.id}/demographics`}
            >
              Review demographics
            </Link>
          </p>
        </StaffSection>
      ) : (
        <>
          {slug === "demographics" ? (
            <StaffSection
              title="Identity and contact"
              description="Confirm these fields before clinical or scheduling actions."
            >
              {edit ? (
                <form onSubmit={save}>
                  <div className="form-grid">
                    <Field id="edit-name" label="Legal name" required>
                      <TextField
                        required
                        value={form.name}
                        onChange={(e) =>
                          setForm({ ...form, name: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="edit-dob" label="Date of birth" required>
                      <TextField
                        type="date"
                        required
                        value={form.dateOfBirth}
                        onChange={(e) =>
                          setForm({ ...form, dateOfBirth: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="edit-email" label="Email" required>
                      <TextField
                        type="email"
                        required
                        value={form.email}
                        onChange={(e) =>
                          setForm({ ...form, email: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="edit-phone" label="Phone">
                      <TextField
                        value={form.phone}
                        onChange={(e) =>
                          setForm({ ...form, phone: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="edit-address" label="Address" required>
                      <TextField
                        required
                        value={form.address}
                        onChange={(e) =>
                          setForm({ ...form, address: e.target.value })
                        }
                      />
                    </Field>
                    <Field id="edit-language" label="Preferred language">
                      <TextField
                        value={form.preferredLanguage}
                        onChange={(e) =>
                          setForm({
                            ...form,
                            preferredLanguage: e.target.value,
                          })
                        }
                      />
                    </Field>
                  </div>
                  <div className="task-actions">
                    <Button type="submit" disabled={demographic.isPending}>
                      Save demographics
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      onClick={() => setEdit(false)}
                    >
                      Cancel
                    </Button>
                  </div>
                </form>
              ) : (
                <>
                  <DefinitionList
                    items={[
                      ["Legal name", patient.name],
                      ["MRN", patient.mrn],
                      ["Date of birth", patient.dateOfBirth],
                      ["Gender", patient.gender || "Not recorded"],
                      ["Email", patient.email],
                      ["Phone", patient.phone || "Not recorded"],
                      ["Address", patient.address],
                      [
                        "Preferred language",
                        patient.preferredLanguage || "Not recorded",
                      ],
                    ].map(([term, description]) => ({ term, description }))}
                  />
                  <div className="task-actions">
                    <Button onClick={() => setEdit(true)}>
                      Edit demographics
                    </Button>
                    <Field id="lifecycle-select" label="Change lifecycle">
                      <Select defaultValue="">
                        <option value="" disabled>
                          Select status
                        </option>
                        {["ACTIVE", "INACTIVE", "DECEASED"]
                          .filter((s) => s !== patient.status)
                          .map((s) => (
                            <option key={s}>{s}</option>
                          ))}
                      </Select>
                    </Field>
                    <ConfirmAction
                      triggerLabel="Apply selected lifecycle"
                      title="Change patient lifecycle"
                      description={`Confirm a lifecycle change for ${patient.name} (${patient.mrn}). Select the status immediately before confirming.`}
                      confirmLabel="Change lifecycle"
                      onConfirm={() => {
                        const select = document.getElementById(
                          "lifecycle-select",
                        ) as HTMLSelectElement;
                        if (select.value)
                          workflow.mutate({
                            action: "status",
                            id: select.value,
                          });
                      }}
                    />
                  </div>
                </>
              )}
              <MutationStatus
                error={demographic.error}
                success={
                  demographic.isSuccess ? "Demographics saved." : undefined
                }
              />
            </StaffSection>
          ) : (
            <QueryState
              loading={chart.isLoading}
              error={chart.error}
              onRetry={() => chart.refetch()}
            >
              {chart.data && (
                <ChartSection
                  slug={slug}
                  patient={patient}
                  data={chart.data}
                  act={(action, id) => workflow.mutate({ action, id })}
                />
              )}
            </QueryState>
          )}
        </>
      )}
    </div>
  );
}

function ChartSection({
  slug,
  patient,
  data,
  act,
}: {
  slug: string;
  patient: Patient;
  data: Awaited<ReturnType<typeof getChart>>;
  act: (a: "sign" | "discontinue" | "acknowledge", id: string) => void;
}) {
  if (slug === "summary")
    return (
      <>
        <div className="clinical-summary-grid">
          <div className="metric">
            <span>Open alerts</span>
            <strong>
              {data.alerts.filter((a) => a.status !== "ACKNOWLEDGED").length}
            </strong>
          </div>
          <div className="metric">
            <span>Active medications</span>
            <strong>
              {data.prescriptions.filter((p) => p.status === "ACTIVE").length}
            </strong>
          </div>
          <div className="metric">
            <span>Recent encounters</span>
            <strong>{data.encounters.length}</strong>
          </div>
        </div>
        {data.alerts.map((a) => (
          <Alert key={a.id} tone="warning" title="Clinical safety alert">
            {a.summary}{" "}
            <ConfirmAction
              triggerLabel="Acknowledge"
              title="Acknowledge clinical alert"
              description={`Confirm review of this alert for ${patient.name}. Staff task completion remains a separate action.`}
              confirmLabel="Acknowledge alert"
              onConfirm={() => act("acknowledge", a.id)}
            />
          </Alert>
        ))}
        <StaffSection title="Latest history">
          {data.histories[0] ? (
            <>
              <p>{data.histories[0].summary}</p>
              <p className="danger-text">
                <strong>Allergies:</strong>{" "}
                {data.histories[0].allergies.join(", ") || "None recorded"}
              </p>
            </>
          ) : (
            <p>No history recorded.</p>
          )}
        </StaffSection>
      </>
    );
  if (slug === "encounters")
    return (
      <>
        <StaffSection title="Encounters">
          <Table
            caption="Patient encounters"
            headers={["Started", "Reason", "Clinician", "Status"]}
          >
            {data.encounters.map((x) => (
              <tr key={x.id}>
                <td>{fmt(x.startedAt)}</td>
                <td>{x.reason}</td>
                <td>{x.clinicianId}</td>
                <td>
                  <StatusBadge>{x.status}</StatusBadge>
                </td>
              </tr>
            ))}
          </Table>
        </StaffSection>
        <StaffSection title="History and allergies">
          {data.histories.map((x) => (
            <article key={x.id} className="record-entry">
              <h3>{x.summary}</h3>
              <p>
                <strong>Allergies:</strong>{" "}
                {x.allergies.join(", ") || "None recorded"}
              </p>
              <p>
                <strong>Chronic conditions:</strong>{" "}
                {x.chronicConditions.join(", ") || "None recorded"}
              </p>
            </article>
          ))}
        </StaffSection>
      </>
    );
  if (slug === "medications")
    return (
      <>
        <StaffSection title="Diagnoses">
          <Table caption="Diagnoses" headers={["Date", "Code", "Description"]}>
            {data.diagnoses.map((x) => (
              <tr key={x.id}>
                <td>{fmt(x.diagnosedOn)}</td>
                <td>{x.code}</td>
                <td>{x.description}</td>
              </tr>
            ))}
          </Table>
        </StaffSection>
        <StaffSection title="Prescriptions">
          <Table
            caption="Prescriptions"
            headers={["Medication", "Dose", "Instructions", "Status", "Action"]}
          >
            {data.prescriptions.map((x) => (
              <tr key={x.id}>
                <td>{x.medication}</td>
                <td>{x.dosage}</td>
                <td>{x.instructions}</td>
                <td>
                  <StatusBadge>{x.status}</StatusBadge>
                </td>
                <td>
                  {x.status === "ACTIVE" && (
                    <ConfirmAction
                      triggerLabel="Discontinue"
                      title="Discontinue prescription"
                      description={`Stop ${x.medication} for ${patient.name}. This does not dispense or replace medication.`}
                      confirmLabel="Discontinue"
                      danger
                      onConfirm={() => act("discontinue", x.id)}
                    />
                  )}
                </td>
              </tr>
            ))}
          </Table>
        </StaffSection>
      </>
    );
  if (slug === "labs")
    return (
      <StaffSection title="Laboratory results">
        <Table
          caption="Laboratory results"
          headers={["Collected", "Test", "Result", "Source"]}
        >
          {data.labs.map((x) => (
            <tr key={x.id}>
              <td>{fmt(x.collectedOn)}</td>
              <td>{x.testName}</td>
              <td>{x.resultSummary}</td>
              <td>{x.source}</td>
            </tr>
          ))}
        </Table>
      </StaffSection>
    );
  if (slug === "notes")
    return (
      <StaffSection title="Clinical notes">
        <Table
          caption="Clinical notes"
          headers={["Created", "Note", "Status", "Action"]}
        >
          {data.notes.map((x) => (
            <tr key={x.id}>
              <td>{fmt(x.createdAt)}</td>
              <td className="clinical-text">{x.body}</td>
              <td>
                <StatusBadge
                  tone={x.status === "SIGNED" ? "success" : "warning"}
                >
                  {x.status}
                  {x.status === "SIGNED" ? " · locked" : ""}
                </StatusBadge>
              </td>
              <td>
                {x.status !== "SIGNED" && (
                  <ConfirmAction
                    triggerLabel="Sign note"
                    title="Sign and lock clinical note"
                    description={`Signing this note for ${patient.name} makes it immutable.`}
                    confirmLabel="Sign note"
                    onConfirm={() => act("sign", x.id)}
                  />
                )}
              </td>
            </tr>
          ))}
        </Table>
      </StaffSection>
    );
  return (
    <StaffSection title="Vaccinations">
      <Table caption="Vaccination records" headers={["Date", "Vaccine", "Lot"]}>
        {data.vaccinations.map((x) => (
          <tr key={x.id}>
            <td>{fmt(x.administeredOn)}</td>
            <td>{x.vaccine}</td>
            <td>{x.lotNumber}</td>
          </tr>
        ))}
      </Table>
    </StaffSection>
  );
}
