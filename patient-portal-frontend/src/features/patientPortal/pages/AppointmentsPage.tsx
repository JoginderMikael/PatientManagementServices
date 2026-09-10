import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { queryKeys } from "../../../api/queryKeys";
import { PageHeader } from "../../../components/PageHeader";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import {
  Field,
  Select,
  TextArea,
  ValidationSummary,
} from "../../../components/forms/FormControls";
import { ConfirmAction } from "../../../components/safety/ConfirmAction";
import { cancelAppointmentRequest, createAppointmentRequest } from "../api";
import { MutationError, NoActivity, PortalSection } from "../components";
import { formatPortalDate, PortalStatus } from "../format";
import { PortalBoundary, usePortalOverview } from "../portalState";
import { PortalOverview } from "../types";

const schema = z.object({
  preferredSpecialty: z.string().min(1, "Select a preferred specialty."),
  reason: z
    .string()
    .trim()
    .min(3, "Provide a brief reason for the visit.")
    .max(500, "Keep the reason under 500 characters."),
});
type Values = z.infer<typeof schema>;

export function AppointmentsPage() {
  const state = usePortalOverview();
  const queryClient = useQueryClient();
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { preferredSpecialty: "", reason: "" },
  });
  const key = queryKeys.portalOverview();
  const create = useMutation({
    mutationFn: (values: Values) =>
      createAppointmentRequest(
        { patientId: state.patientId!, ...values },
        state.token,
      ),
    onSuccess: (created) => {
      queryClient.setQueryData<PortalOverview>(key, (current) =>
        current
          ? { ...current, appointments: [created, ...current.appointments] }
          : current,
      );
      form.reset();
    },
  });
  const cancel = useMutation({
    mutationFn: (id: string) => cancelAppointmentRequest(id, state.token),
    onSuccess: (_value, id) =>
      queryClient.setQueryData<PortalOverview>(key, (current) =>
        current
          ? {
              ...current,
              appointments: current.appointments.map((item) =>
                item.id === id ? { ...item, status: "CANCELLED" } : item,
              ),
            }
          : current,
      ),
  });
  const summary = Object.entries(form.formState.errors).map(
    ([fieldId, error]) => ({
      fieldId,
      message: error?.message ?? "Review this field.",
    }),
  );
  return (
    <>
      <PageHeader
        eyebrow="Patient portal"
        title="Appointments"
        description="Submit a care request. Scheduling confirms the final clinician, date and location."
      />
      <PortalBoundary state={state}>
        {state.data && (
          <div className="portal-stack">
            <PortalSection title="Request an appointment">
              {create.isSuccess && (
                <Alert title="Request submitted" tone="success">
                  Scheduling can now review your request.
                </Alert>
              )}
              <MutationError
                error={create.error}
                conflictMessage="Your appointment activity changed. Review the list before trying again."
              />
              <form
                onSubmit={form.handleSubmit((values) => create.mutate(values))}
                noValidate
              >
                <ValidationSummary errors={summary} />
                <div className="form-grid">
                  <Field
                    id="preferredSpecialty"
                    label="Preferred specialty"
                    required
                    error={form.formState.errors.preferredSpecialty?.message}
                  >
                    <Select {...form.register("preferredSpecialty")}>
                      <option value="">Select specialty</option>
                      <option>General medicine</option>
                      <option>Cardiology</option>
                      <option>Dermatology</option>
                      <option>Paediatrics</option>
                    </Select>
                  </Field>
                </div>
                <Field
                  id="reason"
                  label="Reason for visit"
                  required
                  error={form.formState.errors.reason?.message}
                  help="Include only information needed to route the request."
                >
                  <TextArea maxLength={500} {...form.register("reason")} />
                </Field>
                <button
                  className="button button--primary"
                  disabled={create.isPending}
                  type="submit"
                >
                  {create.isPending ? "Submitting…" : "Submit request"}
                </button>
              </form>
            </PortalSection>
            <PortalSection title="Appointment requests">
              <MutationError
                error={cancel.error}
                conflictMessage="This request has already changed and can no longer be cancelled. Refreshing will show its current state."
              />
              {!state.data.appointments.length ? (
                <NoActivity
                  title="No appointment requests"
                  message="Requests you submit will appear here."
                />
              ) : (
                <Table
                  caption="Your appointment requests"
                  headers={[
                    "Specialty",
                    "Reason",
                    "Submitted",
                    "Status",
                    "Action",
                  ]}
                >
                  {state.data.appointments.map((item) => (
                    <tr key={item.id}>
                      <td>{item.preferredSpecialty}</td>
                      <td>{item.reason}</td>
                      <td>{formatPortalDate(item.createdAt)}</td>
                      <td>
                        <PortalStatus value={item.status} />
                      </td>
                      <td>
                        {item.status === "REQUESTED" ? (
                          <ConfirmAction
                            triggerLabel="Cancel request"
                            title="Cancel appointment request"
                            description={`Cancel the unresolved ${item.preferredSpecialty} request?`}
                            confirmLabel="Cancel request"
                            danger
                            onConfirm={() => cancel.mutate(item.id)}
                          />
                        ) : (
                          <span>Resolved</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </Table>
              )}
            </PortalSection>
          </div>
        )}
      </PortalBoundary>
    </>
  );
}
