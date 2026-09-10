import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { queryKeys } from "../../../api/queryKeys";
import { PageHeader } from "../../../components/PageHeader";
import { Table } from "../../../components/data/Table";
import { Alert } from "../../../components/feedback/Alert";
import {
  Checkbox,
  Field,
  Select,
  ValidationSummary,
} from "../../../components/forms/FormControls";
import { createRecordRequest, downloadRecordRequest } from "../api";
import { MutationError, NoActivity, PortalSection } from "../components";
import { formatPortalDate, PortalStatus } from "../format";
import { PortalBoundary, usePortalOverview } from "../portalState";
import { PortalOverview } from "../types";

const schema = z.object({
  recordType: z.string().min(1, "Select a record type."),
  privacyAcknowledged: z.literal(true, {
    errorMap: () => ({
      message: "Confirm that you understand the privacy notice.",
    }),
  }),
});
type Values = z.infer<typeof schema>;

export function RecordsPage() {
  const state = usePortalOverview();
  const queryClient = useQueryClient();
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { recordType: "", privacyAcknowledged: false as true },
  });
  const key = state.patientId
    ? queryKeys.portalOverview(state.patientId)
    : ["portal-overview"];
  const create = useMutation({
    mutationFn: (values: Values) =>
      createRecordRequest(
        { patientId: state.patientId!, recordType: values.recordType },
        state.token,
      ),
    onSuccess: (created) => {
      queryClient.setQueryData<PortalOverview>(key, (current) =>
        current
          ? { ...current, recordRequests: [created, ...current.recordRequests] }
          : current,
      );
      form.reset();
    },
  });
  const download = useMutation({
    mutationFn: async (id: string) => {
      const content = await downloadRecordRequest(id, state.token);
      const url = URL.createObjectURL(
        new Blob([content], { type: "text/plain" }),
      );
      const link = document.createElement("a");
      link.href = url;
      link.download = "records.txt";
      link.rel = "noopener";
      link.click();
      URL.revokeObjectURL(url);
    },
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
        title="Health records"
        description="Request a private text release and download it only after fulfillment."
      />
      <PortalBoundary state={state}>
        {state.data && (
          <div className="portal-stack">
            <PortalSection title="Request health records">
              <Alert title="Private information" tone="warning">
                Released files may contain sensitive health information. Use a
                trusted device.
              </Alert>
              {create.isSuccess && (
                <Alert title="Records requested" tone="success">
                  The request is ready for staff review.
                </Alert>
              )}
              <MutationError error={create.error} />
              <form
                onSubmit={form.handleSubmit((values) => create.mutate(values))}
                noValidate
              >
                <ValidationSummary errors={summary} />
                <Field
                  id="recordType"
                  label="Record type"
                  required
                  error={form.formState.errors.recordType?.message}
                >
                  <Select {...form.register("recordType")}>
                    <option value="">Select record type</option>
                    <option value="CLINICAL_SUMMARY">Clinical summary</option>
                    <option value="LAB_RESULTS">Laboratory results</option>
                    <option value="VISIT_SUMMARIES">Visit summaries</option>
                  </Select>
                </Field>
                <Field
                  id="privacyAcknowledged"
                  label="Privacy acknowledgement"
                  required
                  error={form.formState.errors.privacyAcknowledged?.message}
                >
                  <Checkbox {...form.register("privacyAcknowledged")}>
                    I understand that the released file may contain private
                    health information.
                  </Checkbox>
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
            <PortalSection title="Record requests">
              <MutationError
                error={download.error}
                conflictMessage="This release is not ready yet. Review its current status before trying again."
              />
              {!state.data.recordRequests.length ? (
                <NoActivity
                  title="No record requests"
                  message="Requests and fulfilled releases will appear here."
                />
              ) : (
                <Table
                  caption="Your health record requests"
                  headers={["Record type", "Requested", "Status", "Download"]}
                >
                  {state.data.recordRequests.map((item) => (
                    <tr key={item.id}>
                      <td>{item.recordType.replace(/_/g, " ")}</td>
                      <td>{formatPortalDate(item.createdAt)}</td>
                      <td>
                        <PortalStatus value={item.status} />
                      </td>
                      <td>
                        {item.status === "FULFILLED" ? (
                          <button
                            className="button button--secondary button--small"
                            type="button"
                            disabled={download.isPending}
                            onClick={() => download.mutate(item.id)}
                          >
                            Download records.txt
                          </button>
                        ) : (
                          <span>Not ready</span>
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
