import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import {
  Field,
  Select,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import {
  createComplianceCase,
  listComplianceCases,
  listPrivacyReviews,
  reviewEmergencyGrant,
  setComplianceHold,
  transitionComplianceCase,
} from "../api";
import {
  formatDate,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";
import { ComplianceCase } from "../types";
const nextStatus = (c: ComplianceCase) =>
  c.kind === "INCIDENT"
    ? (
        {
          OPEN: "INVESTIGATING",
          INVESTIGATING: "CONTAINED",
          CONTAINED: "ASSESSED",
          ASSESSED: "NO_NOTIFICATION_REQUIRED",
          NO_NOTIFICATION_REQUIRED: "REMEDIATING",
          NOTIFICATION_REQUIRED: "NOTIFIED",
          NOTIFIED: "REMEDIATING",
          REMEDIATING: "CLOSED",
        } as Record<string, string>
      )[c.status]
    : (
        {
          OPEN: "INVESTIGATING",
          INVESTIGATING: "REMEDIATING",
          REMEDIATING: "CLOSED",
        } as Record<string, string>
      )[c.status];
export function ComplianceOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [overdue, setOverdue] = useState(false);
  const [create, setCreate] = useState({
    kind: "ACCESS_REVIEW",
    owner: "",
    evidenceReference: "",
    dueAt: "",
  });
  const [evidence, setEvidence] = useState("");
  const cases = useQuery({
    queryKey: ["operations", "compliance", "cases", overdue],
    queryFn: ({ signal }) =>
      listComplianceCases(overdue, session!.token, signal),
  });
  const reviews = useQuery({
    queryKey: ["operations", "compliance", "reviews"],
    queryFn: ({ signal }) => listPrivacyReviews(session!.token, signal),
  });
  const refresh = () =>
    client.invalidateQueries({ queryKey: ["operations", "compliance"] });
  const mutation = useMutation({
    mutationFn: async (action: {
      kind: "create" | "transition" | "hold" | "review";
      record?: ComplianceCase;
      id?: string;
    }) => {
      if (action.kind === "create")
        await createComplianceCase(
          { ...create, dueAt: new Date(create.dueAt).toISOString() },
          session!.token,
        );
      else if (action.kind === "review")
        await reviewEmergencyGrant(action.id!, evidence, session!.token);
      else if (action.kind === "hold")
        await setComplianceHold(
          action.record!.id,
          !action.record!.legalHold,
          evidence,
          session!.token,
        );
      else
        await transitionComplianceCase(
          action.record!.id,
          {
            expectedStatus: action.record!.status,
            status: nextStatus(action.record!)!,
            evidenceReference: evidence,
          },
          session!.token,
        );
    },
    onSuccess: () => {
      setEvidence("");
      refresh();
    },
    onError: refresh,
  });
  const submit = (e: FormEvent) => {
    e.preventDefault();
    mutation.mutate({ kind: "create" });
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Privacy and compliance"
        description="Manage evidence-referenced cases, legal holds and independent emergency-access review."
      />
      <MutationStatus
        error={mutation.error}
        success={
          mutation.isSuccess
            ? "The compliance service confirmed the change."
            : undefined
        }
        conflict="The case or grant changed, is held, or requires an independent reviewer. Current state has been refreshed."
      />
      <StaffSection title="Open compliance case">
        <form className="form-grid" onSubmit={submit}>
          <Field id="case-kind" label="Case kind" required>
            <Select
              value={create.kind}
              onChange={(e) => setCreate({ ...create, kind: e.target.value })}
            >
              <option>INCIDENT</option>
              <option>ACCESS_REVIEW</option>
              <option>RESTORE_DRILL</option>
              <option>RETENTION_REVIEW</option>
              <option>DOCUMENT_REVIEW</option>
            </Select>
          </Field>
          <Field id="case-owner" label="Accountable owner" required>
            <TextField
              required
              maxLength={200}
              value={create.owner}
              onChange={(e) => setCreate({ ...create, owner: e.target.value })}
            />
          </Field>
          <Field id="case-evidence" label="Evidence reference" required>
            <TextField
              required
              maxLength={200}
              value={create.evidenceReference}
              onChange={(e) =>
                setCreate({ ...create, evidenceReference: e.target.value })
              }
            />
          </Field>
          <Field id="case-due" label="Due time" required>
            <TextField
              required
              type="datetime-local"
              value={create.dueAt}
              onChange={(e) => setCreate({ ...create, dueAt: e.target.value })}
            />
          </Field>
          <div>
            <Button type="submit" disabled={mutation.isPending}>
              Create case
            </Button>
          </div>
        </form>
      </StaffSection>
      <StaffSection title="Case management">
        <div className="toolbar">
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={overdue}
              onChange={(e) => setOverdue(e.target.checked)}
            />
            <span>Show overdue open cases only</span>
          </label>
          <Field
            id="case-action-evidence"
            label="Evidence reference for next action"
            required
          >
            <TextField
              required
              maxLength={200}
              value={evidence}
              onChange={(e) => setEvidence(e.target.value)}
            />
          </Field>
        </div>
        <QueryState
          loading={cases.isLoading}
          error={cases.error}
          empty={!cases.data?.length}
          onRetry={() => cases.refetch()}
        >
          <Table
            caption="Compliance cases"
            headers={[
              "Kind",
              "Owner",
              "Due",
              "Status",
              "Hold",
              "Controlled actions",
            ]}
          >
            {cases.data?.map((c) => {
              const next = nextStatus(c);
              return (
                <tr
                  key={c.id}
                  className={
                    new Date(c.dueAt) < new Date() && c.status !== "CLOSED"
                      ? "row-overdue"
                      : ""
                  }
                >
                  <td>{c.kind}</td>
                  <td>{c.owner}</td>
                  <td>{formatDate(c.dueAt)}</td>
                  <td>
                    <StatusBadge tone={statusTone(c.status)}>
                      {c.status}
                    </StatusBadge>
                  </td>
                  <td>{c.legalHold ? "ENABLED" : "—"}</td>
                  <td>
                    <div className="task-actions">
                      {next && (
                        <ConfirmAction
                          triggerLabel={`Move to ${next}`}
                          title="Transition compliance case"
                          description={`Move ${c.kind} case ${c.id} from ${c.status} to ${next}. Evidence is required and stale state is rejected.`}
                          confirmLabel="Confirm transition"
                          disabled={!evidence || mutation.isPending}
                          onConfirm={() =>
                            mutation.mutate({ kind: "transition", record: c })
                          }
                        />
                      )}
                      <ConfirmAction
                        triggerLabel={
                          c.legalHold ? "Release hold" : "Enable hold"
                        }
                        title={
                          c.legalHold
                            ? "Release legal hold"
                            : "Enable legal hold"
                        }
                        description="This records case-level hold state and evidence. It does not automatically freeze external storage."
                        confirmLabel={
                          c.legalHold ? "Release hold" : "Enable hold"
                        }
                        danger={c.legalHold}
                        disabled={!evidence || mutation.isPending}
                        onConfirm={() =>
                          mutation.mutate({ kind: "hold", record: c })
                        }
                      />
                    </div>
                  </td>
                </tr>
              );
            })}
          </Table>
        </QueryState>
      </StaffSection>
      <StaffSection
        title="Emergency-access reviews"
        description="Review must be independent and revokes the emergency grant."
      >
        <QueryState
          loading={reviews.isLoading}
          error={reviews.error}
          empty={!reviews.data?.length}
          onRetry={() => reviews.refetch()}
        >
          <Table
            caption="Pending break-glass reviews"
            headers={["Patient", "Subject", "Expires", "Evidence", "Action"]}
          >
            {reviews.data?.map((r) => (
              <tr key={r.id}>
                <td>{r.patientId}</td>
                <td>{r.subject}</td>
                <td>{formatDate(r.expiresAt)}</td>
                <td>{r.evidence}</td>
                <td>
                  <ConfirmAction
                    triggerLabel="Review and revoke"
                    title="Complete emergency-access review"
                    description={`Review grant ${r.id} independently and revoke it. Supply the review evidence reference above.`}
                    confirmLabel="Complete review"
                    danger
                    disabled={!evidence || mutation.isPending}
                    onConfirm={() =>
                      mutation.mutate({ kind: "review", id: r.id })
                    }
                  />
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
