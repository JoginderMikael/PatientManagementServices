import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "../../../auth/AuthProvider";
import { Button } from "../../../components/Button";
import { StatusBadge } from "../../../components/data/StatusBadge";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import {
  Field,
  Select,
  TextArea,
  TextField,
} from "../../../components/forms/FormControls";
import { PageHeader } from "../../../components/PageHeader";
import { listNotifications, sendNotification } from "../api";
import {
  formatDate,
  MutationStatus,
  QueryState,
  StaffSection,
  statusTone,
} from "../components";
export function NotificationsOperationsPage() {
  const { session } = useAuth();
  const client = useQueryClient();
  const [recipient, setRecipient] = useState("");
  const [filter, setFilter] = useState("");
  const [form, setForm] = useState({
    recipientId: "",
    channel: "EMAIL",
    destination: "",
    template: "OPERATIONAL",
    body: "",
  });
  const messages = useQuery({
    queryKey: ["operations", "notifications", recipient],
    queryFn: ({ signal }) =>
      listNotifications(recipient, session!.token, signal),
  });
  const send = useMutation({
    mutationFn: () => sendNotification(form, session!.token),
    onSuccess: () => {
      setForm({ ...form, destination: "", body: "" });
      client.invalidateQueries({ queryKey: ["operations", "notifications"] });
    },
  });
  const submit = (e: FormEvent) => {
    e.preventDefault();
    send.mutate();
  };
  return (
    <div className="portal-stack">
      <PageHeader
        eyebrow="Specialized operations"
        title="Notifications"
        description="Queue permitted operational messages and review delivery state without treating acceptance as delivery."
      />
      <Alert tone="warning" title="Minimum necessary content">
        Do not place passwords, MFA codes, diagnoses or unnecessary clinical
        details in general notification bodies. A QUEUED response only confirms
        acceptance.
      </Alert>
      <StaffSection title="Queue operational message">
        <form onSubmit={submit}>
          <div className="form-grid">
            <Field id="notify-recipient" label="Recipient UUID" required>
              <TextField
                required
                value={form.recipientId}
                onChange={(e) =>
                  setForm({ ...form, recipientId: e.target.value })
                }
              />
            </Field>
            <Field id="notify-channel" label="Channel" required>
              <Select
                value={form.channel}
                onChange={(e) => setForm({ ...form, channel: e.target.value })}
              >
                <option>EMAIL</option>
                <option>SMS</option>
                <option>PUSH</option>
              </Select>
            </Field>
            <Field id="notify-destination" label="Destination" required>
              <TextField
                required
                value={form.destination}
                onChange={(e) =>
                  setForm({ ...form, destination: e.target.value })
                }
              />
            </Field>
            <Field id="notify-template" label="Approved template" required>
              <TextField
                required
                value={form.template}
                onChange={(e) => setForm({ ...form, template: e.target.value })}
              />
            </Field>
          </div>
          <Field
            id="notify-body"
            label="Message"
            required
            help="Use only the minimum information needed for this operational purpose."
          >
            <TextArea
              required
              maxLength={2000}
              value={form.body}
              onChange={(e) => setForm({ ...form, body: e.target.value })}
            />
          </Field>
          <Button type="submit" disabled={send.isPending}>
            Queue message
          </Button>
        </form>
        <MutationStatus
          error={send.error}
          success={
            send.isSuccess
              ? "The notification was accepted into the delivery workflow."
              : undefined
          }
        />
      </StaffSection>
      <StaffSection title="Delivery activity">
        <form
          className="toolbar"
          onSubmit={(e) => {
            e.preventDefault();
            setRecipient(filter);
          }}
        >
          <Field id="notification-filter" label="Recipient UUID">
            <TextField
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
            />
          </Field>
          <Button type="submit">Apply filter</Button>
        </form>
        <QueryState
          loading={messages.isLoading}
          error={messages.error}
          empty={!messages.data?.length}
          onRetry={() => messages.refetch()}
        >
          <Table
            caption="Notification delivery activity"
            headers={[
              "Recipient",
              "Channel",
              "Template",
              "Status",
              "Attempts",
              "Last update",
            ]}
          >
            {messages.data?.map((m) => (
              <tr key={m.id}>
                <td>{m.recipientId}</td>
                <td>{m.channel}</td>
                <td>{m.template}</td>
                <td>
                  <StatusBadge tone={statusTone(m.status)}>
                    {m.status}
                  </StatusBadge>
                  {m.lastError && (
                    <span className="request-id-inline danger-text">
                      Delivery error recorded
                    </span>
                  )}
                </td>
                <td>{m.attempts}</td>
                <td>{formatDate(m.sentAt ?? m.createdAt)}</td>
              </tr>
            ))}
          </Table>
        </QueryState>
      </StaffSection>
    </div>
  );
}
