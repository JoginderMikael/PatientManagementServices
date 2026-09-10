import { FormEvent, useMemo, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { createIdempotencyKey } from "../../../api/client";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { Table } from "../../../components/data/Table";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Alert } from "../../../components/feedback/Alert";
import { Field, TextField } from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { createPatient, findDuplicates, listPatients } from "../api";
import { MutationStatus, QueryState, StaffSection } from "../components";
import { PatientCommand } from "../types";

const blank: PatientCommand = {
  name: "",
  email: "",
  address: "",
  dateOfBirth: "",
  phone: "",
  preferredLanguage: "",
};
export function PatientsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [params, setParams] = useSearchParams();
  const status = params.get("status") ?? "ALL";
  const attempt = useRef<{ signature: string; key: string } | null>(null);
  const [term, setTerm] = useState("");
  const [draft, setDraft] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState(blank);
  const [duplicateTerm, setDuplicateTerm] = useState("");
  const patients = useQuery({
    queryKey: ["staff-patients"],
    queryFn: ({ signal }) => listPatients(session!.token, signal),
  });
  const duplicates = useQuery({
    queryKey: ["patient-duplicates", duplicateTerm],
    queryFn: ({ signal }) =>
      findDuplicates(duplicateTerm, session!.token, signal),
    enabled: duplicateTerm.length >= 2,
  });
  const create = useMutation({
    mutationFn: (body: PatientCommand) => {
      const command = {
        ...body,
        registeredDate: new Date().toISOString().slice(0, 10),
      };
      const signature = JSON.stringify(command);
      if (!attempt.current || attempt.current.signature !== signature)
        attempt.current = { signature, key: createIdempotencyKey() };
      return createPatient(command, session!.token, attempt.current.key);
    },
    onSuccess: () => {
      attempt.current = null;
      setForm(blank);
      setShowCreate(false);
      client.invalidateQueries({ queryKey: ["staff-patients"] });
    },
  });
  const filtered = useMemo(
    () =>
      patients.data?.filter(
        (p) =>
          (status === "ALL" || p.status === status) &&
          (!term ||
            [p.name, p.mrn, p.phone, p.email].some((v) =>
              v?.toLowerCase().includes(term.toLowerCase()),
            )),
      ) ?? [],
    [patients.data, status, term],
  );
  function submit(e: FormEvent) {
    e.preventDefault();
    setDuplicateTerm(form.name);
  }
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Staff operations"
        title="Patient registry"
        description="Search and establish explicit patient context before opening clinical records."
        actions={
          <Button onClick={() => setShowCreate((v) => !v)}>
            {showCreate ? "Cancel registration" : "Register patient"}
          </Button>
        }
      />
      <Alert tone="info" title="Patient context is deliberate">
        Opening a record replaces the previous context and clears its cached
        clinical data.
      </Alert>
      {showCreate && (
        <StaffSection
          title="Register patient"
          description="Duplicate review is required before the final create action."
        >
          <form onSubmit={submit}>
            <div className="form-grid">
              <Field id="patient-name" label="Legal name" required>
                <TextField
                  required
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                />
              </Field>
              <Field id="patient-dob" label="Date of birth" required>
                <TextField
                  type="date"
                  required
                  value={form.dateOfBirth}
                  onChange={(e) =>
                    setForm({ ...form, dateOfBirth: e.target.value })
                  }
                />
              </Field>
              <Field id="patient-email" label="Email" required>
                <TextField
                  type="email"
                  required
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                />
              </Field>
              <Field id="patient-phone" label="Phone">
                <TextField
                  value={form.phone}
                  onChange={(e) => setForm({ ...form, phone: e.target.value })}
                />
              </Field>
              <Field id="patient-address" label="Address" required>
                <TextField
                  required
                  value={form.address}
                  onChange={(e) =>
                    setForm({ ...form, address: e.target.value })
                  }
                />
              </Field>
              <Field id="patient-language" label="Preferred language">
                <TextField
                  value={form.preferredLanguage}
                  onChange={(e) =>
                    setForm({ ...form, preferredLanguage: e.target.value })
                  }
                />
              </Field>
            </div>
            <Button type="submit">Check for duplicates</Button>
          </form>
          {duplicates.data && (
            <div className="registration-review">
              <Alert
                tone={duplicates.data.length ? "warning" : "success"}
                title={
                  duplicates.data.length
                    ? "Possible duplicate records"
                    : "No name matches found"
                }
              >
                {duplicates.data.length
                  ? "Review the matching records below before creating a new identity."
                  : "The server found no name matches. Confirm the details before registration."}
              </Alert>
              {duplicates.data.length > 0 && (
                <Table
                  caption="Potential duplicate patients"
                  headers={["Patient", "MRN", "Date of birth"]}
                >
                  {duplicates.data.map((p) => (
                    <tr key={p.id}>
                      <td>{p.name}</td>
                      <td>{p.mrn}</td>
                      <td>{p.dateOfBirth}</td>
                    </tr>
                  ))}
                </Table>
              )}
              <Button
                type="button"
                onClick={() => create.mutate(form)}
                disabled={create.isPending}
              >
                {create.isPending ? "Registering…" : "Confirm and register"}
              </Button>
            </div>
          )}
          <MutationStatus
            error={create.error}
            success={
              create.isSuccess
                ? "Patient registered and available in the registry."
                : undefined
            }
            conflict="This registration reference or identity now conflicts with current data. Search again before continuing."
          />
        </StaffSection>
      )}
      <StaffSection title="Find a patient">
        <form
          className="toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setTerm(draft);
          }}
        >
          <Field id="patient-search" label="Name, MRN, phone or email">
            <TextField
              type="search"
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
            />
          </Field>
          <Field id="patient-status" label="Lifecycle">
            <select
              className="select-input"
              value={status}
              onChange={(e) =>
                setParams(e.target.value !== "ALL" ? { status: e.target.value } : {})
              }
            >
              <option value="ALL">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="MERGED">Merged</option>
            </select>
          </Field>
          <Button type="submit">Search</Button>
        </form>
        <QueryState
          loading={patients.isLoading}
          error={patients.error}
          empty={!filtered.length}
          onRetry={() => patients.refetch()}
        >
          <Table
            caption="Patient search results"
            headers={[
              "Patient",
              "MRN",
              "Date of birth",
              "Phone",
              "Lifecycle",
              "Action",
            ]}
          >
            {filtered.map((p) => (
              <tr key={p.id}>
                <td>{p.name}</td>
                <td>{p.mrn}</td>
                <td>{p.dateOfBirth}</td>
                <td>{p.phone || "—"}</td>
                <td>
                  <StatusBadge>{p.status}</StatusBadge>
                </td>
                <td>
                  <Link
                    className="button button--secondary button--small"
                    to={`/staff/patients/${p.id}/summary`}
                  >
                    Open record
                  </Link>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
