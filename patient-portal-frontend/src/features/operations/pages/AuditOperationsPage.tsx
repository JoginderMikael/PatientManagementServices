import { FormEvent, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import { Button } from "../../../components/Button";
import { Field, TextField } from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { listAuditEvents } from "../api";
import {
  formatDate,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";
export function AuditOperationsPage() {
  const { session } = useAuth();
  const [filters, setFilters] = useState({ patientId: "", actorId: "" });
  const [draft, setDraft] = useState(filters);
  const events = useQuery({
    queryKey: ["operations", "audit", filters.patientId, filters.actorId],
    queryFn: ({ signal }) =>
      listAuditEvents(filters.patientId, filters.actorId, session!.token, signal),
  });
  const submit = (e: FormEvent) => {
    e.preventDefault();
    setFilters(draft);
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Audit evidence"
        description="Read append-only access and mutation evidence. Sensitive filters remain only in memory for this view."
      />
      <Alert tone="info" title="Evidence is read-only">
        This screen intentionally has no create, edit, delete or export action.
        Hashes are displayed only as abbreviated verification references.
      </Alert>
      <StaffSection title="Evidence search">
        <form className="toolbar" onSubmit={submit}>
          <Field id="audit-patient" label="Patient UUID">
            <TextField
              value={draft.patientId}
              onChange={(e) =>
                setDraft({ ...draft, patientId: e.target.value })
              }
            />
          </Field>
          <Field id="audit-actor" label="Actor UUID">
            <TextField
              value={draft.actorId}
              onChange={(e) => setDraft({ ...draft, actorId: e.target.value })}
            />
          </Field>
          <Button type="submit">Search evidence</Button>
        </form>
        <QueryState
          loading={events.isLoading}
          error={events.error}
          empty={!events.data?.length}
          onRetry={() => events.refetch()}
        >
          <Table
            caption="Append-only audit evidence"
            headers={[
              "Occurred",
              "Actor / role",
              "Action",
              "Resource",
              "Outcome",
              "Source",
              "Hash",
            ]}
          >
            {events.data?.map((e) => (
              <tr key={e.id}>
                <td>{formatDate(e.occurredAt)}</td>
                <td>
                  {e.actorId}
                  <span className="request-id-inline">{e.actorRole}</span>
                </td>
                <td>{e.action}</td>
                <td>
                  {e.resourceType}
                  <span className="request-id-inline">
                    {e.resourceId ?? "—"}
                  </span>
                </td>
                <td>
                  <StatusBadge tone={statusTone(e.outcome)}>
                    {e.outcome}
                  </StatusBadge>
                </td>
                <td>{e.sourceService}</td>
                <td>
                  <code>{e.eventHash?.slice(0, 12)}…</code>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
