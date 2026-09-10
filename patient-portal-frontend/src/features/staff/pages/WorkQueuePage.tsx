import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useSearchParams } from "react-router-dom";
import { queryKeys } from "../../../api/queryKeys";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { Alert } from "../../../components/feedback/Alert";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import {
  Field,
  Select,
  TextArea,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import {
  claimTask,
  commentTask,
  completeTask,
  getQueue,
  getTaskHistory,
  handoffTask,
} from "../api";
import { MutationStatus, QueryState, StaffSection } from "../components";
import { StaffTask } from "../types";

const queueRoles = [
  "RECEPTIONIST",
  "NURSE",
  "CLINICIAN",
  "BILLING_STAFF",
  "PHARMACIST",
  "LAB_STAFF",
  "ADMIN",
];
const fmt = (v: string) =>
  v
    ? new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short",
      }).format(new Date(v))
    : "—";
export function WorkQueuePage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [params, setParams] = useSearchParams();
  const defaultRole =
    session!.user.role === "ADMIN" ? "CLINICIAN" : session!.user.role;
  const role = params.get("role") ?? defaultRole;
  const priority = params.get("priority") ?? "ALL";
  const limit = Math.min(200, Math.max(1, Number(params.get("limit")) || 50));
  const [selected, setSelected] = useState<StaffTask>();
  const [mode, setMode] = useState<"history" | "comment" | "handoff">(
    "history",
  );
  const [comment, setComment] = useState("");
  const [dueAt, setDueAt] = useState("");
  const [reason, setReason] = useState("");
  const queue = useQuery({
    queryKey: queryKeys.staffQueue(role, { priority, limit: String(limit) }),
    queryFn: ({ signal }) => getQueue(role, limit, session!.token, signal),
  });
  const currentTask =
    queue.data?.find((task) => task.id === selected?.id) ?? selected;
  const canManage = Boolean(
    currentTask &&
    (session!.user.role === "ADMIN" ||
      currentTask.assigneeId === session!.user.subject),
  );
  const history = useQuery({
    queryKey: ["staff-task", selected?.id, "history"],
    queryFn: ({ signal }) =>
      getTaskHistory(selected!.id, session!.token, signal),
    enabled: Boolean(selected && canManage),
  });
  const refresh = () => client.invalidateQueries({ queryKey: ["staff-queue"] });
  const mutation = useMutation({
    mutationFn: async (
      action: "claim" | "complete" | "comment" | "handoff",
    ) => {
      if (!selected) return;
      if (action === "claim") return claimTask(selected.id, session!.token);
      if (action === "complete")
        return completeTask(selected.id, session!.token);
      if (action === "comment")
        return commentTask(selected.id, comment, session!.token);
      return handoffTask(
        selected.id,
        {
          assigneeId: session!.user.subject,
          queueRole: role,
          dueAt: new Date(dueAt).toISOString(),
          reason,
        },
        session!.token,
      );
    },
    onSuccess: () => {
      setComment("");
      setReason("");
      refresh();
      client.invalidateQueries({
        queryKey: ["staff-task", selected?.id, "history"],
      });
    },
    onError: refresh,
  });
  const rows =
    queue.data?.filter((t) => priority === "ALL" || t.priority === priority) ??
    [];
  function act(
    action: "claim" | "complete" | "comment" | "handoff",
    e?: FormEvent,
  ) {
    e?.preventDefault();
    mutation.mutate(action);
  }
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Staff operations"
        title="Work queue"
        description="Claim, document, hand off and complete the work assigned to your permitted queue."
      />
      <StaffSection title="Queue filters">
        <div className="toolbar">
          <Field id="queue-role" label="Queue">
            <Select
              value={role}
              disabled={session!.user.role !== "ADMIN"}
              onChange={(e) =>
                setParams({
                  role: e.target.value,
                  priority,
                  limit: String(limit),
                })
              }
            >
              {queueRoles.map((r) => (
                <option key={r}>{r}</option>
              ))}
            </Select>
          </Field>
          <Field id="queue-priority" label="Priority">
            <Select
              value={priority}
              onChange={(e) =>
                setParams({
                  role,
                  priority: e.target.value,
                  limit: String(limit),
                })
              }
            >
              <option>ALL</option>
              <option>URGENT</option>
              <option>HIGH</option>
              <option>ROUTINE</option>
            </Select>
          </Field>
          <Field id="queue-limit" label="Maximum rows">
            <TextField
              type="number"
              min="1"
              max="200"
              value={limit}
              onChange={(e) =>
                setParams({ role, priority, limit: e.target.value })
              }
            />
          </Field>
        </div>
        <QueryState
          loading={queue.isLoading}
          error={queue.error}
          empty={!rows.length}
          onRetry={() => queue.refetch()}
        >
          <Table
            caption={`${role} work queue`}
            headers={["Priority", "Task", "Patient", "Due", "Status", "Action"]}
          >
            {rows.map((task) => (
              <tr
                key={task.id}
                className={
                  new Date(task.dueAt) < new Date() ? "row-overdue" : ""
                }
              >
                <td>
                  <StatusBadge
                    tone={
                      task.priority === "URGENT"
                        ? "danger"
                        : task.priority === "HIGH"
                          ? "warning"
                          : "neutral"
                    }
                  >
                    {task.priority}
                  </StatusBadge>
                </td>
                <td>{task.title}</td>
                <td>
                  <Link to={`/staff/patients/${task.patientId}/summary`}>
                    Open patient
                  </Link>
                </td>
                <td>
                  {fmt(task.dueAt)}
                  {new Date(task.dueAt) < new Date() && (
                    <span className="danger-text"> · Overdue</span>
                  )}
                </td>
                <td>{task.status}</td>
                <td>
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setSelected(task);
                      setMode("history");
                    }}
                  >
                    Review
                  </Button>
                </td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
      {selected && (
        <StaffSection
          title={`Task: ${selected.title}`}
          description="Ownership changes are confirmed by the server; conflicts trigger an authoritative refresh."
        >
          <div className="task-actions">
            {!canManage && (
              <Button
                onClick={() => act("claim")}
                disabled={mutation.isPending}
              >
                Claim
              </Button>
            )}
            {canManage && (
              <>
                <Button variant="secondary" onClick={() => setMode("comment")}>
                  Add comment
                </Button>
                <Button variant="secondary" onClick={() => setMode("handoff")}>
                  Hand off
                </Button>
                <Button
                  variant="secondary"
                  onClick={() => act("complete")}
                  disabled={mutation.isPending}
                >
                  Complete
                </Button>
              </>
            )}
          </div>
          <MutationStatus
            error={mutation.error}
            success={
              mutation.isSuccess
                ? "The server confirmed the task update."
                : undefined
            }
          />
          {mode === "comment" && (
            <form onSubmit={(e) => act("comment", e)}>
              <Field
                id="task-comment"
                label="Comment"
                required
                help="Keep the task history limited to information needed for this workflow."
              >
                <TextArea
                  required
                  maxLength={2000}
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                />
              </Field>
              <Button type="submit" disabled={!comment || mutation.isPending}>
                Save comment
              </Button>
            </form>
          )}
          {mode === "handoff" && (
            <form onSubmit={(e) => act("handoff", e)}>
              <Alert
                tone="info"
                title="Reassign to your authenticated identity"
              >
                A staff directory is not available, so this safe handoff changes
                the due cycle while assigning the task to you. Arbitrary staff
                identifiers are not accepted in the browser.
              </Alert>
              <Field id="handoff-due" label="New due time" required>
                <TextField
                  required
                  type="datetime-local"
                  value={dueAt}
                  onChange={(e) => setDueAt(e.target.value)}
                />
              </Field>
              <Field id="handoff-reason" label="Reason" required>
                <TextArea
                  required
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                />
              </Field>
              <Button type="submit" disabled={mutation.isPending}>
                Confirm handoff to me
              </Button>
            </form>
          )}
          {mode === "history" && (
            <QueryState
              loading={history.isLoading}
              error={history.error}
              empty={!history.data?.length}
              onRetry={() => history.refetch()}
            >
              <Table
                caption="Task history"
                headers={["When", "Action", "Actor", "Comment"]}
              >
                {history.data?.map((h) => (
                  <tr key={h.id}>
                    <td>{fmt(h.occurredAt)}</td>
                    <td>{h.action}</td>
                    <td>{h.actorId}</td>
                    <td>{h.comment || "—"}</td>
                  </tr>
                ))}
              </Table>
            </QueryState>
          )}
        </StaffSection>
      )}
    </div>
  );
}
